package es.iesjandula.reaktor.school_manager_server.generator.core.threads;

import java.util.List;

import es.iesjandula.reaktor.school_manager_server.generator.core.GestorDeSesiones;
import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.utils.CopiaEstructuras;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HorarioThread extends Thread
{
    /** Clase con todos los parámetros necesarios */
    private HorarioThreadParams horarioThreadParams ;
    
    /** Lista de listas sesiones pendientes de asignar donde la columna es el número de restricciones y la fila la sesion */
    private List<List<Sesion>> sesionesPendientes ;
    
	/** Matriz con las asignaciones matutinas */
    private Asignacion[][] matrizAsignacionesMatutinas ;
    
	/** Matriz con las asignaciones vespertinas */
    private Asignacion[][] matrizAsignacionesVespertinas ;

	/** Ultima asignación */
	private UltimaAsignacion ultimaAsignacion ;
    
    /**
     * @param horarioThreadParams Clase con todos los parámetros necesarios
     * @param sesiones Lista de listas sesiones donde la columna es el número de restricciones y la fila la sesion
     * @param matrizAsignacionesMatutinas matriz con las asignaciones matutinas
     * @param matrizAsignacionesVespertinas matriz con las asignaciones vespertinas
     * @param ultimaAsignacion ultima asignación
     */
    public HorarioThread(HorarioThreadParams horarioThreadParams,
    					 List<List<Sesion>> sesiones,
    					 Asignacion[][] matrizAsignacionesMatutinas,
    					 Asignacion[][] matrizAsignacionesVespertinas,
    					 UltimaAsignacion ultimaAsignacion)
    {
    	this.horarioThreadParams 		   = horarioThreadParams ;
    	
    	// Creamos una copia de las sesiones
    	this.sesionesPendientes	 		   = CopiaEstructuras.copiarListaDeSesiones(sesiones) ;
    	
    	// Creamos una copia del estado actual del horario
    	this.matrizAsignacionesMatutinas   = CopiaEstructuras.copiarMatriz(matrizAsignacionesMatutinas) ;
    	this.matrizAsignacionesVespertinas = CopiaEstructuras.copiarMatriz(matrizAsignacionesVespertinas) ;
		
		// Asociamos la ultima asignación
    	this.ultimaAsignacion			   = ultimaAsignacion ;
    }
    
    @Override
    public void run() 
    {    
		UltimaAsignacion nuevaUltimaAsignacion = null ;	
    	try
    	{
			// Generamos el horario
			nuevaUltimaAsignacion = this.generarHorario() ;  
		}
    	catch (SchoolManagerServerException schoolManagerServerException)
    	{
			try
			{
				// Si llegamos aquí es porque la aplicación vio alguna incompatibilidad
				// Por lo que se para este thread y se informa al manejador
				this.horarioThreadParams.getManejadorThreads().gestionarErrorEnAsignacionHoraria(this.matrizAsignacionesMatutinas, 
																								 this.matrizAsignacionesVespertinas,
																								 schoolManagerServerException.getMessage()) ;
			}
			catch (SchoolManagerServerException schoolManagerServerException2)
			{
				// Excepción ya logueada
			}
		}
		catch (Throwable exception)
		{
			String errorString = "EXCEPCIÓN NO ESPERADA CAPTURADA EN HorarioThread: " + exception.getMessage() ;
            log.error(errorString, exception) ;
        }
		finally
		{
			try
			{
				// Indicamos que el Thread ya ha terminado
				this.horarioThreadParams.getManejadorThreads().decrementarNumeroThreadsPendientes() ;

				// Si la nueva última asignación no se asignó nada, comenzamos el proceso de nuevo
				if (nuevaUltimaAsignacion == null)
				{
					this.horarioThreadParams.getManejadorThreads().iniciarProceso();
				}
				else  // Si se asignó algo correctamente ...
				{

					// ... lanzamos nuevos threads para procesar las siguientes sesiones
					this.horarioThreadParams.getManejadorThreads().lanzarNuevosThreads(this.sesionesPendientes,
																						this.matrizAsignacionesMatutinas, 
																						this.matrizAsignacionesVespertinas,
																						nuevaUltimaAsignacion) ;
				}
			}
			catch (SchoolManagerServerException exception)
			{
				// Si sucede una excepción aquí, el sistema no tendrá más remedio que pararse
			}		
		}
    }

	/**
     * Método que va a tratar de asignar una clase al horario actual
	 * @return ultima asignación
	 * @throws SchoolManagerServerException con un error
     */
    private UltimaAsignacion generarHorario() throws SchoolManagerServerException
    {
		// Creamos una nueva instancia de GestorDeSesiones
		GestorDeSesiones gestorDeSesiones = new GestorDeSesiones(this.horarioThreadParams.getAsignaturaService(), this.sesionesPendientes, this.matrizAsignacionesMatutinas, this.matrizAsignacionesVespertinas, this.ultimaAsignacion) ;

		// Cogemos una de las sesiones pendientes de asignar
		Sesion sesion = gestorDeSesiones.obtenerSesionParaAsignar() ;

		int numeroCursos 				  = -1 ;
		int indiceCursoDiaInicial 	 	  = -1 ;

		// Si la asignatura es matutina ...
		boolean esAsignaturaMatutina = sesion.getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getHorarioMatutino() ;
		if (esAsignaturaMatutina)
		{
			// ... obtenemos el número de cursos ...
			numeroCursos = this.horarioThreadParams.getNumeroCursosMatutinos() ;

			// ... obtenemos el índice del curso/día inicial matutino
			indiceCursoDiaInicial = this.horarioThreadParams.getMapCorrelacionadorCursosMatutinos()
															.get(sesion.getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getCursoEtapaGrupoString()) ;
		}
		else
		{
			// ... obtenemos el número de cursos ...
			numeroCursos = this.horarioThreadParams.getNumeroCursosVespertinos() ;

			// ... obtenemos el índice del curso/día inicial vespertino
			indiceCursoDiaInicial = this.horarioThreadParams.getMapCorrelacionadorCursosVespertinos()
															.get(sesion.getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getCursoEtapaGrupoString()) ;
		}

		// Encontramos el hueco para la sesión y devolvemos la última asignación
		return gestorDeSesiones.encontrarHuecoParaSesion(sesion, esAsignaturaMatutina, numeroCursos, indiceCursoDiaInicial) ;					
	}
}

