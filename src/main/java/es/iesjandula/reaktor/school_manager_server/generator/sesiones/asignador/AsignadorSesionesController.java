package es.iesjandula.reaktor.school_manager_server.generator.sesiones.asignador;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.PreferenciasHorariasProfesor;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHorariaThread;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHorariaItem;
import es.iesjandula.reaktor.school_manager_server.services.manager.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.generator.threads.UltimaAsignacion;
import es.iesjandula.reaktor.school_manager_server.generator.sesiones.SesionesUtils;
import es.iesjandula.reaktor.school_manager_server.generator.threads.IndicesAsignacionSesion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsignadorSesionesController
{
	/** Asignador de sesiones de asignaturas */
	private AsignadorSesionesAsignaturas asignadorSesionesAsignaturas ;

	/**
	 * Constructor de la clase
	 * 
	 * @param asignaturaService asignatura service
	 */
	public AsignadorSesionesController(AsignaturaService asignaturaService)
	{
		this.asignadorSesionesAsignaturas = new AsignadorSesionesAsignaturas(asignaturaService) ;
	}
				
	/**
	 * @param matrizAsignaciones matriz de asignaciones
     * @param sesion sesión
     * @param numeroCursos número de cursos
	 * @param indiceCursoDiaInicial índice del curso y día inicial
	 * @return ultima asignacion
     * @throws SchoolManagerServerException con un error
     */
    public UltimaAsignacion asignarSesion(Asignacion[][] matrizAsignaciones,
										  SesionBase sesion,
										  int numeroCursos,
										  int indiceCursoDiaInicial) throws SchoolManagerServerException
    {
        // Obtener restricción horaria de iteración de esa sesion
		RestriccionHorariaThread restriccionHorariaThread = 
			this.obtenerRestriccionHorariaDeSesion(matrizAsignaciones, sesion, numeroCursos, indiceCursoDiaInicial) ;

		// Obtenemos el siguiente item de la restricción horaria
		RestriccionHorariaItem restriccionHorariaItem = restriccionHorariaThread.obtenerRestriccionHorariaItem(sesion) ;
		
		// Creamos una nueva instancia de indicesAsignacionSesion
		IndicesAsignacionSesion indicesAsignacionSesion = new IndicesAsignacionSesion(restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario()) ;

		// Asignamos la sesión
		Asignacion asignacion = this.asignarSesion(matrizAsignaciones, sesion, indicesAsignacionSesion) ;

		// Devolvemos la última asignación
        return new UltimaAsignacion(indicesAsignacionSesion, asignacion) ;
    }
    
    /**
     * @param matrizAsignaciones matriz de asignaciones
     * @param sesion sesion
	 * @param numeroCursos número de cursos
     * @param indiceCursoDiaInicial índice del curso y día inicial
     * @return una nueva instancia que restringe los horarios (días y horas)
     * @throws SchoolManagerServerException con un error
     */
    private RestriccionHorariaThread obtenerRestriccionHorariaDeSesion(Asignacion[][] matrizAsignaciones,
		                                                               SesionBase sesion,
																       int numeroCursos,
    															       int indiceCursoDiaInicial) throws SchoolManagerServerException
    {
		// Obtenemos la posible restricción horaria iteración impuesta sobre la sesión
		RestriccionHorariaThread restriccionHorariaThread = sesion.getRestriccionHorariaThread() ;

		// Obtenemos las restricciones relacionadas con el día y tramo horario
		this.obtenerRestriccionHorariaDeSesionPorDiaTramo(matrizAsignaciones, sesion, numeroCursos, restriccionHorariaThread) ;

		// Ahora vemos si la sesión es de tipo asignatura
		boolean esAsignatura = sesion instanceof SesionAsignatura ;
		if (esAsignatura)
		{
			SesionAsignatura sesionAsignatura = (SesionAsignatura) sesion ;

			// Ahora vemos si hay que restringir por bloques de asignaturas (optativas) si es que es una optativa
			if (sesionAsignatura.getAsignatura().isOptativa())
			{
				this.obtenerRestriccionHorariaDeSesionPorOptativas(matrizAsignaciones, sesionAsignatura.getAsignatura(), restriccionHorariaThread) ;
			}

			// Si no es ESO ni BACHILLERATO, la restricción será a nivel de FP
			if (!sesionAsignatura.isEsoBachillerato())
			{
				this.obtenerRestriccionHorariaDeSesionPorModuloFp(matrizAsignaciones, sesionAsignatura.getAsignatura(), restriccionHorariaThread) ;
			}
		}
		else // Entonces es una reducción
		{
			// No hacemos nada por ahora
		}

		return restriccionHorariaThread ;
	}
	
	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param sesion sesion
	 * @param numeroCursos número de cursos
	 * @param restriccionHorariaThread restriccion horaria thread
	 */
	private void obtenerRestriccionHorariaDeSesionPorDiaTramo(Asignacion[][] matrizAsignaciones,
															  SesionBase sesion,
															  int numeroCursos,
															  RestriccionHorariaThread restriccionHorariaThread)
	{
		// Introducimos en una misma lista las restricciones no evitables y las evitables
		List<RestriccionHorariaItem> restriccionesHorarias = new ArrayList<>() ;
		restriccionesHorarias.addAll(restriccionHorariaThread.getRestriccionesHorariasNoEvitables()) ;
		restriccionesHorarias.addAll(restriccionHorariaThread.getRestriccionesHorariasEvitables()) ;

		// Iteramos y quitamos todos aquellos items incompatibles
		Iterator<RestriccionHorariaItem> iterator = restriccionesHorarias.iterator() ;
		while (iterator.hasNext())
		{
			RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

			// Vemos si se cumple el número máximo de ocurrencias por día y si el profesor no tiene sesión en esta hora ya asignada
			boolean restriccionIncompatible = 
			  !SesionesUtils.sesionSinMasXOcurrenciasElMismoDia(matrizAsignaciones, restriccionHorariaItem.getIndiceDia(), sesion) ||
			  !this.profesorSinSesionEnEstaHora(matrizAsignaciones, numeroCursos, restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario(), sesion.getProfesor()) ;

			// Si todavía es compatible la restricción ...
			if (!restriccionIncompatible)
			{
				if (sesion instanceof SesionAsignatura)
				{
					restriccionIncompatible = 
					  this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorDiaTramo(matrizAsignaciones, ((SesionAsignatura) sesion).getAsignatura(), restriccionHorariaItem) ;
				}
			}

			if (restriccionIncompatible)
			{
				restriccionHorariaThread.eliminarRestriccionHorariaItem(restriccionHorariaItem) ;
			}
		}
	}

	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param asignatura asignatura
	 * @param restriccionHorariaThread restriccion horaria thread
	 */
	private void obtenerRestriccionHorariaDeSesionPorOptativas(Asignacion[][] matrizAsignaciones, Asignatura asignatura, RestriccionHorariaThread restriccionHorariaThread)
	{
		// Creamos un nuevo ArrayList de RestriccionHorariaItem sobre las restricciones no evitables
		List<RestriccionHorariaItem> restriccionesHorariasNoEvitables = new ArrayList<>(restriccionHorariaThread.getRestriccionesHorariasNoEvitables()) ;

		// Obtenemos las restricciones de sesión por optativas en las no evitables
		boolean encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorOptativas(matrizAsignaciones, asignatura, restriccionHorariaThread, restriccionesHorariasNoEvitables) ;

		// Obtenemos las restricciones de sesión por optativas en las evitables
		if (!encontrado)
		{
			// Creamos un nuevo ArrayList de RestriccionHorariaItem sobre las restricciones evitables
			List<RestriccionHorariaItem> restriccionesHorariasEvitables = new ArrayList<>(restriccionHorariaThread.getRestriccionesHorariasEvitables()) ;

			// Obtenemos las restricciones de sesión por optativas en las evitables
			encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorOptativas(matrizAsignaciones, asignatura, restriccionHorariaThread, restriccionesHorariasEvitables) ;
		}
	}

	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param asignatura asignatura
	 * @param restriccionHorariaThread restriccion horaria thread
	 */
	private void obtenerRestriccionHorariaDeSesionPorModuloFp(Asignacion[][] matrizAsignaciones, Asignatura asignatura, RestriccionHorariaThread restriccionHorariaThread)
	{
		// Creamos un nuevo ArrayList de RestriccionHorariaItem sobre las restricciones no evitables
		List<RestriccionHorariaItem> restriccionesHorariasNoEvitables = new ArrayList<>(restriccionHorariaThread.getRestriccionesHorariasNoEvitables()) ;

		// Obtenemos las restricciones de sesión por módulo FP en las no evitables
		boolean encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorModuloFp(matrizAsignaciones, asignatura, restriccionHorariaThread, restriccionesHorariasNoEvitables) ;

		// Si no se ha encontrado en las no evitables, se intenta en las evitables
		if (!encontrado)
		{
			// Obtenemos las restricciones de sesión por módulo FP en las evitables
			List<RestriccionHorariaItem> restriccionesHorariasEvitables = new ArrayList<>(restriccionHorariaThread.getRestriccionesHorariasEvitables()) ;

			// Obtenemos las restricciones de sesión por módulo FP en las evitables
			encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorModuloFp(matrizAsignaciones, asignatura, restriccionHorariaThread, restriccionesHorariasEvitables) ;
		}
	}
	
	/**
	 * @param matrizAsignaciones matriz de asignaciones
     * @param numeroCursos numero de cursos
     * @param indiceCursoDia índice curso día
     * @param indiceTramoHorario hora sobre la que verificar
	 * @param profesor profesor
	 * @return true si el profesor ya tiene una clase asignada a esa misma hora
	 */
	private boolean profesorSinSesionEnEstaHora(Asignacion[][] matrizAsignaciones, int numeroCursos, int indiceCursoDia, int indiceTramoHorario, Profesor profesor)
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
    		profesorSinSesionEnEstaHora = matrizAsignaciones[indiceCursoBusqueda][indiceTramoHorario] == null ||
    									 !this.buscarProfesor(matrizAsignaciones[indiceCursoBusqueda][indiceTramoHorario], profesor) ;
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
	 * @param matrizAsignaciones matriz de asignaciones
     * @param sesion sesión
     * @param asignacionSesion asignacion sesion
	 * @return asignacion con la sesión asignada
     * @throws SchoolManagerServerException con un error
     */
	protected Asignacion asignarSesion(Asignacion[][] matrizAsignaciones, SesionBase sesion, IndicesAsignacionSesion indicesAsignacionSesion) throws SchoolManagerServerException
	{
		// Obtenemos la asignación actual
		Asignacion asignacion = matrizAsignaciones[indicesAsignacionSesion.getIndiceCursoDia()][indicesAsignacionSesion.getIndiceTramoHorario()] ;
		
		// Si no hay ninguna asignación, inicializamos la instancia
		if (asignacion == null)
		{
			asignacion = new Asignacion() ;
			matrizAsignaciones[indicesAsignacionSesion.getIndiceCursoDia()][indicesAsignacionSesion.getIndiceTramoHorario()] = asignacion ;
		}
		
		// Si es una asignatura ...
		if (sesion instanceof SesionAsignatura)
		{
			// ... y es optativa, lo indicamos en la asignación
			asignacion.setOptativas(((SesionAsignatura) sesion).getAsignatura().isOptativa()) ;
		}
		
		// Introducimos la sesion en la lista
		asignacion.getListaSesiones().add(sesion) ;
		
		return asignacion ;
	}
}
