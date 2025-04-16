package es.iesjandula.reaktor.school_manager_server.utils;

import java.io.File;

/**
 * Clase de utilidades - Constants
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase contiene constantes que se utilizan en diferentes partes de la aplicación para estandarizar valores 
 * comunes, como delimitadores de archivos CSV, rutas, y otras configuraciones generales.
 * -----------------------------------------------------------------------------------------------------------------
 */
public class Constants 
{
    /** 
     * Delimitador para separar valores en archivos CSV.
     * <p>Por defecto, se utiliza la coma (",").</p>
     */
    public static final String CSV_DELIMITER = ",";

    /** 
     * Letra por defecto para el grupo .
     * <p>Indica el grupo base asignado, comenzando con 'A'.</p>
     */
    public static final char GROUP = 'A';
    
	/*********************************************************/
	/******************* Ficheros y carpetas *****************/
	/*********************************************************/
	
	/** Nombre de la carpeta de configuracion */
	public static final String SCHOOL_MANAGER_SERVER_CONFIG      = "school_manager_server_config" ;
	
	/** Nombre de la carpeta de configuracion al ejecutarse */
	public static final String SCHOOL_MANAGER_SERVER_CONFIG_EXEC = "school_manager_config_exec" ;

	/** Fichero con los cursos y etapas */
	public static final String FICHERO_CURSOS_ETAPAS 	  		 = SCHOOL_MANAGER_SERVER_CONFIG_EXEC + File.separator + "cursos_etapas.csv";
	
	/** Fichero con los cursos y etapas */
	public static final String FICHERO_DEPARTAMENTOS 	  		 = SCHOOL_MANAGER_SERVER_CONFIG_EXEC + File.separator + "departamentos.csv";
	
	/*********************************************************/
	/****************** Modo DDL - Create ********************/
	/*********************************************************/

	public static final String MODO_DDL_CREATE = "create";
	
	/*********************************************************/
	/******************* Códigos de error ********************/
	/*********************************************************/
	
	// Carga de datos
	public static final int ERR_CODE_PROCESANDO_CURSO_ETAPA   = 200 ;
	public static final int ERR_CODE_PROCESANDO_DEPARTAMENTOS = 201 ;
	public static final int ERR_CODE_CIERRE_READER 			  = 202 ;
	public static final int ERR_CODE_IO_EXCEPTION             = 203 ;
	public final static int ERROR_CONEXION_FIREBASE = 101;

	// Usuarios/Profesores
	public final static int PROFESOR_NO_ENCONTRADO = 40;

	// Errores Generales/De Conexión
	public final static int TIMEOUT_CONEXION_FIREBASE = 102;
	public final static int IO_EXCEPTION_FIREBASE = 103;

	/** Errores del Generador */

	/** Error - Horario no más ampliable */
	public static final int ERR_CODE_HORARIO_NO_MAS_AMPLIABLE = 300 ;

	/** Error - Sin días disponibles */
	public static final int ERR_CODE_SIN_DIAS_DISPONIBLES     = 301 ;

	/** Error - Superado el límite de restricciones que se pueden asignar a esta asignatura por sus horas */
	public static final int ERR_CODE_SUPERADO_LIMITE_RESTRICC = 302 ;



	/******************************************************/
	/******************************************************/
	/********* Constantes - Generador de horario **********/
	/******************************************************/
	/******************************************************/

	/******************************************************/
	/********************* Umbrales ***********************/
	/******************************************************/	

	/** 120 para 2 cursos, 192-198 para 3 cursos, minimo 320 para 5 cursos */
	/** Umbral minimo con respecto a las soluciones */
	public static final int UMBRAL_MINIMO_SOLUCION 	   = 320 ;
	
	/** Umbral minimo con respecto a los horarios con error */
	public static final int UMBRAL_MINIMO_ERROR    	   = 320 ;

	/******************************************************/
	/*** Referencias - Restricciones - Lista de listas ****/
	/******************************************************/

	/** Referencia a la lista de listas SIN restricciones */
	public static final int INDEX_SIN_RESTRICCIONES  		 = 0 ;

