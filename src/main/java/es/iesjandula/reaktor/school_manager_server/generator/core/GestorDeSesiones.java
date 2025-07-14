package es.iesjandula.reaktor.school_manager_server.generator.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.generator.core.threads.IndicesAsignacionSesion;
import es.iesjandula.reaktor.school_manager_server.generator.core.threads.UltimaAsignacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHorariaItem;
import es.iesjandula.reaktor.school_manager_server.services.AsignaturaService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GestorDeSesiones
{
	/** Asignatura service */
	private AsignaturaService asignaturaService ;

    /** Sesiones pendientes */
    private List<List<Sesion>> sesionesPendientes ;

	/** Matriz de asignaciones matutinas */
	private Asignacion[][] matrizAsignacionesMatutinas ;

	/** Matriz de asignaciones vespertinas */
	private Asignacion[][] matrizAsignacionesVespertinas ;

	/** Ultima asignación */
	private UltimaAsignacion ultimaAsignacion ;

	/** Matriz de asignaciones */
	private Asignacion[][] matrizAsignaciones ;

	/**
	 * Constructor de la clase
	 * 
	 * @param asignaturaService asignatura service
	 * @param sesionesPendientes sesiones pendientes
	 * @param matrizAsignacionesMatutinas matriz de asignaciones matutinas
	 * @param matrizAsignacionesVespertinas matriz de asignaciones vespertinas
	 * @param ultimaAsignacion ultima asignación
	 */
	public GestorDeSesiones(AsignaturaService asignaturaService,
							List<List<Sesion>> sesionesPendientes,
							Asignacion[][] matrizAsignacionesMatutinas,
							Asignacion[][] matrizAsignacionesVespertinas,
							UltimaAsignacion ultimaAsignacion)
	{
		this.asignaturaService 			   = asignaturaService ;
		this.sesionesPendientes 		   = sesionesPendientes ;
		this.matrizAsignacionesMatutinas   = matrizAsignacionesMatutinas ;
		this.matrizAsignacionesVespertinas = matrizAsignacionesVespertinas ;
		this.ultimaAsignacion	 		   = ultimaAsignacion ;
	}

	/**
     * @param ultimaAsignacion ultima asignacion
     * @return una de las sesiones pendientes de asignar
     */
    public Sesion obtenerSesionParaAsignar()
    {
    	Sesion outcome = null ;

		// Buscamos el último índice que tenga elementos en la lista de sesiones pendientes
		int ultimoIndiceConElementos = this.buscarUltimoIndiceConElementosEnListaDeSesionesPendientes() ;

		// Nos vamos a la lista que tenga más restricciones
		List<Sesion> listaDeSesiones = this.sesionesPendientes.get(ultimoIndiceConElementos) ;
    	
		// Si hay una asignación previa y resulta que NO es una asignatura de ESO o BACHILLERATO, es decir, es de FP ...
		if (this.ultimaAsignacion != null && !this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0).getAsignatura().isEsoBachillerato())
		{
			// Asignamos previamente la matriz de sesiones ya que nos basamos en la asignación anterior
			this.seleccionarMatrizAsignaciones(listaDeSesiones.get(0)) ;

			// ... vamos a intentar buscar una igual ya que así conseguimos que haya varias seguidas en el mismo día
			outcome = this.obtenerAsignaturaFP(listaDeSesiones) ;
		}
		
		// Nos centramos en las restricciones de la lista en la que estemos
		if (outcome == null && ultimoIndiceConElementos == Constants.INDEX_CON_CONCILIACIONES)
		{
			// Tratamos de obtener una sesión para asignar relacionada con las conciliaciones
			outcome = this.obtenerSesionParaAsignarEnConciliaciones(listaDeSesiones) ;
		}
		
		// Nos centramos en las restricciones de la lista de optativas
		if (outcome == null && ultimoIndiceConElementos == Constants.INDEX_CON_OPTATIVAS)
		{
			outcome = this.obtenerSesionParaAsignarEnOptativas(listaDeSesiones) ;
		}

		// Llegados a este punto, no encontramos ninguna sesión válida ...
		if (outcome == null)
		{
			// ... elegimos una aleatoria
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, 0) ;
		}

		// Llegados a este punto, ya tenemos firmemente la matriz de sesiones
		this.seleccionarMatrizAsignaciones(outcome) ;
		
		// Si este elemento era el último ... 
		if (listaDeSesiones.size() == 0)
		{
			// Borramos la sublista
			this.sesionesPendientes.remove(ultimoIndiceConElementos) ;
		}
				
		return outcome ;
	}

	/**
     * Selecciona la matriz de asignaciones apropiada basada en el tipo de horario
     * @param sesion sesión
     */
    private void seleccionarMatrizAsignaciones(Sesion sesion)
    {
        // Si es matutino ...
        if (sesion.getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getHorarioMatutino())
        {
        	// ... elegimos la matriz de asignaciones matutinas
            this.matrizAsignaciones = this.matrizAsignacionesMatutinas ;
        }
        else
        {
            // ... elegimos la matriz de asignaciones vespertinas
            this.matrizAsignaciones = this.matrizAsignacionesVespertinas ;
        }
    }

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @return una sesión de FP asociada a la asignatura de la última asignación
	 */
	private Sesion obtenerAsignaturaFP(List<Sesion> listaDeSesiones)
	{
		Sesion outcome = null ;

		// Obtenemos la asignatura de la última asignación
		Asignatura asignatura = this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0).getAsignatura() ;

		// Obtenemos el índice del curso-día de la última asignación
		int indiceCursoDia = this.ultimaAsignacion.getIndicesAsignacionSesion().getIndiceCursoDia() ;

		// Validamos si el día de la sesión NO es correcto
		if (this.validarCursoDia(indiceCursoDia, asignatura))
		{
			// Si es válido, tratamos de obtener la sesión
			outcome = this.obtenerAsignaturaFPInternal(listaDeSesiones) ;
		}

		return outcome ;
	}

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @return una sesión de FP asociada a la asignatura de la última asignación
	 */
	private Sesion obtenerAsignaturaFPInternal(List<Sesion> listaDeSesiones)
	{
		Sesion outcome = null ;

		int i = 0 ;
		int indiceSesionEncontrada = -1 ;

		// Iteramos en la lista de sesiones pendientes
		while (i < listaDeSesiones.size() && indiceSesionEncontrada == -1)
		{
			Sesion temp = listaDeSesiones.get(i) ;

			// Obtenemos la lista de sesiones de la última asignación
			List<Sesion> listaSesiones = this.ultimaAsignacion.getAsignacion().getListaSesiones() ;

			// Iteramos en la lista de sesiones de la última asignación
			int j = 0 ;
			while (j < listaSesiones.size() && indiceSesionEncontrada == -1)
			{
				// Si la asignatura de la lista de sesiones pendientes es la misma que la de la última asignación ...
				if (listaSesiones.get(j).getAsignatura().equals(temp.getAsignatura()))
				{
					// ... encontramos la sesión
					indiceSesionEncontrada = i ;
				}

				j++ ;
			}

			i++ ;
		}	

		// Si encontramos alguna ...
		if (indiceSesionEncontrada != -1)
		{
			// ... obtenemos la sesión, la borramos y mezclamos la lista
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, indiceSesionEncontrada) ;
		}

		return outcome ;
	}
	
	/**
	 * @param listaDeSesiones lista de sesiones
	 * @return una sesión con la conciliación de entrar a primera hora y sino, alguna otra
	 */
	private Sesion obtenerSesionParaAsignarEnConciliaciones(List<Sesion> listaDeSesiones)
	{
		// Me interesa asignar primero las conciliaciones de entrar a primera hora
		int i = 0 ;
		int indiceSesionEncontrada = -1 ;
		while (i < listaDeSesiones.size() && indiceSesionEncontrada == -1)
		{
			Sesion temp = listaDeSesiones.get(i) ;

			ObservacionesAdicionales observacionesAdicionales = temp.getProfesor().getObservacionesAdicionales() ;

			if (observacionesAdicionales != null && observacionesAdicionales.getConciliacion() != null && observacionesAdicionales.getConciliacion())
			{
				indiceSesionEncontrada = i ;
			}

			i++ ;
		}	

		Sesion outcome = null ;

		// Si encontramos alguna ...
		if (indiceSesionEncontrada != -1)
		{
			// ... obtenemos la sesión, la borramos y mezclamos la lista
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, indiceSesionEncontrada) ;
		}

		return outcome ;
	}

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @return una sesión de optativas
	 */
	private Sesion obtenerSesionParaAsignarEnOptativas(List<Sesion> listaDeSesiones)
	{
		Sesion outcome = null ;

		// Si la última asignación es una optativa ...
		if (this.ultimaAsignacion != null && this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0).getAsignatura().isOptativa())
		{
			// ... tratamos de buscar la asignatura asociada a esta
			outcome = this.obtenerSesionParaAsignarEnOptativasInternal(listaDeSesiones) ;
		}

		return outcome ;
	}

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @return una sesión de optativas
	 */
	private Sesion obtenerSesionParaAsignarEnOptativasInternal(List<Sesion> listaDeSesiones)
	{
		Sesion outcome = null ;
		
		// Obtenemos la/s optativa/s de la última asignación
		List<Asignatura> bloqueOptativas = this.asignaturaService.buscaOptativasRelacionadas(this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0).getAsignatura()) ;
		
		// Iteramos en la lista de sesiones pendientes
		int i = 0 ;
		int indiceSesionEncontrada = -1 ;
		while (i < listaDeSesiones.size() && indiceSesionEncontrada == -1)
		{
			Sesion temp = listaDeSesiones.get(i) ;

			// Si la asignatura es una de las optativas y no la contiene la asignación actual ...
			if (bloqueOptativas.contains(temp.getAsignatura()) && !this.ultimaAsignacion.getAsignacion().getListaSesiones().contains(temp))
			{
				// ... encontramos la sesión
				indiceSesionEncontrada = i ;
			}

			i++ ;
		}

		// Si encontramos alguna ...
		if (indiceSesionEncontrada != -1)
		{
			// ... obtenemos la sesión, la borramos y mezclamos la lista
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, indiceSesionEncontrada) ;
		}
		
		return outcome ;
	}

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @param indiceElemento índice del elemento a eliminar
	 * @return una sesión aleatoria
	 */
	private Sesion obtenerSesionBorrarYmezclar(List<Sesion> listaDeSesiones, int indiceElemento)
	{
		// Ahora, eliminamos y obtenemos el primero que encontremos
		Sesion sesion = listaDeSesiones.remove(indiceElemento) ;

		// Mezclamos aleatoriamente todas ellas
		Collections.shuffle(listaDeSesiones) ;

		return sesion ;
	}
				
	/**
     * @param sesion sesión
	 * @param esAsignaturaMatutina true si es asignatura matutina
     * @param numeroCursos número de cursos
	 * @param indiceCursoDiaInicial índice del curso y día inicial
	 * @return ultima asignacion
     * @throws SchoolManagerServerException con un error
     */
    public UltimaAsignacion asignarSesion(Sesion sesion,
                                          boolean esAsignaturaMatutina,
                                          int numeroCursos,
                                          int indiceCursoDiaInicial) throws SchoolManagerServerException
    {
        // Obtener restricción horaria de esa sesion
		RestriccionHoraria restriccionHoraria = this.obtenerRestriccionHorariaDeSesion(sesion, esAsignaturaMatutina, numeroCursos, indiceCursoDiaInicial) ;

		// Obtenemos el siguiente item de la restricción horaria
		RestriccionHorariaItem restriccionHorariaItem = restriccionHoraria.obtenerRestriccionHorariaItem(sesion) ;
		
		// Creamos una nueva instancia de indicesAsignacionSesion
		IndicesAsignacionSesion indicesAsignacionSesion = new IndicesAsignacionSesion(restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario()) ;

		// Asignamos la sesión
		Asignacion asignacion = this.asignarSesion(sesion, indicesAsignacionSesion) ;

		// Devolvemos la última asignación
        return new UltimaAsignacion(indicesAsignacionSesion, asignacion) ;
    }
    
    /**
     * @param sesion sesion
	 * @param asignaturaMatutina asignatura matutina
	 * @param numeroCursos número de cursos
     * @param indiceCursoDiaInicial índice del curso y día inicial
     * @return una nueva instancia que restringe los horarios (días y horas)
     * @throws SchoolManagerServerException con un error
     */
    private RestriccionHoraria obtenerRestriccionHorariaDeSesion(Sesion sesion,
																 boolean esAsignaturaMatutina,
																 int numeroCursos,
    															 int indiceCursoDiaInicial) throws SchoolManagerServerException
    {
		// Obtenemos la posible restricción horaria impuesta sobre la sesión
		RestriccionHoraria restriccionHoraria = sesion.getRestriccionHoraria() ;

		// Si no hay restricciones horarias impuestas sobre la sesión, vamos a crear un builder que vamos a ir enriqueciendo con restricciones
		if (sesion.getRestriccionHoraria() == null)
		{
			// Vamos a crear un builder que vamos a ir enriqueciendo con restricciones
			// antes de devolver la instancia de Restriccion Horaria
			RestriccionHoraria.Builder builder = new RestriccionHoraria.Builder(indiceCursoDiaInicial) ;

			// Obtenemos las restricciones relacionadas con el día y tramo horario
			this.obtenerRestriccionHorariaDeSesionPorDiaTramo(sesion, numeroCursos, builder) ;

			// Ahora en la conciliación de los profesores siempre que la asignatura sea matutina
			if (esAsignaturaMatutina)
			{
				this.obtenerRestriccionHorariaDeSesionPorConciliacion(sesion, builder) ;
			}

			// Ahora vemos si hay que restringir por bloques de asignaturas (optativas) si es que es una optativa
			if (sesion.getAsignatura().isOptativa())
			{
				this.obtenerRestriccionHorariaDeSesionPorOptativas(sesion, builder) ;
			}

			// Si no es ESO ni BACHILLERATO, la restricción será a nivel de FP
			if (!sesion.getAsignatura().isEsoBachillerato())
			{
				this.obtenerRestriccionHorariaDeSesionPorModuloFp(sesion, builder) ;
			}

			// Hacemos un build para obtener la instancia de Restriccion Horaria
			restriccionHoraria = builder.build() ;
		}

		return restriccionHoraria ;
	}
    
    /**
     * @param sesion sesion
     * @param builder restric horaria builder
     */
	private void obtenerRestriccionHorariaDeSesionPorConciliacion(Sesion sesion, RestriccionHoraria.Builder builder)
	{
    	if (sesion.getProfesor().getObservacionesAdicionales().getConciliacion())
		{
			if (sesion.getProfesor().getObservacionesAdicionales().getSinClasePrimeraHora())
			{
				builder = builder.sinClasePrimeraHora() ;
			}
			else
			{
				builder = builder.conClasePrimerHora() ;
			}
		}
	}

    /**
     * @param sesion sesion
     * @param builder restriccion horaria builder
     * @throws SchoolManagerServerException con un error
     */
	private void obtenerRestriccionHorariaDeSesionPorOptativas(Sesion sesion,
															   RestriccionHoraria.Builder builder) throws SchoolManagerServerException
	{
		boolean encontrado = false ;

		// Obtenemos la lista de restricciones horarias de esta sesión
		List<RestriccionHorariaItem> restriccionesHorarias = new ArrayList<>(builder.getRestriccionesHorarias()) ;

		// Iteramos y obtenemos las sesiones actualmente asociadas a cada día
		Iterator<RestriccionHorariaItem> iterator = restriccionesHorarias.iterator() ;
		while (iterator.hasNext() && !encontrado)
		{
			// Obtenemos el siguiente item de la restricción horaria
			RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

			// Si:;
				// justo en este día y tramo horario no hay una asignación o 
				// si la hay es la misma asignatura o 
				// si la asignatura no es optativa o
				// si la asignatura no es del bloque de optativas
			// Entonces: la borramos del builder
			boolean restriccionHorariaInvalida = this.matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()] == null ||
												 this.buscarAsignatura(this.matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()], sesion.getAsignatura()) ||
												!this.matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()].isOptativas() || 
												 this.matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()].getListaSesiones().get(0).getAsignatura().getBloqueId().getId() != sesion.getAsignatura().getBloqueId().getId() ;
			if (restriccionHorariaInvalida)
			{
				builder = builder.eliminarRestriccionHorariaItem(restriccionHorariaItem) ;
			}
			else
			{
				builder = builder.hacerCoincidirConOptativaDelBloque(restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario()) ;
			}
		}
	}

    /**
     * @param sesion sesion
     * @param builder restriccion horaria builder
     * @throws SchoolManagerServerException con un error
     */
    private void obtenerRestriccionHorariaDeSesionPorModuloFp(Sesion sesion,
                                                              RestriccionHoraria.Builder builder) throws SchoolManagerServerException
    {
		boolean encontrado = false ;

		// Obtenemos la lista de restricciones horarias de esta sesión
		List<RestriccionHorariaItem> restriccionesHorarias = new ArrayList<>(builder.getRestriccionesHorarias()) ;

		// Iteramos y obtenemos las sesiones actualmente asociadas a cada día
		Iterator<RestriccionHorariaItem> iterator = restriccionesHorarias.iterator() ;
		while (iterator.hasNext() && !encontrado)
		{
			// Obtenemos el siguiente item de la restricción horaria
			RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

			// Si justo en este día y tramo horario hay una asignación, la borramos del builder
			if (this.matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()] != null)
			{
				builder = builder.eliminarRestriccionHorariaItem(restriccionHorariaItem) ;
			}
			else
			{
				// Verificamos si hay un índice donde se pueda realizar la asignación
				encontrado = this.obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndice(restriccionHorariaItem, sesion) ;

				if (encontrado)
				{
					builder = builder.hacerCoincidirConModuloFp(restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario()) ;
				}
			}
		}
    }

	/**
	 * @param sesion sesion
	 * @param numeroCursos número de cursos
	 * @param builder restriccion horaria builder
	 */
	private void obtenerRestriccionHorariaDeSesionPorDiaTramo(Sesion sesion, int numeroCursos, RestriccionHoraria.Builder builder)
	{	
		List<RestriccionHorariaItem> restriccionesHorarias = new ArrayList<>(builder.getRestriccionesHorarias()) ;

		// Iteramos y quitamos todos aquellos items incompatibles
		Iterator<RestriccionHorariaItem> iterator = restriccionesHorarias.iterator() ;
		while (iterator.hasNext())
		{
			RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;
			
			if (!this.validarCursoDia(restriccionHorariaItem.getIndiceDia(), sesion.getAsignatura()) || 
				!this.validarCursoDiaTramo(numeroCursos, restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario(), sesion))
			{
				builder = builder.eliminarRestriccionHorariaItem(restriccionHorariaItem) ;
			}
		}
	}

	/**
	 * @param restriccionHorariaItem restriccion horaria item
	 * @param sesion sesion
	 * @return true si se ha encontrado la misma asignatura
	 */
	private boolean obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndice(RestriccionHorariaItem restriccionHorariaItem, Sesion sesion)
	{
		boolean encontrado = false ;

		// Obtenemos el índice del curso y día
		int indiceCursoDia = restriccionHorariaItem.getIndiceDia() ;

		// Si el tramo horario es 0, solo vemos lo que hay en el tramo 1
		if (restriccionHorariaItem.getTramoHorario() == 0)
		{
			// Verificamos si hay una asignación con la misma asignatura
			encontrado = this.obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndiceVerificarAsignacion(this.matrizAsignaciones[indiceCursoDia][1], sesion) ;
		}
		// Si el tramo horario es el último, solo vemos lo que hay en el tramo anterior
		else if (restriccionHorariaItem.getTramoHorario() == Constants.NUMERO_TRAMOS_HORARIOS - 1)
		{
			// Verificamos si hay una asignación con la misma asignatura
			encontrado = this.obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndiceVerificarAsignacion(this.matrizAsignaciones[indiceCursoDia][Constants.NUMERO_TRAMOS_HORARIOS - 2], 
				                                                                                             sesion) ;
		}
		// Si no es el primero ni el último, vemos lo que hay en el tramo anterior y posterior
		else
		{
			// Verificamos si hay una asignación con la misma asignatura
			encontrado = this.obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndiceVerificarAsignacion(this.matrizAsignaciones[indiceCursoDia][restriccionHorariaItem.getTramoHorario() - 1], 
				                                                                                             sesion) ;

			// Si no hay una asignación con la misma asignatura, vemos lo que hay en el tramo posterior
			if (!encontrado)
			{
				// Verificamos si hay una asignación con la misma asignatura
				encontrado = this.obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndiceVerificarAsignacion(this.matrizAsignaciones[indiceCursoDia][restriccionHorariaItem.getTramoHorario() + 1], 
				                                                                                                 sesion) ;
			}
		}

		return encontrado ;
	}

	
	/**
	 * @param asignacionTemporal asignacion temporal
	 * @param sesion sesion
	 * @return true si se ha encontrado la misma asignatura
	 */
	private boolean obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndiceVerificarAsignacion(Asignacion asignacionTemporal, Sesion sesion)
	{
		boolean encontrado = false ;
		
		// Si hay una asignación, verificamos
		if (asignacionTemporal != null)
		{
			// Verificamos si hay una asignación con la misma asignatura
			encontrado = this.obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndiceMismaAsignatura(asignacionTemporal, sesion) ;
		}

		return encontrado ;
	}

	/**
	 * @param asignacionTemporal asignacion temporal
	 * @param sesion sesion
	 * @return true si se ha encontrado la misma asignatura
	 */
	private boolean obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndiceMismaAsignatura(Asignacion asignacionTemporal, Sesion sesion)
	{
		boolean encontrado = false ;

		// Iteramos y verificamos si alguna sesión corresponde a la misma asignatura
		Iterator<Sesion> iterator = asignacionTemporal.getListaSesiones().iterator() ;
		while (iterator.hasNext() && !encontrado)
		{
			Sesion sesionTemp = iterator.next() ;
			
			encontrado = sesionTemp.getAsignatura().equals(sesion.getAsignatura()) ;
		}

		return encontrado ;
	}

    /**
     * @return el último índice que tenga elementos
     */
	public int buscarUltimoIndiceConElementosEnListaDeSesionesPendientes()
	{
		int ultimoIndiceConElementos = -1 ;
    	
    	int indice = this.sesionesPendientes.size() - 1 ;
    	while (indice > -1 && ultimoIndiceConElementos == -1)
    	{
    		if (this.sesionesPendientes.get(indice).size() > 0)
    		{
    			ultimoIndiceConElementos = indice ;
    		}
    		
    		indice -- ;
    	}
    	
		return ultimoIndiceConElementos ;
	}

	/**
     * @param indiceCursoDia curso asociado a este curso-etapa-grupo (día concreto)
	 * @param asignatura asignatura
	 * @return true si es correcto este día
	 */
	public boolean validarCursoDia(int indiceCursoDia, Asignatura asignatura)
	{
		boolean outcome = true ;
		
        // Si es una asignatura de ESO o BACH ...
        if (asignatura.isEsoBachillerato())
        {
        	// ... no puede haber dos sesiones de la misma asignatura el mismo día
        	outcome = this.asignaturaSinMasXOcurrenciasElMismoDia(indiceCursoDia,
																  asignatura,
																  Constants.NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_ESO_BACH) ;
        }
		else
		{
        	// ... no puede haber dos sesiones de la misma asignatura el mismo día
        	outcome = this.asignaturaSinMasXOcurrenciasElMismoDia(indiceCursoDia,
																  asignatura,
																  Constants.NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_FP) ;
		}
        
        return outcome ;
	}

		/**
     * @param indiceCursoDia curso asociado a esta clase
     * @param asignatura asignatura
	 * @param numeroMaximoOcurrenciasPorDia número de ocurrencias máxima por día
     * @return true si la asignatura no se repite el mismo día
     */
	private boolean asignaturaSinMasXOcurrenciasElMismoDia(int indiceCursoDia,
														   Asignatura asignatura,
														   int numeroMaximoOcurrenciasPorDia)
	{
        boolean asignaturaCumpleOcurrencias = true ;

		int vecesImpartida = 0 ;
        
        int i=0 ;
        while (i < this.matrizAsignaciones[indiceCursoDia].length && asignaturaCumpleOcurrencias)
        {
    		// Si es true es porque la asignatura se ha impartido durante este día
        	if (this.matrizAsignaciones[indiceCursoDia][i] != null && this.buscarAsignatura(this.matrizAsignaciones[indiceCursoDia][i], asignatura))
			{
				vecesImpartida ++ ;

				asignaturaCumpleOcurrencias = vecesImpartida < numeroMaximoOcurrenciasPorDia ;
			}

            i++ ;
        }
        
        return asignaturaCumpleOcurrencias ;
	}
	
	/**
	 * @param asignacion asignación
	 * @param asignatura asignatura
	 * @return true si se encuentra el profesor en las asignaciones
	 */
    private boolean buscarAsignatura(Asignacion asignacion, Asignatura asignatura)
    {
    	boolean outcome = false ;
    	
    	int i = 0 ;
    	while (i < asignacion.getListaSesiones().size() && !outcome)
    	{
    		outcome = asignacion.getListaSesiones().get(i).getAsignatura().equals(asignatura) ;
    		
    		i++ ;
    	}
    	
		return outcome ;
	}

	/**
     * @param sesion sesión
     * @param asignacionSesion asignacion sesion
	 * @return asignacion con la sesión asignada
     * @throws SchoolManagerServerException con un error
     */
	protected Asignacion asignarSesion(Sesion sesion, IndicesAsignacionSesion indicesAsignacionSesion) throws SchoolManagerServerException
	{
		// Obtenemos la asignación actual
		Asignacion asignacion = this.matrizAsignaciones[indicesAsignacionSesion.getIndiceCursoDia()][indicesAsignacionSesion.getIndiceTramoHorario()] ;
		
		// Si no hay ninguna asignación, inicializamos la instancia
		if (asignacion == null)
		{
			asignacion = new Asignacion() ;
			this.matrizAsignaciones[indicesAsignacionSesion.getIndiceCursoDia()][indicesAsignacionSesion.getIndiceTramoHorario()] = asignacion ;
		}
		
		// Si es una optativa, lo indicamos en la asignación
		asignacion.setOptativas(sesion.getAsignatura().isOptativa()) ;
		
		// Introducimos la sesion en la lista
		asignacion.getListaSesiones().add(sesion) ;
		
		return asignacion ;
	}

	/**
	 * @param numeroCursos numero de cursos
	 * @param indiceCursoDia índice curso día
	 * @param indiceTramoHorario índice tramo horario
	 * @param sesion sesion que se quiere verificar
	 * @return true si la hora está disponible en este curso para dar la sesión de clase
	 */
	protected boolean validarCursoDiaTramo(int numeroCursos, int indiceCursoDia, int indiceTramoHorario, Sesion sesion)
	{
		// Obtenemos el profesor
        Profesor profesor = sesion.getProfesor() ;

		// Obtenemos la asignatura
        Asignatura asignatura = sesion.getAsignatura() ;
        
        // Verificamos si el profesor ya está asignado en esa hora en este grupo o en otro
        boolean disponible = this.profesorSinSesionEnEstaHora(numeroCursos, indiceCursoDia, indiceTramoHorario, profesor) ;
        
        // Verificamos que a esa hora no se haya asignado nada siempre que no sea optativa
        if (disponible && !sesion.getAsignatura().isOptativa())
        {
        	disponible = this.matrizAsignaciones[indiceCursoDia][indiceTramoHorario] == null ;
        }
        
        // Validamos el bloque de optativas (si tuviera)
        if (disponible && asignatura.isOptativa())
        {
        	Asignacion asignacion = this.matrizAsignaciones[indiceCursoDia][indiceTramoHorario] ;
        	
        	disponible = asignacion == null || this.asignaturaEnBloqueDeOptativas(this.asignaturaService.buscaOptativasRelacionadas(asignatura), asignacion.getListaSesiones()) ;
        }
        
        return disponible ;
    }
	
	/**
     * @param numeroCursos numero de cursos
     * @param indiceCursoDia índice curso día
     * @param indiceTramoHorario hora sobre la que verificar
	 * @param profesor profesor
	 * @return true si el profesor ya tiene una clase asignada a esa misma hora
	 */
	private boolean profesorSinSesionEnEstaHora(int numeroCursos, int indiceCursoDia, int indiceTramoHorario, Profesor profesor)
	{
        boolean profesorSinSesionEnEstaHora = true ;
        
        // Obtengo el número entre 0-5 que equivale al día
        int diaExacto = indiceCursoDia % Constants.NUMERO_DIAS_SEMANA ;
        
        int i = 0 ;
        
        // Tendremos que buscar en ese día actual en otros cursos
        while (i < numeroCursos && profesorSinSesionEnEstaHora)
        {
        	// Vamos iterando por el mismo día pero en diferentes cursos, en el día de la semana concreto
        	int indiceCursoBusqueda = diaExacto + (i * Constants.NUMERO_DIAS_SEMANA) ;
        	
    		// Si es true es porque el profesor NO tiene clase asignada a esa misma hora
    		profesorSinSesionEnEstaHora = this.matrizAsignaciones[indiceCursoBusqueda][indiceTramoHorario] == null ||
    									 !this.buscarProfesor(this.matrizAsignaciones[indiceCursoBusqueda][indiceTramoHorario], profesor) ;
            i++ ;
        }
        
        return profesorSinSesionEnEstaHora ;
	}
	
	/**
	 * @param asignacion asignación
	 * @param profesor profesor
	 * @return true si se encuentra el profesor en las asignaciones
	 */
    private boolean buscarProfesor(Asignacion asignacion, Profesor profesor)
    {
    	boolean outcome = false ;
    	
    	int i = 0 ;
    	while (i < asignacion.getListaSesiones().size() && !outcome)
    	{
    		outcome = asignacion.getListaSesiones().get(i).getProfesor().equals(profesor) ;
    		
    		i++ ;
    	}
    	
		return outcome ;
	}
    
    /**
     * @param bloqueOptativas bloque de optativas
     * @param listaSesiones lista de sesiones a verificar si alguna de las asignaturas pertenece al bloque de optativas
     * @return true si alguna de las asignaturas pertenece al bloque de optativas
     */
	private boolean asignaturaEnBloqueDeOptativas(List<Asignatura> bloqueOptativas, List<Sesion> listaSesiones)
	{
		boolean outcome = false ;
		
		// Iteramos sobre todas las sesiones de esta asignación para ver si son del bloque de optativas
		
		int k = 0 ;
		while (k < listaSesiones.size() && !outcome)
		{
			// Obtenemos la sesión de esta asignación
			Sesion sesionTemporal = listaSesiones.get(k) ;
			
			// Verificamos si pertenece la asignatura al bloque de optativas que estamos buscando
			outcome = bloqueOptativas.contains(sesionTemporal.getAsignatura()) ;
			
			k++ ;
		}
		
		return outcome ;
	}
}
