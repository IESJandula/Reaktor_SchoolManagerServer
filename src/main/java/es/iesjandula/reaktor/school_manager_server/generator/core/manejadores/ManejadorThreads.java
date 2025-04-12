package es.iesjandula.reaktor.school_manager_server.generator.core.manejadores;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import es.iesjandula.reaktor.school_manager_server.generator.core.Horario;
import es.iesjandula.reaktor.school_manager_server.generator.core.HorarioParams;
import es.iesjandula.reaktor.school_manager_server.generator.core.threads.HorarioThread;
import es.iesjandula.reaktor.school_manager_server.generator.core.threads.HorarioThreadParams;
import es.iesjandula.reaktor.school_manager_server.generator.core.threads.UltimaAsignacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManejadorThreads
{
	/** Parámetros útiles para lanzar el manejador de threads */
	private ManejadorThreadsParams manejadorThreadsParams ;
	
	/** Sesiones pendientes de asignar */
	private List<List<Sesion>> sesionesPendientes ;
	
    /** Thread pendientes */
    private AtomicInteger threadsPendientesFinalizacion ;
    
    /** ExecutorService para gestionar el pool de threads */
    private ExecutorService threadPool ;

    /**
     * Constructor
     * 
     * @param manejadorThreadsParams parametros para el manejador de threads
     * @param sesionesPendientes sesiones pendientes de asignar
     */
    public ManejadorThreads(ManejadorThreadsParams manejadorThreadsParams, List<List<Sesion>> sesionesPendientes)
    {
    	this.manejadorThreadsParams 			  = manejadorThreadsParams ;
    	this.sesionesPendientes					  = sesionesPendientes ;
    	
        // Creamos el ExecutorService con un pool de hilos del tamaño especificado en Constants
        this.threadPool 				  		  = Executors.newFixedThreadPool(manejadorThreadsParams.getPoolSize()) ;
        
        this.threadsPendientesFinalizacion   	  = new AtomicInteger(0) ;
    }
    
    /**
     * Método para iniciar el proceso de generación de horarios
     * @throws SchoolManagerServerException con un error
     */
    public void iniciarProceso() throws SchoolManagerServerException
    {
    	// Obtenemos el número de cursos que hay
    	int numeroCursosMatutinos   = this.manejadorThreadsParams.getNumeroCursosMatutinos() ;
    	int numeroCursosVespertinos = this.manejadorThreadsParams.getNumeroCursosVespertinos() ;
    	
        // Creamos las matrices de sesiones vacía, donde cada columna representa un curso en un día
    	Asignacion[][] asignacionesInicialesMatutinas   = new Asignacion[numeroCursosMatutinos * Constants.NUMERO_DIAS_SEMANA]
    														 			[Constants.NUMERO_TRAMOS_HORARIOS] ;
    	Asignacion[][] asignacionesInicialesVespertinas = new Asignacion[numeroCursosVespertinos * Constants.NUMERO_DIAS_SEMANA]
    														 			[Constants.NUMERO_TRAMOS_HORARIOS] ;
    	
    	// Lanzamos nuevos threads para procesar la siguiente clase
        this.lanzarNuevosThreads(this.sesionesPendientes, asignacionesInicialesMatutinas, asignacionesInicialesVespertinas, null) ;
    }
	
	/**
	 * Método para lanzar nuevos Threads tras encontrar un hueco correcto en el horario
	 * 
	 *  @param sesionesPendientes sesiones pendientes de asignar en el horario
	 *  @param matrizAsignacionesMatutinas matriz con las asignaciones matutinas
	 *  @param matrizAsignacionesVespertinas matriz con las asignaciones vespertinas
	 *  @param ultimaAsignacion ultima asignación
	 * @throws SchoolManagerServerException con un error
	 */
    public void lanzarNuevosThreads(List<List<Sesion>> sesionesPendientes,
    								Asignacion[][] matrizAsignacionesMatutinas,
    								Asignacion[][] matrizAsignacionesVespertinas,
									UltimaAsignacion ultimaAsignacion) throws SchoolManagerServerException
    {
    	// Verificamos si la lista de sesiones está vacía
    	// Si se cumple es porque ya hemos asignado todas las sesiones
    	if (this.todasLasSesionesAsignadas(sesionesPendientes))
    	{
    		// Creamos la nueva instancia de horario
    		Horario horario = this.crearInstanciaHorario(matrizAsignacionesMatutinas, matrizAsignacionesVespertinas) ;
    		
    		// Añadimos a las soluciones esta nueva encontrada
    		this.manejadorThreadsParams.getManejadorResultados().agregarHorarioSolucion(horario) ;
    	}
    	else if (this.manejadorThreadsParams.getManejadorResultados().solucionEncontrada() == null)
    	{
    		// Lanzamos threads para seguir buscando soluciones
    		this.lanzarNuevosThreadsInternal(sesionesPendientes, matrizAsignacionesMatutinas, matrizAsignacionesVespertinas, ultimaAsignacion) ;
    	}
	}

    /**
     * Valida si están todas las sesiones asignadas
	 * @param sesiones sesiones pendientes de asignar en el horario
     * @return true si están todas las sesiones asignadas
     */
    private boolean todasLasSesionesAsignadas(List<List<Sesion>> sesiones)
    {
    	boolean outcome = true ;

		synchronized(sesiones)
		{
			Iterator<List<Sesion>> iterator = sesiones.iterator() ;
			while (iterator.hasNext() && outcome)
    		{
				List<Sesion> sublista = iterator.next() ;
				
				outcome = outcome && sublista.isEmpty() ;
			}

			int cont = 0 ;
			Iterator<List<Sesion>> iterator2 = sesiones.iterator() ;
			while (iterator2.hasNext())
			{
				List<Sesion> sublista = iterator2.next() ;
				cont = cont + sublista.size() ;
			}

			log.info("Sesiones pendientes de asignar: {}", cont) ;
    	}
    	
    	return outcome ; 
    }
    
	/**
	 * @param sesionesPendientes sesiones pendientes para asignar
	 * @param matrizAsignacionesMatutinas matriz con las asignaciones matutinas
	 * @param matrizAsignacionesVespertinas matriz con las asignaciones vespertinas
	 * @param ultimaAsignacion ultima asignación
	 */
	private void lanzarNuevosThreadsInternal(List<List<Sesion>> sesionesPendientes,
											 Asignacion[][] matrizAsignacionesMatutinas,
											 Asignacion[][] matrizAsignacionesVespertinas,
											 UltimaAsignacion ultimaAsignacion)
	{
		// Creamos una nueva instancia de HorarioThreadParams
		HorarioThreadParams horarioThreadParams = 
			new HorarioThreadParams.Builder()
								   .setNumeroCursosMatutinos(this.manejadorThreadsParams.getNumeroCursosMatutinos())
								   .setNumeroCursosVespertinos(this.manejadorThreadsParams.getNumeroCursosVespertinos())
								   .setMapCorrelacionadorCursosMatutinos(this.manejadorThreadsParams.getMapCorrelacionadorCursosMatutinos())
								   .setMapCorrelacionadorCursosVespertinos(this.manejadorThreadsParams.getMapCorrelacionadorCursosVespertinos())
								   .setManejadorThreads(this)
								   .build() ;
		
		// Lanzamos varios threads
		for (int i=0 ; i < this.manejadorThreadsParams.getNumeroThreadPorIteracion() ; i++)
		{
		    // Creamos el hilo raíz para iniciar el proceso
		    HorarioThread horarioThread = new HorarioThread(horarioThreadParams, sesionesPendientes, matrizAsignacionesMatutinas, matrizAsignacionesVespertinas, ultimaAsignacion) ;
		    
		    // Submiteamos el hilo raiz
		    this.threadPool.submit(horarioThread) ;
		    
			// Incrementamos y obtenemos el valor actual
			int numeroActualThreads = this.threadsPendientesFinalizacion.incrementAndGet() ;
			
			// Logueamos
			log.debug("Incrementado el número de Threads. Actualmente hay {} Threads lanzados", numeroActualThreads) ;
		}
	}
    
	/**
	 * Decrementamos el número de Threads pendientes y paramos el ThreadPool 
	 * en caso de que ya no haya más threads que ejecutar
	 * @throws SchoolManagerServerException con un error
	 */
	public void decrementarNumeroThreadsPendientes() throws SchoolManagerServerException
	{
		// Decrementamos y obtenemos el valor actual
		int numeroActualThreads = this.threadsPendientesFinalizacion.decrementAndGet() ;
		
		// Logueamos
		log.info("Decrementado el número de Threads. Actualmente hay {} Threads lanzados", numeroActualThreads) ;
		
		// Si llegamos a 0, hacemos shutdown del Pool de Threads
		if (numeroActualThreads <= 0)
		{
			// Vemos si hemos obtenido alguna solución
			Horario horario = this.manejadorThreadsParams.getManejadorResultados().solucionEncontrada() ;
			
			// Si se ha encontrado, preguntamos si finalizar
			if (horario != null)
			{
				// En caso de que el usuario acepte finalizar, finalizamos el pool
				this.finalizarThreadPool() ;				
			}
			else
			{
				// Logueamos
				log.info("No se ha encontrado ninguna solución. Se reiniciará el proceso") ;

				// Como no hemos encontrado solución, iniciamos de nuevo el proceso
				this.iniciarProceso() ;
			}
		}
	}
	
	/**
	 * @param matrizAsignacionesMatutinas matriz con las asignaciones matutinas
	 * @param matrizAsignacionesVespertinas matriz con las asignaciones vespertinas
	 * @param mensajeError mensaje de error
	 * @throws SchoolManagerServerException con un error
	 */
	public void gestionarErrorEnAsignacionHoraria(Asignacion[][] matrizAsignacionesMatutinas,
												  Asignacion[][] matrizAsignacionesVespertinas,
												  String mensajeError) throws SchoolManagerServerException
	{
		// Creamos la nueva instancia de horario
		Horario horario = this.crearInstanciaHorario(matrizAsignacionesMatutinas, matrizAsignacionesVespertinas) ;
		
		// Avisamos al manejador de resultados que guarde este horario de error
		this.manejadorThreadsParams.getManejadorResultados().agregarHorarioError(horario, mensajeError) ;
	}
	
    /**
     * @param matrizAsignacionesMatutinas matriz con las asignaciones matutinas
     * @param matrizAsignacionesVespertinas matriz con las asignaciones vespertinas
     * @return una nueva instancia de horario
     */
	private Horario crearInstanciaHorario(Asignacion[][] matrizAsignacionesMatutinas, Asignacion[][] matrizAsignacionesVespertinas)
	{
		HorarioParams horarioParams = 
			new HorarioParams.Builder()
				 .setNumeroCursosMatutinos(this.manejadorThreadsParams.getNumeroCursosMatutinos())
				 .setNumeroCursosVespertinos(this.manejadorThreadsParams.getNumeroCursosVespertinos())
			     .setFactorNumeroSesionesInsertadas(this.manejadorThreadsParams.getFactorNumeroSesionesInsertadas())
			     .setFactorSesionesConsecutivasProfesor(this.manejadorThreadsParams.getFactorSesionesConsecutivasProfesor())
			     .setFactorSesionesConsecutivasProfesorMatVes(this.manejadorThreadsParams.getFactorSesionesConsecutivasProfesorMatVes())
			     .setMatrizAsignacionesMatutinas(matrizAsignacionesMatutinas)
				 .setMatrizAsignacionesVespertinas(matrizAsignacionesVespertinas)
			     .build() ;
		
		return new Horario(horarioParams) ;
	}
	
	/**
	 * Método que sirve para finalizar el ThreadPool
	 */
    private void finalizarThreadPool()
    {
    	// Logueamos
    	log.info("Finalizando el ThreadPool" ) ;
    	
    	// Tratamos de parar el ThreadPool
    	this.threadPool.shutdown() ;
    }
}
