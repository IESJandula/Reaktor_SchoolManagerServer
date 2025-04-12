package es.iesjandula.reaktor.school_manager_server.generator.core.manejadores;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import es.iesjandula.reaktor.school_manager_server.generator.core.Horario;
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
    
    /** Carpeta de salida - Soluciones */
    private File carpetaSalidaSoluciones ;
    
    /** Carpeta de salida - Errores */
    private File carpetaSalidaErrores ;
    
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
        
        // Creamos las carpetas de resultados si no existen
        this.crearCarpetasDeResultados() ;
    }

    /**
     * Creación de las carpetas de resultados
     */
    private void crearCarpetasDeResultados()
    {
    	// Si la carpeta de soluciones no existe ...
    	this.carpetaSalidaSoluciones = new File(Constants.CARPETA_SALIDA_SOLUCIONES) ;
        if (!this.carpetaSalidaSoluciones.exists())
        {
        	// ... La creamos
        	this.carpetaSalidaSoluciones.mkdirs() ;
        }
        
    	// Si la carpeta de errores no existe ...
        this.carpetaSalidaErrores = new File(Constants.CARPETA_SALIDA_ERRORES) ;
        if (!this.carpetaSalidaErrores.exists())
        {
        	// ... La creamos
        	this.carpetaSalidaErrores.mkdirs() ;
        }
	}
    
	/**
     * Método para agregar una solución a la lista
     * @param horario solución encontrada
	 * @throws SchoolManagerServerException con un error
     */
    public void agregarHorarioSolucion(Horario horario) throws SchoolManagerServerException
    {
    	// Si llegamos aquí es porque se encontró un horario definitivo,  
		// por lo que lo informamos al manejador de soluciones
		
    	// Calculamos las puntuación de esta solución
        int puntuacionObtenida = horario.calcularPuntuacion() ;
    	
        // Verificamos si la solución cumple unos mínimos
        if (puntuacionObtenida > this.manejadorResultadosParams.getUmbralMinimoSolucion() &&
        	this.horarioSolucionMayorPuntuacion < puntuacionObtenida)
        {
        	// Logueamos
        	log.info("Horario solución que supera la puntuación umbral mayor actual {} > {}", 
        			 puntuacionObtenida, this.horarioSolucionMayorPuntuacion) ;
        	
        	// Añadimos la solución a la lista
            this.horariosSoluciones.add(horario) ;
            
            // Obtenemos el índice actual por si hubiera otra inserción justo a la vez
            int indiceActual = this.horariosSoluciones.size() - 1 ;
        	
            // Guardamos la puntuación y el índice de la solución óptima
        	this.horarioSolucionMayorPuntuacion 	  = puntuacionObtenida ; 
            this.horarioSolucionMayorPuntuacionIndice = indiceActual ;
            
            // Guardamos el fichero en la carpeta
            this.guardarHorariosEnFichero(horario, puntuacionObtenida, this.carpetaSalidaSoluciones, "Solución encontrada") ;
        }
    }
    
    /**
     * Método para agregar un horario de error a la lista
     * @param horario solución encontrada
     * @param mensajeError mensaje de error
     * @throws SchoolManagerServerException con un error
     */
	public void agregarHorarioError(Horario horario, String mensajeError) throws SchoolManagerServerException
	{
    	// Calculamos las puntuación de este horario de error
        int puntuacionObtenida = horario.calcularPuntuacion() ;
		
        // Verificamos si el horario de error cumple unos mínimos y si esta es por ahora la mejor solución de error
        if (puntuacionObtenida > this.manejadorResultadosParams.getUmbralMinimoError() &&
        	this.horarioErrorMayorPuntuacion < puntuacionObtenida)
        {
        	// Logueamos
        	log.info("Horario de error que supera la puntuación umbral {} > {}", 
        			 puntuacionObtenida, this.manejadorResultadosParams.getUmbralMinimoError()) ;
        	
        	// Añadimos el horario con errores a la lista
            this.horariosError.add(horario) ;
            
            // Obtenemos el índice actual por si hubiera otra inserción justo a la vez
            int indiceActual = this.horariosError.size() - 1 ; 
        	
            // Guardamos la puntuación y el índice del horario con errores con más puntuación
        	this.horarioErrorMayorPuntuacion 	   = puntuacionObtenida ; 
            this.horarioErrorMayorPuntuacionIndice = indiceActual ;
            
            // Guardamos el fichero en la carpeta
            this.guardarHorariosEnFichero(horario, puntuacionObtenida, this.carpetaSalidaErrores, mensajeError) ;
        }
	}
	
	/**
	 * @param horario horario
	 * @param puntuacionObtenida puntuación obtenida
	 * @param carpetaSalida carpeta de salida
	 * @param mensajeError mensaje de error
	 * @throws SchoolManagerServerException con un error
	 */
    private void guardarHorariosEnFichero(Horario horario, 
    									  int puntuacionObtenida,
    									  File carpetaSalida,
    									  String mensajeError) throws SchoolManagerServerException
    {
        File archivo = this.crearFichero(horario, puntuacionObtenida, carpetaSalida) ;
        
        FileWriter fileWriter = null ;
        
        try
        {
        	// Abrimos el nuevo fichero
        	fileWriter = new FileWriter(archivo) ;
        	
        	// Copiamos en su interior la salida del horario
            fileWriter.write(horario.toString()) ;

            // Añadimos el mensaje de error
            fileWriter.write(mensajeError) ;
        }
        catch (IOException ioException)
        {
        	String errorString = "IOException mientras se creaba fichero de salida " + archivo.getAbsolutePath() ;
        	
        	log.error(errorString, ioException) ;
        	throw new SchoolManagerServerException(Constants.ERR_CODE_IO_EXCEPTION, errorString, ioException) ;
        }
        finally
        {
        	if (fileWriter != null)
        	{
        		try
        		{
					fileWriter.close() ;
				}
        		catch (IOException ioException)
        		{
                	String errorString = "IOException mientras se cerraba fichero de salida " + archivo.getAbsolutePath() ;
                	
                	log.error(errorString, ioException) ;
                	throw new SchoolManagerServerException(Constants.ERR_CODE_IO_EXCEPTION, errorString, ioException) ;
				}
        	}
        }
	}

    /**
     * @param horario horario
     * @param puntuacionObtenida puntuación obtenida
	 * @param carpetaSalida carpeta de salida
     * @return una instancia de tipo File con el nuevo fichero a crear
     */
	private File crearFichero(Horario horario, int puntuacionObtenida, File carpetaSalida)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSS") ;
        
        Date now = new Date() ;
        String fechaActual = dateFormat.format(now) ;
        
        // Le ponemos nombre al fichero
        String nombreFichero = puntuacionObtenida + " - " + fechaActual + ".txt" ;
        
        // Creamos y devolvemos la instancia de fichero
        return new File(carpetaSalida, nombreFichero) ;
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
