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
	 * Letra para el grupo inicial inicial
	 * <p>Indica el grupo base asignado, comenzando con 'A'.</p>
	 */
	public static final char GRUPO_INICIAL = 'A';

	/**
	 * Letra para el grupo sin grupo asignado
	 * <p>Indica que no hay grupo asignado, comenzando con 'Z'.</p>
	 */
	public static final char SIN_GRUPO_ASIGNADO = 'Z';


	/*********************************************************/
	/******************* Ficheros y carpetas *****************/
	/*********************************************************/

	/**
	 * Nombre de la carpeta de configuracion
	 */
	public static final String SCHOOL_MANAGER_SERVER_CONFIG = "school_manager_server_config";

	/**
	 * Nombre de la carpeta de configuracion al ejecutarse
	 */
	public static final String SCHOOL_MANAGER_SERVER_CONFIG_EXEC = "school_manager_config_exec";

	/**
	 * Fichero con los cursos y etapas
	 */
	public static final String FICHERO_CURSOS_ETAPAS = SCHOOL_MANAGER_SERVER_CONFIG_EXEC + File.separator + "cursos_etapas.csv";

	/**
	 * Fichero con los cursos y etapas
	 */
	public static final String FICHERO_DEPARTAMENTOS = SCHOOL_MANAGER_SERVER_CONFIG_EXEC + File.separator + "departamentos.csv";

	/**
	 * Fichero con los dias, tramos y tipo de horario
	 */
	public static final String FICHERO_DIAS_TRAMOS_TIPO_HORARIO = SCHOOL_MANAGER_SERVER_CONFIG_EXEC + File.separator + "dias_tramos_tipo_horario.csv";

	/*********************************************************/
	/****************** Modo DDL - Create ********************/
	/*********************************************************/

	public static final String MODO_DDL_CREATE = "create";

	/*********************************************************/
	/******************* Códigos de error ********************/
	/*********************************************************/

	/************* ERRORES - Lógica de negocio ***************/
	/**
	 * Error - Archivo vacío
	 */
	public final static int ARCHIVO_VACIO = 30;

	/**
	 * Error - Matricula no encotrada
	 */
	public final static int MATRICULA_NO_ENCONTRADA = 31;

	/**
	 * Error - Datos matricula no encontrados
	 */
	public final static int DATOS_MATRICULA_NO_ENCONTRADA = 32;

	/**
	 * Error - Matricula alumno no encontrada
	 */
	public final static int MATRICULA_ALUMNO_NO_ENCONTRADA = 33;

	/**
	 * Error - Matricula alumno ya existente
	 */
	public final static int MATRICULA_ALUMNO_EXISTENTE = 34;

	/**
	 * Error - Grupo no encontrado
	 */
	public final static int GRUPO_NO_ENCONTRADO = 35;


	public final static int  ASIGNATURAS_MINIMAS_NO_SELECCIONADAS = 37;

	public final static int ASIGNATURA_CON_BLOQUE = 37;

	/**
	 * Error - No se encontró el profesor
	 */
	public final static int PROFESOR_NO_ENCONTRADO = 40;

	/**
	 * Error - No se encontró el curso etapa grupo
	 */
	public final static int CURSO_ETAPA_GRUPO_NO_ENCONTRADO = 41;

	/**
	 * Error - No se encontró la asignatura
	 */
	public final static int ASIGNATURA_NO_ENCONTRADA = 42;

	/**
	 * Error - No se encontró el curso etapa
	 */
	public final static int CURSO_ETAPA_NO_ENCONTRADO = 43;

	/**
	 * Error - No se encontró ningun curso
	 */
	public final static int SIN_CURSOS_ETAPAS_ENCONTRADOS = 44;

	/**
	 * Error - No se encontró ningun alumno
	 */
	public final static int SIN_ALUMNOS_ENCONTRADOS = 45;

	/**
	 * Error - La conciliación no es válida
	 */
	public static final int ERROR_CONCILIACION_NO_VALIDA = 46;

	/**
	 * Error - El tipo de horario no es válido
	 */
	public static final int ERROR_TIPO_HORARIO_NO_VALIDO = 47;

	/************* ERRORES - Generales/De Conexión ***************/

	/**
	 * Error genérico
	 */
	public static final int ERROR_GENERICO = 100;

	/**
	 * Error de conexión con Firebase
	 */
	public final static int ERROR_CONEXION_FIREBASE = 101;

	/**
	 * Timeout de conexión con Firebase
	 */
	public final static int TIMEOUT_CONEXION_FIREBASE = 102;

	/**
	 * IO Exception de Firebase
	 */
	public final static int IO_EXCEPTION_FIREBASE = 103;

	/**
	 * Error de conexión con el lector de datos
	 */
	public final static int IO_EXCEPTION = 104;


	/************* ERRORES - Carga de datos ***************/

	/**
	 * Error - Procesando curso etapa
	 */
	public static final int ERR_CODE_PROCESANDO_CURSO_ETAPA = 200;

	/**
	 * Error - Procesando departamentos
	 */
	public static final int ERR_CODE_PROCESANDO_DEPARTAMENTOS = 201;

	/**
	 * Error - Cierre de lector
	 */
	public static final int ERR_CODE_CIERRE_READER = 202;

	/**
	 * Error - IO Exception
	 */
	public static final int ERR_CODE_IO_EXCEPTION = 203;

	/************* Errores del Generador de horario ***************/

	/**
	 * Error - Horario no más ampliable
	 */
	public static final int ERR_CODE_HORARIO_NO_MAS_AMPLIABLE = 300;

	/**
	 * Error - Sin días disponibles
	 */
	public static final int ERR_CODE_SIN_DIAS_DISPONIBLES = 301;

	/**
	 * Error - Superado el límite de restricciones que se pueden asignar a esta asignatura por sus horas
	 */
	public static final int ERR_CODE_SUPERADO_LIMITE_RESTRICCIONES = 302;

	/**
	 * Mensaje de error - Hay un generador en curso
	 */
	public static final int ERROR_CODE_GENERADOR_EN_CURSO = 303;

	/**
	 * Mensaje de error - No hay cursos/etapas/grupos por cada curso/etapa
	 */
	public static final int ERROR_CODE_SIN_CURSOS_ETAPAS_ENCONTRADOS = 304;

	/**
	 * Constantes/Configuración
	 */
	public final static int CONSTANTE_NO_ENCONTRADA = 50;

	// Códigos de error para validaciones globales previas a la Selección horarios por claustro
	public static final int ERROR_OBTENIENDO_PARAMETROS = 23;
	public static final int ERROR_APP_DESHABILITADA = 24;


	/******************************************************/
	/*************** Constantes - Conciliaciones **********/
	/******************************************************/

	/**
	 * Conciliación - Sin conciliación
	 */
	public static final String CONCILIACION_SIN_CONCILIACION = "SIN_CONCILIACION";

	/**
	 * Conciliación - Entrar después de la segunda hora
	 */
	public static final String CONCILIACION_ENTRAR_DESPUES_SEGUNDA_HORA = "ENTRAR_DESPUES_SEGUNDA_HORA";

	/**
	 * Conciliación - Salir antes de la quinta hora
	 */
	public static final String CONCILIACION_SALIR_ANTES_QUINTA_HORA = "SALIR_ANTES_QUINTA_HORA";

	/******************************************************/
	/******************************************************/
	/********* Constantes - Generador de horario **********/
	/******************************************************/
	/******************************************************/

	/******************************************************/
	/********************* Umbrales ***********************/
	/******************************************************/

	/** 120 para 2 cursos, 192-198 para 3 cursos, minimo 320 para 5 cursos */
	/**
	 * Umbral minimo con respecto a las soluciones
	 */
	public static final int UMBRAL_MINIMO_SOLUCION = 320;

	/**
	 * Umbral minimo con respecto a los horarios con error
	 */
	public static final int UMBRAL_MINIMO_ERROR = 320;

	/******************************************************/
	/*** Referencias - Restricciones - Lista de listas ****/
	/******************************************************/

	/**
	 * Referencia a la lista de listas SIN restricciones
	 */
	public static final int INDEX_SIN_RESTRICCIONES = 0;

	/**
	 * Referencia a la lista de listas CON optativas
	 */
	public static final int INDEX_CON_OPTATIVAS = 1;

	/**
	 * Referencia a la lista de listas CON conciliaciones
	 */
	public static final int INDEX_CON_CONCILIACIONES = 2;

	/**
	 * Referencia a la lista de listas CON restricciones horarias
	 */
	public static final int INDEX_CON_RESTRICCIONES_HORARIAS = 3;

	/******************************************************/
	/*************** Días de la semana *******************/
	/******************************************************/

	/**
	 * Número de días a la semana
	 */
	public static final int NUMERO_DIAS_SEMANA = 5;

	/**
	 * Dia de la semana - Lunes
	 */
	public static final int DIA_SEMANA_LUNES = 0;

	/**
	 * Dia de la semana - Martes
	 */
	public static final int DIA_SEMANA_MARTES = 1;

	/**
	 * Dia de la semana - Miércoles
	 */
	public static final int DIA_SEMANA_MIERCOLES = 2;

	/**
	 * Dia de la semana - Jueves
	 */
	public static final int DIA_SEMANA_JUEVES = 3;

	/**
	 * Dia de la semana - Viernes
	 */
	public static final int DIA_SEMANA_VIERNES = 4;

	/******************************************************/
	/***************** Tramos horarios ********************/
	/******************************************************/

	/**
	 * Número de tramos horarios al día
	 */
	public static final int NUMERO_TRAMOS_HORARIOS = 6;

	/**
	 * Tramo horario - Primera hora
	 */
	public static final int TRAMO_HORARIO_PRIMERA_HORA = 0;

	/**
	 * Tramo horario - Segunda hora
	 */
	public static final int TRAMO_HORARIO_SEGUNDA_HORA = 1;

	/**
	 * Tramo horario - Tercera hora
	 */
	public static final int TRAMO_HORARIO_TERCERA_HORA = 2;

	/**
	 * Tramo horario - Cuarta hora
	 */
	public static final int TRAMO_HORARIO_CUARTA_HORA = 3;

	/**
	 * Tramo horario - Quinta hora
	 */
	public static final int TRAMO_HORARIO_QUINTA_HORA = 4;

	/**
	 * Tramo horario - Sexta hora
	 */
	public static final int TRAMO_HORARIO_SEXTA_HORA = 5;

	/******************************************************/
	/******************** Threads *************************/
	/******************************************************/

	/**
	 * Número de Threads en cada iteración
	 */
	public static final int THREAD_POR_ITERACION = 1;

	/**
	 * Tamaño del ThreadPool
	 */
	public static final int THREAD_POOL_SIZE = 10;

	/******************************************************/
	/**************** Carpetas de salida ******************/
	/******************************************************/

	/**
	 * Carpeta de "salida"
	 */
	public static final String CARPETA_SALIDA = "salida";

	/**
	 * Carpeta de salida "soluciones"
	 */
	public static final String CARPETA_SALIDA_SOLUCIONES = CARPETA_SALIDA + File.separator + "soluciones";

	/**
	 * Carpeta de salida "errores"
	 */
	public static final String CARPETA_SALIDA_ERRORES = CARPETA_SALIDA + File.separator + "errores";

	/******************************************************/
	/*************** Factor de puntuación *****************/
	/******************************************************/

	/**
	 * Factor de puntuación por número de sesiones insertadas
	 */
	public static final int FACTOR_NUMERO_SESIONES_INSERTADAS = 1;

	/**
	 * Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor
	 */
	public static final int FACTOR_SESIONES_CONSECUTIVAS_PROFESOR = 2;

	/**
	 * Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina
	 */
	public static final int FACTOR_SESIONES_CONSECUTIVAS_PROFESOR_MAT_VES = 4;

	/******************************************************/
	/**************** Ocurrencias por día *****************/
	/******************************************************/

	/**
	 * Número máximo de ocurrencias por día en ESO y Bachillerato
	 */
	public static final int NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_ESO_BACH = 1;

	/** Número máximo de ocurrencias por día en FP */
	/**
	 * Si no se pone 2, el sistema no logra encontrar un horario final completo
	 */
	public static final int NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_FP = 2;

	/******************************************************/
	/**************** Estado del generador ****************/
	/******************************************************/

	/**
	 * Estado del generador - En curso
	 */
	public static final String ESTADO_EN_CURSO = "EN_CURSO";

	/**
	 * Estado del generador - Finalizado
	 */
	public static final String ESTADO_FINALIZADO = "FINALIZADO";

	/**
	 * Estado del generador - Detenido
	 */
	public static final String ESTADO_DETENIDO = "DETENIDO";

	/*********************************************************/
	/******************** Tabla Constantes *******************/
	/*********************************************************/

	public static final String TABLA_CONST_SELECCION_HORARIOS_POR_CLAUSTRO = "Selección horarios por claustro";

	/*********************************************************/
	/******************* Parámetros YAML *********************/
	/*********************************************************/

	/**
	 * Parametro YAML para inicializar las constantes
	 */
	public static final String PARAM_YAML_REINICIAR_CONSTANTES = "reaktor.reiniciarConstantes";

	/**
	 * Constante - Parámetros YAML - Selección horarios por claustro
	 */
	public static final String PARAM_YAML_SELECCION_HORARIOS_POR_CLAUSTRO = "reaktor.constantes.seleccionHorariosPorClaustro";

	public static final String MODO_INICIALIZAR_SISTEMA = "true";

}
