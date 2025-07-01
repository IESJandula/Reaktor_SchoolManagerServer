package es.iesjandula.reaktor.school_manager_server.generator.core.manejadores;

import java.util.concurrent.CopyOnWriteArrayList;

import es.iesjandula.reaktor.school_manager_server.generator.core.Horario;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Hago uso de la clase CopyOnWriteArrayList debido a las siguientes ventajas:
 * - Concurrente y Segura: permite añadir elementos desde múltiples hilos sin necesidad de sincronización manual.
 * - Acceso por Índice: a diferencia de ConcurrentLinkedQueue, esta estructura te permite acceder a los elementos 
 * 						mediante índices, lo que es ideal si necesitas saber en qué posición se encuentra la solución óptima.
 */
@Slf4j
@Data
public class ManejadorResultados
{
	/** Parámetros útiles para lanzar el manejador de resultados */
	private ManejadorResultadosParams manejadorResultadosParams ;
	
    /** Lista concurrente para almacenar las soluciones generadas por los hilos */
    private CopyOnWriteArrayList<Horario> horariosSoluciones ;
    
    /** Almacena la puntuación de la solución con mayor puntuación */
    private int horarioSolucionMayorPuntuacion ;
    
    /** Almacena el índice de la solución con mayor puntuación */
    private int horarioSolucionMayorPuntuacionIndice ;
    
    /** Lista concurrente para almacenar los horarios con errores generados por los hilos */
    private CopyOnWriteArrayList<Horario> horariosError ;
    
    /** Almacena la puntuación del horario con error con mayor puntuación */
    private int horarioErrorMayorPuntuacion ;
    
    /** Almacena el índice del horario con error con mayor puntuación */
    private int horarioErrorMayorPuntuacionIndice ;
    
    /**
     * Constructor
     * 
     * @param manejadorResultadosParams parametros para el manejador de resultados
     */
    public ManejadorResultados(ManejadorResultadosParams manejadorResultadosParams)
    {
    	this.manejadorResultadosParams 			  = manejadorResultadosParams ;
    	
        this.horariosSoluciones   				  = new CopyOnWriteArrayList<Horario>() ;
        this.horarioSolucionMayorPuntuacion 	  = -1 ;
        this.horarioSolucionMayorPuntuacionIndice = -1 ;
        
        this.horariosError						  = new CopyOnWriteArrayList<Horario>() ;
        this.horarioErrorMayorPuntuacion 	  	  = -1 ;
        this.horarioErrorMayorPuntuacionIndice 	  = -1 ;
    }
    
	/**
     * Método para agregar una solución a la lista
     * @param generadorInstancia generador instancia
     * @param horario solución encontrada
     * @return true si la solución supera el umbral, false en caso contrario
	 * @throws SchoolManagerServerException con un error
     */
    public boolean agregarHorarioSolucion(GeneradorInstancia generadorInstancia, Horario horario) throws SchoolManagerServerException
    {
    	// Si llegamos aquí es porque se encontró un horario definitivo,  
		// por lo que lo informamos al manejador de soluciones
		
    	// Calculamos las puntuación de esta solución
        int puntuacionObtenida = horario.calcularPuntuacion() ;

        // Vemos si la solución es la mejor hasta el momento
        boolean solucionSuperaUmbral = puntuacionObtenida > this.manejadorResultadosParams.getUmbralMinimoSolucion() &&
                                       this.horarioSolucionMayorPuntuacion < puntuacionObtenida ;
    	
        // Verificamos si la solución cumple unos mínimos
        if (!solucionSuperaUmbral)
        {
            // Logueamos
            log.info("Horario solución no supera la puntuación umbral: " + puntuacionObtenida + " < {} ó " + puntuacionObtenida + " < {}", 
            		 this.manejadorResultadosParams.getUmbralMinimoSolucion(), this.horarioSolucionMayorPuntuacion) ;

            // Borramos de la tabla de generador instancia
            this.manejadorResultadosParams.getGeneradorService().eliminarGeneradorInstancia(generadorInstancia) ;
        }
        else
        {
        	// Logueamos
            log.info("Horario solución supera la puntuación umbral: " + puntuacionObtenida + " > {} ó " + puntuacionObtenida + " > {}", 
            		 this.manejadorResultadosParams.getUmbralMinimoSolucion(), this.horarioSolucionMayorPuntuacion) ;
        	
        	// Añadimos la solución a la lista
            this.horariosSoluciones.add(horario) ;
            
            // Obtenemos el índice actual por si hubiera otra inserción justo a la vez
            int indiceActual = this.horariosSoluciones.size() - 1 ;
        	
            // Guardamos la puntuación y el índice de la solución óptima
        	this.horarioSolucionMayorPuntuacion 	  = puntuacionObtenida ; 
            this.horarioSolucionMayorPuntuacionIndice = indiceActual ;
            
            // Guardamos el horario en la base de datos
            this.manejadorResultadosParams.getGeneradorService().guardarHorario(generadorInstancia, horario, puntuacionObtenida, Constants.MENSAJE_SOLUCION_ENCONTRADA) ;
        }

        return solucionSuperaUmbral ;
    }
    
