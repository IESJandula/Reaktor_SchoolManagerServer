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
	public static final String GRUPO_INICIAL = "A";

	/**
	 * Constate que representa el grupo sin grupo asignado
	 * <p>Indica que no hay grupo asignado.</p>
	 */
	public static final String SIN_GRUPO_ASIGNADO = "Sin grupo";

	/**
	 * Constante que representa el grupo de asignaturas optativas.
	 * <p>Este valor se utiliza para identificar o gestionar asignaturas optativas dentro del sistema.</p>
	 */
	public static final String GRUPO_OPTATIVAS = "Optativas";

    /**
     * Tipo de solicitud: Asignatura.
     */
    public static final String TIPO_ASIGNATURA = "Asignatura";

    /**
     * Tipo de solicitud: Reducción.
     */
    public static final String TIPO_REDUCCION = "Reducción";

	/**
	 * Tipo de estado: Matriculado.
	 */
	public static final String ESTADO_MATRICULADO = "MATR";

	/**
	 * Tipo de estado: No matriculado.
	 */
	public static final String ESTADO_NO_MATRICULADO = "NO_MATR";

	/**
	 * Tipo de estado: Pendiente.
	 */
	public static final String ESTADO_PENDIENTE = "PEND";

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
	 *	Error - Datos no procesados
	 */
	public static final int DATOS_NO_PROCESADO = 39;

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

	/**
	 * Error - Archivo vacío
	 */
	public final static int ARCHIVO_VACIO = 48;

	/**
	 * Error - Matricula no encotrada
	 */
	public final static int MATRICULA_NO_ENCONTRADA = 49;

	/**
	 * Error - Datos matricula no encontrados
	 */
	public final static int DATOS_MATRICULA_NO_ENCONTRADA = 51;

	/**
	 * Error - Matricula alumno no encontrada
	 */
	public final static int MATRICULA_ALUMNO_NO_ENCONTRADA = 52;

	/**
	 * Error - Matricula alumno ya existente
	 */
	public final static int MATRICULA_ALUMNO_EXISTENTE = 53;

	/**
	 * Error - Grupo no encontrado
	 */
	public final static int GRUPO_NO_ENCONTRADO = 54;

	/**
	 * Error - Asignaturas mínimas no seleccionadas
	 */
	public final static int  ASIGNATURAS_MINIMAS_NO_SELECCIONADAS = 55;

	/**
	 * Error - Asignatura con bloque asignado
	 */
	public final static int ASIGNATURA_CON_BLOQUE = 56;

	/**
	 * Error - Reducción ya asignada a un profesor
	 */
	public final static int REDUCCION_ASIGNADA = 57;

	/**
	 * Error - Departamento no encontrado
	 */
	public final static int DEPARTAMENTO_NO_ENCONTRADO = 58;

	/**
	 * Error - La reducción ya existe
	 */
	public final static int REDUCCION_EXISTENTE = 59;

	/**
	 * Error - No se encontró la reducción
	 */
	public final static int REDUCCION_NO_ENCONTRADA = 60;

	/**
	 * Error - No se encontro ninguna una reducción asignada a un profesor
	 */
	public final static int REDUCCION_NO_ASIGNADA_A_PROFESOR = 61;

	/**
	 * Error - La asignatura ya ha sido asignada a el profesor
	 */
	public final static int ASIGNATURA_ASIGNADA_A_PROFESOR = 62;

	/**
	 * Error - Días, tramos y tipos horarios no encontrados
	 */
	public final static int DIAS_TRAMOS_TIPOS_HORARIOS_NO_ENCONTRADOS = 63;

	/**
	 * Error - No se han encontrado asignaturas para el departamento
	 */
	public final static int ASIGNATURAS_NO_ENCONTRADAS_PARA_DEPARTAMENTO = 64;

	/**
	 * Error - No se han encontrado grupos para esa asignatura
	 */
	public final static int GRUPOS_NO_ENCONTRADOS_PARA_ASIGNATURA = 65;

	/**
	 * Error - No se encontró una asignatura asignada al profesor con esos datos
	 */
	public final static int ASIGNATURA_NO_ASIGNADA_A_PROFESOR = 66;

	/**
	 * Error - No se encontraron profesores en la base de datos
	 */
	public static final int SIN_PROFESORES_ENCONTRADOS = 67;

	/**
	 * Error - No se ha encontrado el alumno en la base de datos
	 */
	public static final int ALUMNO_NO_ENCONTRADO = 68;

	/**
	 * Error - La asignatura está asigna a un departamento
	 */
	public static final int ASIGNATURA_ASIGNADA_A_DEPARTAMENTO = 69;

	/**
	 * Error - El alumno está asignado a un grupo y no se puede desmatricular
	 */
	public static final int ALUMNO_ASIGNADO_A_GRUPO = 70;

	/**
	 * Error - La relación entre la asignatura y el profesor no existe
	 */
	public static final int IMPARTIR_NO_ENCONTRADA = 71;

	/**
	 * Error - El día y el tramo no existe
	 */
	public static final int DIA_TRAMO_NO_ENCONTRADO = 72;

	/**
	 * Error - No se han encontrado días de la semana
	 */
	public static final int DIAS_SEMANA_NO_ENCONTRADOS = 73;

	/**
	 * Error - No se han encontrado tramos horarios
	 */
	public static final int TRAMOS_HORARIOS_NO_ENCONTRADOS = 74;

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
	 * Error - Validaciones de datos incorrectos
	 */
	public static final int ERROR_VALIDACIONES_DATOS_INCORRECTOS = 304;

	/**
	 * Error - No hay un generador en curso
	 */
	public static final int ERROR_CODE_NO_GENERADOR_EN_CURSO = 305;

	/**
	 * Error - No se encontró la instancia del generador
	 */
	public static final int ERROR_CODE_GENERADOR_INSTANCIA_NO_ENCONTRADA = 306;

	/**
	 * Error - No se han encontrado sesiones asignadas
	 */
	public static final int ERROR_CODE_SESIONES_ASIGNADAS_NO_ENCONTRADAS = 307;
	
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
	/*********** Constantes - Validador de datos **********/
	/******************************************************/

	/** Tipo de error de datos - Warning */
	public static final String ERROR_DATOS_TIPO_WARNING = "warning" ;

	/** Tipo de error de datos - Error */
	public static final String ERROR_DATOS_TIPO_ERROR   = "error" ;

	/******************************************************/
	/******************************************************/
	/********* Constantes - Generador de horario **********/
	/******************************************************/
	/******************************************************/


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
	/*************** Constantes - Generador ***************/
	/******************************************************/

	/**
	 * Dia de la semana - Sin Seleccionar
	 */
	public static final String SIN_SELECCIONAR = "Sin Seleccionar" ;


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
	/******************** Tipo de horario *****************/
	/******************************************************/

	/**
	 * Tipo de horario - Matutino
	 */	
	public static final String TIPO_HORARIO_MATUTINO = "mañana";

	/**
	 * Tipo de horario - Vespertino
	 */
	public static final String TIPO_HORARIO_VESPERTINO = "tarde";

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
	public static final String ESTADO_GENERADOR_EN_CURSO   = "EN_CURSO" ;

	/**
	 * Estado del generador - Error
	 */
	public static final String ESTADO_GENERADOR_ERROR      = "ERROR" ;

	/**
	 * Estado del generador - Finalizado
	 */
	public static final String ESTADO_GENERADOR_FINALIZADO = "FINALIZADO" ;

	/**
	 * Estado del generador - Detenido
	 */
	public static final String ESTADO_GENERADOR_DETENIDO   = "DETENIDO" ;

	/******************************************************/
	/******************** Categorías **********************/
	/******************************************************/
	
	/**
	 * Categoría de la solución - General
	 */	
	public static final String CATEGORIA_SOLUCION_GENERAL = "General";

	/**
	 * Categoría de la solución - Profesor
	 */	
	public static final String CATEGORIA_SOLUCION_PROFESOR = "Profesor";

	/******************************************************/
	/******************** Soluciones **********************/
	/******************************************************/

	/**
	 * Solución - Consecutividad
	 */
	public static final String SOL_INFO_SESIONES_CONSECUTIVAS = "Sesiones consecutivas";


	/******************************************************/
	/******************** Mensajes ************************/
	/******************************************************/
	


	/**
	 * Mensaje de solución encontrada
	 */
	public static final String MENSAJE_SOLUCION_ENCONTRADA = "Solución encontrada" ;


	/*********************************************************/
	/******************** Tabla Constantes *******************/
	/*********************************************************/

	/** Tabla de constantes - Selección horarios por claustro */
	public static final String TABLA_CONST_SELECCION_HORARIOS_POR_CLAUSTRO = "Selección horarios por claustro";

	/** Tabla de constantes - Umbral mínimo de soluciones */
	public static final String TABLA_CONST_UMBRAL_MINIMO_SOLUCION 		   = "Umbral mínimo de soluciones";

	/** Tabla de constantes - Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor */
	public static final String TABLA_CONST_FACTOR_SESIONES_CONSECUTIVAS = "Factor de puntuación - Sesiones consecutivas";


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

	/**
	 * Constante - Parámetros YAML - Umbral mínimo de soluciones
	 */
	public static final String PARAM_YAML_UMBRAL_MINIMO_SOLUCION = "reaktor.constantes.umbralMinimoSolucion";

	/**
	 * Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor
	 */
	public static final String PARAM_YAML_FACTOR_SESIONES_CONSECUTIVAS = "reaktor.constantes.factores.sesionesConsecutivas";

	/**
	 * Constante - Parámetros YAML - MODO_INICIALIZAR_SISTEMA
	 */
	public static final String MODO_INICIALIZAR_SISTEMA = "true";



}