	/** Referencia a la lista de listas CON optativas */
	public static final int INDEX_CON_OPTATIVAS 	 		 = 1 ;

	/** Referencia a la lista de listas CON conciliaciones */
	public static final int INDEX_CON_CONCILIACIONES 	     = 2 ;

	/** Referencia a la lista de listas CON restricciones horarias */
	public static final int INDEX_CON_RESTRICCIONES_HORARIAS = 3 ;

	/******************************************************/
	/*************** Días de la semana *******************/
	/******************************************************/

	/** Número de días a la semana */
	public static final int NUMERO_DIAS_SEMANA 	   	   = 5 ;

	/** Dia de la semana - Lunes */
	public static final int DIA_SEMANA_LUNES 	   	   = 0 ;

	/** Dia de la semana - Martes */
	public static final int DIA_SEMANA_MARTES 	   	   = 1 ;
	
	/** Dia de la semana - Miércoles */
	public static final int DIA_SEMANA_MIERCOLES 	   = 2 ;

	/** Dia de la semana - Jueves */
	public static final int DIA_SEMANA_JUEVES 	   	   = 3 ;
	
	/** Dia de la semana - Viernes */
	public static final int DIA_SEMANA_VIERNES 	   	   = 4 ;

	/******************************************************/
	/***************** Tramos horarios ********************/
	/******************************************************/

	/** Número de tramos horarios al día */
	public static final int NUMERO_TRAMOS_HORARIOS 	   = 6 ;

	/** Tramo horario - Primera hora */
	public static final int TRAMO_HORARIO_PRIMERA_HORA = 0 ;

	/** Tramo horario - Segunda hora */
	public static final int TRAMO_HORARIO_SEGUNDA_HORA = 1 ;

	/** Tramo horario - Tercera hora */
	public static final int TRAMO_HORARIO_TERCERA_HORA = 2 ;

	/** Tramo horario - Cuarta hora */
	public static final int TRAMO_HORARIO_CUARTA_HORA = 3 ;
	
	/** Tramo horario - Quinta hora */
	public static final int TRAMO_HORARIO_QUINTA_HORA  = 4 ;

	/** Tramo horario - Sexta hora */
	public static final int TRAMO_HORARIO_SEXTA_HORA   = 5 ;

	/******************************************************/
	/******************** Threads *************************/
	/******************************************************/

    /** Número de Threads en cada iteración */
    public static final int THREAD_POR_ITERACION 	   = 1 ;
	
    /** Tamaño del ThreadPool */
    public static final int THREAD_POOL_SIZE 		   = 10 ;

	/******************************************************/
	/**************** Carpetas de salida ******************/
	/******************************************************/

    /** Carpeta de "salida" */
    public static final String CARPETA_SALIDA 			 = "salida" ;
    
    /** Carpeta de salida "soluciones" */
    public static final String CARPETA_SALIDA_SOLUCIONES = CARPETA_SALIDA + File.separator + "soluciones" ;
    
    /** Carpeta de salida "errores" */
    public static final String CARPETA_SALIDA_ERRORES	 = CARPETA_SALIDA + File.separator + "errores" ;

	/******************************************************/
	/*************** Factor de puntuación *****************/
	/******************************************************/

    /** Factor de puntuación por número de sesiones insertadas */
	public static final int FACTOR_NUMERO_SESIONES_INSERTADAS 			  = 1 ;

	/** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor */
	public static final int FACTOR_SESIONES_CONSECUTIVAS_PROFESOR 		  = 2 ;

	/** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina */
	public static final int FACTOR_SESIONES_CONSECUTIVAS_PROFESOR_MAT_VES = 4 ;

	/******************************************************/
	/**************** Ocurrencias por día *****************/
	/******************************************************/

	/** Número máximo de ocurrencias por día en ESO y Bachillerato */
	public static final int NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_ESO_BACH 	  = 1 ;

	/** Número máximo de ocurrencias por día en FP */
	/** Si no se pone 2, el sistema no logra encontrar un horario final completo */
    public static final int NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_FP 		  = 2 ;
}
