package es.iesjandula.reaktor.school_manager_server.generator.sesiones.asignador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales;
import es.iesjandula.reaktor.school_manager_server.models.PreferenciasHorariasProfesor;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHorariaItem;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.services.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.generator.threads.UltimaAsignacion;
import es.iesjandula.reaktor.school_manager_server.generator.sesiones.SesionesUtils;
import es.iesjandula.reaktor.school_manager_server.generator.threads.IndicesAsignacionSesion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsignadorSesionesController
{
	/** Matriz de asignaciones */
	private Asignacion[][] matrizAsignaciones ;

	/** Asignador de sesiones de asignaturas */
	private AsignadorSesionesAsignaturas asignadorSesionesAsignaturas ;

	/**
	 * Constructor de la clase
	 * 
	 * @param asignaturaService asignatura service
	 * @param matrizAsignaciones matriz de asignaciones
	 */
	public AsignadorSesionesController(AsignaturaService asignaturaService, Asignacion[][] matrizAsignaciones)
	{
		this.matrizAsignaciones 		  = matrizAsignaciones ;
		this.asignadorSesionesAsignaturas = new AsignadorSesionesAsignaturas(asignaturaService, matrizAsignaciones) ;
	}
				
	/**
     * @param sesion sesión
     * @param numeroCursos número de cursos
	 * @param indiceCursoDiaInicial índice del curso y día inicial
	 * @return ultima asignacion
     * @throws SchoolManagerServerException con un error
     */
    public UltimaAsignacion asignarSesion(SesionBase sesion, int numeroCursos, int indiceCursoDiaInicial) throws SchoolManagerServerException
    {
        // Obtener restricción horaria de esa sesion
		RestriccionHoraria restriccionHoraria = this.obtenerRestriccionHorariaDeSesion(sesion, numeroCursos, indiceCursoDiaInicial) ;

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
	 * @param numeroCursos número de cursos
     * @param indiceCursoDiaInicial índice del curso y día inicial
     * @return una nueva instancia que restringe los horarios (días y horas)
     * @throws SchoolManagerServerException con un error
     */
    private RestriccionHoraria obtenerRestriccionHorariaDeSesion(SesionBase sesion,
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

			// Obtenemos las restricciones relacionadas con tratar de evitar que se coja de primera o última hora
			this.obtenerRestriccionHorariaDeSesionTratarEvitarClasePrimeraUltimaHora(sesion, builder) ;

			// Obtenemos las restricciones relacionadas con los días y tramos que le gustaría evitar al profesor
			this.obtenerRestriccionHorariaDeSesionPorPreferenciasHorariasProfesores(sesion, builder) ;

			// Obtenemos las restricciones relacionadas con el día y tramo horario
			this.obtenerRestriccionHorariaDeSesionPorDiaTramo(sesion, numeroCursos, builder) ;

			// Ahora en la conciliación de los profesores siempre que la asignatura sea matutina
			if (sesion.isTipoHorarioMatutino())
			{
				this.obtenerRestriccionHorariaDeSesionPorConciliacion(sesion, builder) ;
			}

			// Ahora vemos si la sesión es de tipo asignatura
			boolean esAsignatura = sesion instanceof SesionAsignatura ;
			if (esAsignatura)
			{
				SesionAsignatura sesionAsignatura = (SesionAsignatura) sesion ;

				// Ahora vemos si hay que restringir por bloques de asignaturas (optativas) si es que es una optativa
				if (sesionAsignatura.getAsignatura().isOptativa())
				{
					this.obtenerRestriccionHorariaDeSesionPorOptativas(sesionAsignatura.getAsignatura(), builder) ;
				}

				// Si no es ESO ni BACHILLERATO, la restricción será a nivel de FP
				if (!sesionAsignatura.isEsEsoBachillerato())
				{
					this.obtenerRestriccionHorariaDeSesionPorModuloFp(sesionAsignatura.getAsignatura(), builder) ;
				}
			}
			else // Entonces es una reducción
			{
				// No hacemos nada por ahora
			}
			// Hacemos un build para obtener la instancia de Restriccion Horaria
			restriccionHoraria = builder.build() ;
		}

		return restriccionHoraria ;
	}
	
	/**
	 * @param sesion sesion
	 * @param builder restriccion horaria builder
	 */
	private void obtenerRestriccionHorariaDeSesionTratarEvitarClasePrimeraUltimaHora(SesionBase sesion, RestriccionHoraria.Builder builder)
	{
		// Si el profesor prefiere no tener clase a primera hora, se intenta que no se coja de esta
		if (sesion.getProfesor().getObservacionesAdicionales().getSinClasePrimeraHora())
		{
			builder = builder.tratarEvitarClasePrimeraHora() ;
		}
		// Si el profesor prefiere no tener clase a última hora, se intenta que no se coja de esta
		else if (!sesion.getProfesor().getObservacionesAdicionales().getSinClasePrimeraHora())
		{
			builder = builder.tratarEvitarClaseUltimaHora() ;
		}
	}

	/**
	 * @param sesion sesion
	 * @param builder restriccion horaria builder
	 */
	private void obtenerRestriccionHorariaDeSesionPorPreferenciasHorariasProfesores(SesionBase sesion, RestriccionHoraria.Builder builder)
	{
		// Obtenemos las preferencias horarias del profesor
		List<PreferenciasHorariasProfesor> preferenciasHorariasProfesores = sesion.getProfesor().getPreferenciasHorariasProfesor() ;

		// Si hay preferencias horarias, se intenta que no se coja de esta
		if (preferenciasHorariasProfesores != null && preferenciasHorariasProfesores.size() > 0)
		{
			// Si hay preferencias horarias, se intenta que no se coja de esta
			builder = builder.tratarEvitarClaseTramoHorario(preferenciasHorariasProfesores) ;
		}
	}
	
	/**
	 * @param sesion sesion
	 * @param builder restriccion horaria builder
	 */
	private void obtenerRestriccionHorariaDeSesionPorDiaTramo(SesionBase sesion, int numeroCursos, RestriccionHoraria.Builder builder)
	{
		// Introducimos en una misma lista las restricciones no evitables y las evitables
		List<RestriccionHorariaItem> restriccionesHorarias = new ArrayList<>() ;
		restriccionesHorarias.addAll(builder.getRestriccionesHorariasNoEvitables()) ;
		restriccionesHorarias.addAll(builder.getRestriccionesHorariasEvitables()) ;

		// Iteramos y quitamos todos aquellos items incompatibles
		Iterator<RestriccionHorariaItem> iterator = restriccionesHorarias.iterator() ;
		while (iterator.hasNext())
		{
			RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

			// Vemos si se cumple el número máximo de ocurrencias por día y si el profesor no tiene sesión en esta hora ya asignada
			boolean restriccionIncompatible = 
			  !SesionesUtils.sesionSinMasXOcurrenciasElMismoDia(this.matrizAsignaciones, restriccionHorariaItem.getIndiceDia(), sesion) ||
			  !this.profesorSinSesionEnEstaHora(numeroCursos, restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario(), sesion.getProfesor()) ;

			// Si todavía es compatible la restricción ...
			if (!restriccionIncompatible)
			{
				if (sesion instanceof SesionAsignatura)
				{
					restriccionIncompatible = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorDiaTramo(((SesionAsignatura) sesion).getAsignatura(), restriccionHorariaItem) ;
				}
			}

			if (restriccionIncompatible)
			{
				builder = builder.eliminarRestriccionHorariaItem(restriccionHorariaItem) ;
			}
		}
	}
    
    /**
     * @param sesion sesion
     * @param builder restriccion horaria builder
     */
	private void obtenerRestriccionHorariaDeSesionPorConciliacion(SesionBase sesion, RestriccionHoraria.Builder builder)
	{
    	if (sesion.getProfesor().getObservacionesAdicionales().getConciliacion())
		{
			if (sesion.getProfesor().getObservacionesAdicionales().getSinClasePrimeraHora())
			{
				builder = builder.sinClasePrimeraHora() ;
			}
			else
			{
				builder = builder.sinClaseUltimaHora() ;
			}
		}
	}

	/**
	 * @param asignatura asignatura
	 * @param builder restriccion horaria builder
	 */
	private void obtenerRestriccionHorariaDeSesionPorOptativas(Asignatura asignatura, RestriccionHoraria.Builder builder)
	{
		// Creamos un nuevo ArrayList de RestriccionHorariaItem sobre las restricciones no evitables
		List<RestriccionHorariaItem> restriccionesHorariasNoEvitables = new ArrayList<>(builder.getRestriccionesHorariasNoEvitables()) ;

		// Obtenemos las restricciones de sesión por optativas en las no evitables
		boolean encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorOptativas(asignatura, builder, restriccionesHorariasNoEvitables) ;

		// Obtenemos las restricciones de sesión por optativas en las evitables
		if (!encontrado)
		{
			// Creamos un nuevo ArrayList de RestriccionHorariaItem sobre las restricciones evitables
			List<RestriccionHorariaItem> restriccionesHorariasEvitables = new ArrayList<>(builder.getRestriccionesHorariasEvitables()) ;

			// Obtenemos las restricciones de sesión por optativas en las evitables
			encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorOptativas(asignatura, builder, restriccionesHorariasEvitables) ;
		}
	}

	/**
	 * @param asignatura asignatura
	 * @param builder restriccion horaria builder
	 */
	private void obtenerRestriccionHorariaDeSesionPorModuloFp(Asignatura asignatura, RestriccionHoraria.Builder builder)
	{
		// Creamos un nuevo ArrayList de RestriccionHorariaItem sobre las restricciones no evitables
		List<RestriccionHorariaItem> restriccionesHorariasNoEvitables = new ArrayList<>(builder.getRestriccionesHorariasNoEvitables()) ;

		// Obtenemos las restricciones de sesión por módulo FP en las no evitables
		boolean encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorModuloFp(asignatura, builder, restriccionesHorariasNoEvitables) ;

		// Si no se ha encontrado en las no evitables, se intenta en las evitables
		if (!encontrado)
		{
			// Obtenemos las restricciones de sesión por módulo FP en las evitables
			List<RestriccionHorariaItem> restriccionesHorariasEvitables = new ArrayList<>(builder.getRestriccionesHorariasEvitables()) ;

			// Obtenemos las restricciones de sesión por módulo FP en las evitables
			encontrado = this.asignadorSesionesAsignaturas.obtenerRestriccionHorariaDeSesionPorModuloFp(asignatura, builder, restriccionesHorariasEvitables) ;
		}
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
     * @param sesion sesión
     * @param asignacionSesion asignacion sesion
	 * @return asignacion con la sesión asignada
     * @throws SchoolManagerServerException con un error
     */
	protected Asignacion asignarSesion(SesionBase sesion, IndicesAsignacionSesion indicesAsignacionSesion) throws SchoolManagerServerException
	{
		// Obtenemos la asignación actual
		Asignacion asignacion = this.matrizAsignaciones[indicesAsignacionSesion.getIndiceCursoDia()][indicesAsignacionSesion.getIndiceTramoHorario()] ;
		
		// Si no hay ninguna asignación, inicializamos la instancia
		if (asignacion == null)
		{
			asignacion = new Asignacion() ;
			this.matrizAsignaciones[indicesAsignacionSesion.getIndiceCursoDia()][indicesAsignacionSesion.getIndiceTramoHorario()] = asignacion ;
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
