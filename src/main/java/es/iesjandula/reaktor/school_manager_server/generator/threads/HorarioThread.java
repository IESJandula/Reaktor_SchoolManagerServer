package es.iesjandula.reaktor.school_manager_server.generator.threads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.generator.Horario;
import es.iesjandula.reaktor.school_manager_server.generator.sesiones.asignador.AsignadorSesionesController;
import es.iesjandula.reaktor.school_manager_server.generator.sesiones.selector.SelectorSesionesController;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HorarioThread extends Thread
{
    /** Clase con todos los parámetros necesarios */
    private HorarioThreadParams horarioThreadParams ;
    
    /** Lista de listas sesiones pendientes originales */
    private List<List<SesionBase>> sesionesOriginales ;

	/** Lista de listas sesiones pendientes de asignar */
    private List<List<SesionBase>> sesionesThread ;
    
	/** Matriz con las asignaciones matutinas */
    private Asignacion[][] matrizAsignacionesMatutinas ;
    
	/** Matriz con las asignaciones vespertinas */
    private Asignacion[][] matrizAsignacionesVespertinas ;

	/** SelectorSesionesController */
	private SelectorSesionesController selectorSesionesController ;

	/** AsignadorSesionesController */
	private AsignadorSesionesController asignadorSesionesController ;

	/** Bandera para indicar si se debe detener el hilo */
	private boolean detenerHilo ;
    
    /**
     * @param horarioThreadParams Clase con todos los parámetros necesarios
     * @param sesionesOriginales Lista de listas sesiones originales
     */
    public HorarioThread(HorarioThreadParams horarioThreadParams,
    					 List<List<SesionBase>> sesionesOriginales)
    {
    	this.horarioThreadParams = horarioThreadParams ;
    	this.sesionesOriginales  = sesionesOriginales ;

		// Creamos una nueva instancia de SelectorSesionesController y asignadorSesionesController
		this.selectorSesionesController  = new SelectorSesionesController(this.horarioThreadParams.getAsignaturaService()) ;
		this.asignadorSesionesController = new AsignadorSesionesController(this.horarioThreadParams.getAsignaturaService()) ;

		// Inicializamos la bandera para indicar si se debe detener el hilo
		this.detenerHilo = false ;
    }

	/**
	 * Método que forza la detención del hilo del generador
	 */
	public void forzarDetencion()
	{
		this.detenerHilo = true ;
	}
    
    @Override
    public void run() 
    {    
		// Comenzamos el proceso de nuevo
		this.comenzarProceso() ;
	}

	/**
	 * Método que comienza el proceso
	 */
	private void comenzarProceso()
	{
		while (!this.detenerHilo)
		{
			this.comenzarProcesoInternal() ;
		}
	}

	/**
	 * Método que comienza el proceso
	 */
	private void comenzarProcesoInternal()
	{
		GeneradorInstancia generadorInstancia = null;

		try
		{
			// Creamos una instancia de GeneradorInstancia
			generadorInstancia = this.horarioThreadParams.getGeneradorService().crearGeneradorInstancia() ;

			// Configuramos las estructuras de datos
			this.configurarEstructurasDeDatos() ;

			UltimaAsignacion ultimaAsignacion = null ;

			// Este bucle es para asegurarnos que se asignen todas las sesiones
			while (!this.todasLasSesionesAsignadas())
			{    		
				// Asignamos una sesión y obtenemos la última asignación
				UltimaAsignacion nuevaUltimaAsignacion = this.asignarSesion(ultimaAsignacion) ;	

				// Asignamos la nueva última asignación
				ultimaAsignacion = nuevaUltimaAsignacion ;
			}

			// Si se han asignado todas las sesiones ...
			if (this.todasLasSesionesAsignadas())
			{
				// ... agregamos la solución encontrada
				this.agregarHorarioSolucion(generadorInstancia) ;
			}
		}
		catch (SchoolManagerServerException schoolManagerServerException)
    	{
			try
			{
				if (log.isDebugEnabled())
				{
					Horario horario = new Horario(this.matrizAsignacionesMatutinas, this.matrizAsignacionesVespertinas) ;
					log.debug("Horario actual: \n" + horario) ;
				}

				// Borramos la instancia del generador relacionada
				this.horarioThreadParams.getGeneradorService().eliminarGeneradorInstancia(generadorInstancia) ;

				// Si sucede una excepción aquí, el sistema no tendrá más remedio que comenzar de nuevo
				this.comenzarProceso() ;
			}
			catch (SchoolManagerServerException schoolManagerServerException2)
			{
				// Excepción ya logueada
			}
			catch (Throwable exception)
			{
				String errorString = "EXCEPCIÓN NO ESPERADA CAPTURADA EN HorarioThread: " + exception.getMessage() ;
				log.error(errorString, exception) ;
			}
		}
		catch (Throwable exception)
		{
			String errorString = "EXCEPCIÓN NO ESPERADA CAPTURADA EN HorarioThread: " + exception.getMessage() ;
            log.error(errorString, exception) ;
        }
	}
	
	/**
	 * Método que configura las estructuras de datos
	 */
	private void configurarEstructurasDeDatos()
	{
        // Obtenemos el número de cursos matutinos que hay
        int numeroCursosMatutinos = this.horarioThreadParams.getMapCorrelacionadorCursosMatutinos().size() ;

        // Creamos las matrices de sesiones vacía, donde cada columna representa un curso en un día
    	this.matrizAsignacionesMatutinas = null ;
		if (numeroCursosMatutinos > 0)
		{
			this.matrizAsignacionesMatutinas = new Asignacion[numeroCursosMatutinos * Constants.NUMERO_DIAS_SEMANA]
														     [Constants.NUMERO_TRAMOS_HORARIOS] ;

		}

		// Obtenemos el número de cursos vespertinos que hay
    	int numeroCursosVespertinos = this.horarioThreadParams.getMapCorrelacionadorCursosVespertinos().size() ;

    	this.matrizAsignacionesVespertinas = null ;
		if (numeroCursosVespertinos > 0)
		{
			this.matrizAsignacionesVespertinas = new Asignacion[numeroCursosVespertinos * Constants.NUMERO_DIAS_SEMANA]
														       [Constants.NUMERO_TRAMOS_HORARIOS] ;

		}

        // Clonamos las listas de sesiones originales en las listas de sesiones del thread
        this.clonarListaDeSesionesOriginalesEnSesionesThread() ;

        // Visitamos todas las sesiones e inicializamos la restricción horaria thread
        for (List<SesionBase> listaSesiones : this.sesionesThread)
        {
            for (SesionBase sesion : listaSesiones)
            {
                sesion.inicializarRestriccionHorariaThread() ;
            }
        }
	}

	/**
	 * Clona la lista de sesiones originales en la lista de sesiones del thread
	 */
	public void clonarListaDeSesionesOriginalesEnSesionesThread()
	{
        this.sesionesThread = new ArrayList<>() ;

		for (List<SesionBase> sublista : this.sesionesOriginales)
		{
			// Creamos una nueva sublista para cada sublista original
			List<SesionBase> copiaSublista = new ArrayList<SesionBase>(sublista) ;
			
			// Añadimos la copia de la sublista a la nueva lista
			this.sesionesThread.add(copiaSublista) ;
		}
	}

	/**
     * Valida si están todas las sesiones asignadas
     * @return true si están todas las sesiones asignadas
     */
    private boolean todasLasSesionesAsignadas()
    {
    	boolean outcome = true ;

		Iterator<List<SesionBase>> iterator = this.sesionesThread.iterator() ;
		while (iterator.hasNext() && outcome)
		{
			List<SesionBase> sublista = iterator.next() ;
			
			outcome = outcome && sublista.isEmpty() ;
		}

		int cont = 0 ;
		Iterator<List<SesionBase>> iterator2 = this.sesionesThread.iterator() ;
		while (iterator2.hasNext())
		{
			List<SesionBase> sublista = iterator2.next() ;
			cont = cont + sublista.size() ;
		}

		log.debug("Sesiones pendientes de asignar: {}", cont) ;
    	
    	return outcome ; 
    }

	/**
     * Método que va a tratar de asignar una sesión
	 * @param ultimaAsignacion última asignación
	 * @return ultima asignación
	 * @throws SchoolManagerServerException con un error
     */
    private UltimaAsignacion asignarSesion(UltimaAsignacion ultimaAsignacion) throws SchoolManagerServerException
    {
		// Inicializamos las matriz de asignación
		Asignacion[][] matrizAsignacion = null ;

		// Cogemos una de las sesiones pendientes de asignar
		SesionBase sesion = this.selectorSesionesController.obtenerSesionParaAsignar(this.sesionesThread,
																		             this.matrizAsignacionesMatutinas,
																		             this.matrizAsignacionesVespertinas,
																			         ultimaAsignacion) ;

		int numeroCursos 		  = -1 ;
		int indiceCursoDiaInicial = -1 ;

		// Si la sesión es matutina ...
		if (sesion.isTipoHorarioMatutino())
		{
			// ... obtenemos el número de cursos ...
			numeroCursos = this.horarioThreadParams.getMapCorrelacionadorCursosMatutinos().size() ;

			// ... obtenemos el índice del curso/día inicial matutino
			indiceCursoDiaInicial = this.horarioThreadParams.getMapCorrelacionadorCursosMatutinos().get(sesion.getCursoEtapaGrupoString()) ;

			// Cogemos las matrices de asignación
			matrizAsignacion = this.matrizAsignacionesMatutinas ;
		}
		else
		{
			// ... obtenemos el número de cursos ...
			numeroCursos = this.horarioThreadParams.getMapCorrelacionadorCursosVespertinos().size() ;

			// ... obtenemos el índice del curso/día inicial vespertino
			indiceCursoDiaInicial = this.horarioThreadParams.getMapCorrelacionadorCursosVespertinos().get(sesion.getCursoEtapaGrupoString()) ;

			// Cogemos las matrices de asignación
			matrizAsignacion = this.matrizAsignacionesVespertinas ;
		}

		// Asignamos la sesión y devolvemos la última asignación
		return this.asignadorSesionesController.asignarSesion(matrizAsignacion, sesion, numeroCursos, indiceCursoDiaInicial) ;					
	}

	/**
     * Método para agregar una solución a la lista
     * @param generadorInstancia generador instancia
     * @return true si la solución supera el umbral, false en caso contrario
	 * @throws SchoolManagerServerException con un error
     */
    private boolean agregarHorarioSolucion(GeneradorInstancia generadorInstancia) throws SchoolManagerServerException
    {
    	// Creamos una instancia de Horario
		Horario horario = new Horario(this.matrizAsignacionesMatutinas, this.matrizAsignacionesVespertinas) ;

        // Guardamos el horario en la base de datos
        this.horarioThreadParams.getGeneradorService().guardarHorariosEnGeneradorSesionAsignada(generadorInstancia, horario) ;
		
    	// Calculamos las puntuación de esta solución
        int puntuacionObtenida = this.horarioThreadParams.getGeneradorService().calcularPuntuacion(generadorInstancia) ;

		// Verificamos si la solución supera el umbral
		boolean solucionSuperaUmbral = puntuacionObtenida > this.horarioThreadParams.getUmbralMinimoSolucion() ;
   	
        // Verificamos si la solución cumple unos mínimos
        if (!solucionSuperaUmbral)
        {
            // Logueamos
            log.info("Horario solución no supera la puntuación umbral: " + puntuacionObtenida + " < {} ó " + puntuacionObtenida + " < {}", 
            		 this.horarioThreadParams.getUmbralMinimoSolucion(), puntuacionObtenida) ;

            // Borramos de la tabla de generador instancia
            this.horarioThreadParams.getGeneradorService().eliminarGeneradorInstancia(generadorInstancia) ;
        }
        else
        {
        	// Logueamos
            log.info("Horario solución supera la puntuación umbral: " + puntuacionObtenida + " > {} ó " + puntuacionObtenida + " > {}", 
            		 this.horarioThreadParams.getUmbralMinimoSolucion(), puntuacionObtenida) ;

            // Actualizamos el GeneradorInstancia y el Generador en BBDD
            this.horarioThreadParams.getGeneradorService().actualizarGeneradorYgeneradorInstancia(generadorInstancia, Constants.MENSAJE_SOLUCION_ENCONTRADA, puntuacionObtenida) ;

			// Paramos el hilo
			this.detenerHilo = true ;
        }

        return solucionSuperaUmbral ;
    }
}

