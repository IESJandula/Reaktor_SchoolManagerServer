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
}