    /**
     * Método para agregar un horario de error a la lista
     * @param generadorInstancia generador instancia
     * @param horario solución encontrada
     * @param mensajeError mensaje de error
     * @throws SchoolManagerServerException con un error
     */
	public void agregarHorarioError(GeneradorInstancia generadorInstancia, Horario horario, String mensajeError) throws SchoolManagerServerException
	{
    	// Calculamos las puntuación de este horario de error
        int puntuacionObtenida = horario.calcularPuntuacion() ;

        // Vemos si el horario de error supera el umbral
        boolean errorSuperaUmbral = puntuacionObtenida > this.manejadorResultadosParams.getUmbralMinimoError() &&
                                    this.horarioErrorMayorPuntuacion < puntuacionObtenida ;
		
        // Verificamos si el horario de error cumple unos mínimos y si esta es por ahora la mejor solución de error
        if (!errorSuperaUmbral)
        {
            // Logueamos
            log.info("Horario de error no supera la puntuación umbral: " + puntuacionObtenida + " < {} ó " + puntuacionObtenida + " < {}", 
            		 this.manejadorResultadosParams.getUmbralMinimoError(), this.horarioErrorMayorPuntuacion) ;

            // Borramos de la tabla de generador instancia
            this.manejadorResultadosParams.getGeneradorService().eliminarGeneradorInstancia(generadorInstancia) ;
        }
        else
        {
        	// Logueamos
            log.info("Horario de error supera la puntuación umbral: " + puntuacionObtenida + " > {} ó " + puntuacionObtenida + " > {}", 
            		 this.manejadorResultadosParams.getUmbralMinimoError(), this.horarioErrorMayorPuntuacion) ;
        	
        	// Añadimos el horario con errores a la lista
            this.horariosError.add(horario) ;
            
            // Obtenemos el índice actual por si hubiera otra inserción justo a la vez
            int indiceActual = this.horariosError.size() - 1 ; 
        	
            // Guardamos la puntuación y el índice del horario con errores con más puntuación
        	this.horarioErrorMayorPuntuacion 	   = puntuacionObtenida ; 
            this.horarioErrorMayorPuntuacionIndice = indiceActual ;
            
            // Guardamos el horario en la base de datos
            this.manejadorResultadosParams.getGeneradorService().guardarHorario(generadorInstancia, horario, puntuacionObtenida, mensajeError) ;
        }
	}
	
	/**
	 * @return Horario si se ha encontrado una solución
	 */
	public Horario solucionEncontrada()
	{
		Horario outcome = null ;

		// Vemos si hay algún horario solución
    	if (this.horarioSolucionMayorPuntuacionIndice != -1)
    	{
    		outcome = this.horariosSoluciones.get(this.horarioErrorMayorPuntuacionIndice) ;
    	}
		
		return outcome ;
	}
}
