package es.iesjandula.reaktor.school_manager_server.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
	 * Grupo por defecto para entradas de catálogo curso/etapa creadas desde la vista de espacios.
	 * <p>Permite persistir en {@code Curso_Etapa_Grupo} sin alterar la PK ni los grupos reales (A, B, etc.).</p>
	 */
	public static final String GRUPO_CATALOGO_CURSO_ETAPA = "-";

	/**
	 * Valor de curso académico para registros globales (grupos docentes, matrículas, espacios fijos).
	 * <p>El catálogo curso/etapa por año académico usa el identificador real del curso académico (p. ej. {@code 2025/2026}).</p>
	 */
	public static final String CURSO_ACADEMICO_GLOBAL = "";

	/**
	 * Número de horas lectivas semanales que imparte un profesor a jornada completa.
	 * <p>Se usa como jornada lectiva estándar para calcular las horas que aporta la plantilla
	 * de un departamento ({@code plantilla * HORAS_LECTIVAS_PROFESOR}) y para proponer el número
	 * de profesores necesarios ({@code techo(horasNecesarias / HORAS_LECTIVAS_PROFESOR)}).</p>
	 */
	public static final int HORAS_LECTIVAS_PROFESOR = 18;

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
	 * Error - El tipo de horario no es válido
	 */
	public static final int ERROR_TIPO_HORARIO_NO_VALIDO = 46;

	/**
	 * Error - Archivo vacío
	 */
	public final static int ARCHIVO_VACIO = 47;

	/**
	 * Error - Matricula no encotrada
	 */
	public final static int MATRICULA_NO_ENCONTRADA = 48;

	/**
	 * Error - Datos matricula no encontrados
	 */
	public final static int DATOS_MATRICULA_NO_ENCONTRADA = 49;

	/**
	 * Error - Matricula alumno no encontrada
	 */
	public final static int MATRICULA_ALUMNO_NO_ENCONTRADA = 50;

	/**
	 * Error - Matricula alumno ya existente
	 */
	public final static int MATRICULA_ALUMNO_EXISTENTE = 51;

	/**
	 * Error - Grupo no encontrado
	 */
	public final static int GRUPO_NO_ENCONTRADO = 52;

	/**
	 * Error - Asignaturas mínimas no seleccionadas
	 */
	public final static int  ASIGNATURAS_MINIMAS_NO_SELECCIONADAS = 53;

	/**
	 * Error - Asignatura con bloque asignado
	 */
	public final static int ASIGNATURA_CON_BLOQUE = 54;

	/**
	 * Error - Reducción ya asignada a un profesor
	 */
	public final static int REDUCCION_ASIGNADA = 55;

	/**
	 * Error - Departamento no encontrado
	 */
	public final static int DEPARTAMENTO_NO_ENCONTRADO = 56;

	/**
	 * Error - La reducción ya existe
	 */
	public final static int REDUCCION_EXISTENTE = 57;

	/**
	 * Error - No se encontró la reducción
	 */
	public final static int REDUCCION_NO_ENCONTRADA = 58;

	/**
	 * Error - No se encontro ninguna una reducción asignada a un profesor
	 */
	public final static int REDUCCION_NO_ASIGNADA_A_PROFESOR = 59;

	/**
	 * Error - La asignatura ya ha sido asignada a el profesor
	 */
	public final static int ASIGNATURA_ASIGNADA_A_PROFESOR = 60;

	/**
	 * Error - Días, tramos y tipos horarios no encontrados
	 */
	public final static int DIAS_TRAMOS_TIPOS_HORARIOS_NO_ENCONTRADOS = 61;

	/**
	 * Error - No se han encontrado asignaturas para el departamento
	 */
	public final static int ASIGNATURAS_NO_ENCONTRADAS_PARA_DEPARTAMENTO = 62;

	/**
	 * Error - No se han encontrado grupos para esa asignatura
	 */
	public final static int GRUPOS_NO_ENCONTRADOS_PARA_ASIGNATURA = 63;

	/**
	 * Error - No se encontró una asignatura asignada al profesor con esos datos
	 */
	public final static int ASIGNATURA_NO_ASIGNADA_A_PROFESOR = 64;

	/**
	 * Error - No se encontraron profesores en la base de datos
	 */
	public static final int SIN_PROFESORES_ENCONTRADOS = 65;

	/**
	 * Error - No se ha encontrado el alumno en la base de datos
	 */
	public static final int ALUMNO_NO_ENCONTRADO = 66;

	/**
	 * Error - La asignatura está asigna a un departamento
	 */
	public static final int ASIGNATURA_ASIGNADA_A_DEPARTAMENTO = 67;

	/**
	 * Error - El alumno está asignado a un grupo y no se puede desmatricular
	 */
	public static final int ALUMNO_ASIGNADO_A_GRUPO = 68;

	/**
	 * Error - La relación entre la asignatura y el profesor no existe
	 */
	public static final int IMPARTIR_NO_ENCONTRADA = 69;

	/**
	 * Error - El día y el tramo no existe
	 */
	public static final int DIA_TRAMO_NO_ENCONTRADO = 70;

	/**
	 * Error - No se han encontrado días de la semana
	 */
	public static final int DIAS_SEMANA_NO_ENCONTRADOS = 71;

	/**
	 * Error - No se han encontrado tramos horarios
	 */
	public static final int TRAMOS_HORARIOS_NO_ENCONTRADOS = 72;

	/**
	 * Error - Las asignaturas del bloque tienen horas diferentes entre sí
	 */
	public static final int ASIGNATURAS_BLOQUE_HORAS_DIFERENTES = 73;

	/**
	 * Error - Una asignatura en bloque no puede marcarse como sin docencia, o un bloque no puede incluir asignaturas sin docencia
	 */
	public static final int ASIGNATURA_BLOQUE_SIN_DOCENCIA = 74;

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
	 * Mensaje de error - Hay un generador en curso
	 */
	public static final int ERROR_CODE_GENERADOR_EN_CURSO = 302;

	/**
	 * Error - Validaciones de datos incorrectos
	 */
	public static final int ERROR_VALIDACIONES_DATOS_INCORRECTOS = 303;

	/**
	 * Error - No hay un generador en curso
	 */
	public static final int ERROR_CODE_NO_GENERADOR_EN_CURSO = 304;

	/**
	 * Error - No se encontró la instancia del generador
	 */
	public static final int ERROR_CODE_GENERADOR_INSTANCIA_NO_ENCONTRADA = 305;

	/**
	 * Error - No se han encontrado sesiones asignadas
	 */
	public static final int ERROR_CODE_SESIONES_ASIGNADAS_NO_ENCONTRADAS = 306;
	
	/**
	 * Constantes/Configuración
	 */
	public final static int CONSTANTE_NO_ENCONTRADA = 50;

	// Códigos de error para validaciones globales previas a la Selección horarios por claustro
	public static final int ERROR_OBTENIENDO_PARAMETROS = 23;
	public static final int ERROR_APP_DESHABILITADA = 24;


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

	/** Factor de huecos entre sesiones */
	public static final double FACTOR_HUECOS = 2.00d;

	/** Factor divisor de huecos entre sesiones */
	public static final double FACTOR_DIVISOR_HUECOS = 6.00d;

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
	 * Solución - Horario con huecos entre sesiones
	 */
	public static final String SOL_INFO_HUECOS    = "Huecos";

	/**
	 * Solución - Horario con preferencias diarias de no tener clase a primera hora o no tener clase a última hora
	 */
	public static final String SOL_INFO_PREFERENCIAS_DIARIAS = "Preferencias diarias";

	/**
	 * Solución - Preferencias concretas de no tener clase en unas horas determinadas
	 */
	public static final String SOL_INFO_PREFERENCIAS_CONCRETAS = "Preferencias concretas";

	/**
	 * Número máximo de preferencias concretas a tener en cuenta
	 */
	public static final int NUMERO_MAXIMO_PREFERENCIAS_CONCRETAS = 3 ;


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
	 * Constante - Parámetros YAML - MODO_INICIALIZAR_SISTEMA
	 */
	public static final String MODO_INICIALIZAR_SISTEMA = "true";


	/******************************************************/
	/*************** Espacios - Cursos académicos *********/
	/******************************************************/

	/**
	 * Lista de cursos académicos que se inicializan en el sistema.
	 */
	public static final List<String> CURSOS_ACADEMICOS = Arrays.asList("2025/2026", "2026/2027", "2027/2028", "2028/2029",
																	   "2029/2030", "2030/2031", "2031/2032",
																	   "2032/2033", "2033/2034", "2034/2035",
																	   "2035/2036", "2036/2037", "2037/2038",
																	   "2038/2039", "2039/2040", "2040/2041",
																	   "2041/2042", "2042/2043", "2043/2044",
																	   "2044/2045", "2045/2046", "2046/2047",
																	   "2047/2048", "2048/2049", "2049/2050");

	/**
	 * Parámetro YAML que indica el curso académico seleccionado por defecto.
	 */
	public static final String PARAM_YAML_CURSO_ACADEMICO_SELECCIONADO = "reaktor.curso_academico";

	/**
	 * Valor por defecto del curso académico seleccionado.
	 */
	public static final String VALOR_CURSO_ACADEMICO_SELECCIONADO = "2025/2026";

	/******************************************************/
	/*********** Espacios - Códigos de error *************/
	/******************************************************/

	/** Error - El curso académico es nulo o vacío */
	public static final int ERR_CURSO_ACADEMICO_NULO_VACIO_CODE = 400;

	/** Mensaje - El curso académico es nulo o vacío */
	public static final String ERR_CURSO_ACADEMICO_NULO_VACIO_MESSAGE = "El curso académico no puede ser nulo o vacío";

	/** Error - El curso académico no existe */
	public static final int ERR_CURSO_ACADEMICO_NO_EXISTE_CODE = 401;

	/** Mensaje - El curso académico no existe */
	public static final String ERR_CURSO_ACADEMICO_NO_EXISTE_MESSAGE = "El curso académico no existe";

	/** Error - No hay ningún curso académico seleccionado */
	public static final int ERR_NO_CURSO_ACADEMICO_SELECCIONADO_CODE = 402;

	/** Mensaje - No hay ningún curso académico seleccionado */
	public static final String ERR_NO_CURSO_ACADEMICO_SELECCIONADO_MESSAGE = "No hay ningún curso académico seleccionado";

	/** Error - La etapa es nula o vacía */
	public static final int ERR_ETAPA_NULO_VACIO_CODE = 411;

	/** Mensaje - La etapa es nula o vacía */
	public static final String ERR_ETAPA_NULO_VACIO_MESSAGE = "La etapa no puede ser nula o vacía";

	/** Error - El grupo es nulo o vacío */
	public static final int ERR_GRUPO_NULO_VACIO_CODE = 412;

	/** Mensaje - El grupo es nulo o vacío */
	public static final String ERR_GRUPO_NULO_VACIO_MESSAGE = "El grupo no puede ser nulo o vacío";

	/** Error - El curso, etapa y grupo ya existe */
	public static final int ERR_CURSO_ETAPA_GRUPO_YA_EXISTE_CODE = 413;

	/** Mensaje - El curso, etapa y grupo ya existe */
	public static final String ERR_CURSO_ETAPA_GRUPO_YA_EXISTE_MESSAGE = "El curso, etapa y grupo ya existe en la base de datos";

	/** Error - El curso, etapa y grupo no existe */
	public static final int ERR_CURSO_ETAPA_GRUPO_NO_EXISTE_CODE = 414;

	/** Mensaje - El curso, etapa y grupo no existe */
	public static final String ERR_CURSO_ETAPA_GRUPO_NO_EXISTE_MESSAGE = "El curso, etapa y grupo no existe en la base de datos";

	/** Error - El grupo está asignado a un espacio fijo */
	public static final int ERR_GRUPO_ASIGNADO_A_ESPACIO_FIJO_CODE = 415;

	/** Mensaje - El grupo está asignado a un espacio fijo */
	public static final String ERR_GRUPO_ASIGNADO_A_ESPACIO_FIJO_MESSAGE = "No puedes borrar el grupo mientras esté asociado a un espacio fijo";

	/** Error - El nombre del espacio es nulo o vacío */
	public static final int ERR_ESPACIO_NOMBRE_NULO_VACIO_CODE = 421;

	/** Mensaje - El nombre del espacio es nulo o vacío */
	public static final String ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE = "El nombre del espacio no puede ser nulo o vacío";

	/** Error - El espacio ya existe en sin docencia */
	public static final int ERR_ESPACIO_YA_EXISTE_EN_SIN_DOCENCIA_CODE = 422;

	/** Mensaje - El espacio ya existe en sin docencia */
	public static final String ERR_ESPACIO_YA_EXISTE_EN_SIN_DOCENCIA_MESSAGE = "Ya existe un espacio con ese nombre en este curso académico";

	/** Error - El espacio ya existe en desdoble */
	public static final int ERR_ESPACIO_YA_EXISTE_EN_DESDOBLE_CODE = 424;

	/** Mensaje - El espacio ya existe en desdoble */
	public static final String ERR_ESPACIO_YA_EXISTE_EN_DESDOBLE_MESSAGE = "Ya existe un espacio desdoble con ese nombre en este curso académico";

	/** Error - El espacio no existe en sin docencia */
	public static final int ERR_ESPACIO_NO_EXISTE_EN_SIN_DOCENCIA_CODE = 425;

	/** Mensaje - El espacio no existe en sin docencia */
	public static final String ERR_ESPACIO_NO_EXISTE_EN_SIN_DOCENCIA_MESSAGE = "El espacio no existe en sin docencia";

	/** Error - El espacio no existe en fijo */
	public static final int ERR_ESPACIO_NO_EXISTE_EN_FIJO_CODE = 426;

	/** Mensaje - El espacio no existe en fijo */
	public static final String ERR_ESPACIO_NO_EXISTE_EN_FIJO_MESSAGE = "El espacio no existe en fijo";

	/** Error - El espacio no existe en desdoble */
	public static final int ERR_ESPACIO_NO_EXISTE_EN_DESDOBLE_CODE = 427;

	/** Mensaje - El espacio no existe en desdoble */
	public static final String ERR_ESPACIO_NO_EXISTE_EN_DESDOBLE_MESSAGE = "El espacio no existe en desdoble";

	/** Error - Faltan datos del grupo (curso, etapa y grupo) para el espacio fijo */
	public static final int ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_CODE = 428;

	/** Mensaje - Faltan datos del grupo para el espacio fijo */
	public static final String ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_MESSAGE = "Debes indicar curso, etapa y grupo";

	/** Error - El grupo indicado para el espacio fijo no existe */
	public static final int ERR_ESPACIO_FIJO_GRUPO_NO_EXISTE_CODE = 429;

	/** Mensaje - El grupo indicado para el espacio fijo no existe */
	public static final String ERR_ESPACIO_FIJO_GRUPO_NO_EXISTE_MESSAGE = "El grupo indicado no existe";

	/** Error - El curso académico origen y destino son iguales */
	public static final int ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_CODE = 430;

	/** Mensaje - El curso académico origen y destino son iguales */
	public static final String ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_MESSAGE = "El curso académico origen y destino no pueden ser iguales";

	/** Error - El aula no está disponible para usarse como desdoble (no existe en el catálogo o ya es aula de referencia) */
	public static final int ERR_ESPACIO_NO_DISPONIBLE_DESDOBLE_CODE = 440;

	/** Mensaje - El aula no está disponible para usarse como desdoble */
	public static final String ERR_ESPACIO_NO_DISPONIBLE_DESDOBLE_MESSAGE = "El aula no está disponible para usarse como desdoble: no existe en el catálogo del instituto o ya está asignada como aula de referencia de un grupo";

	/** Error - El aula ya está usada como desdoble y no puede asignarse como aula de referencia (fijo) */
	public static final int ERR_ESPACIO_USADO_COMO_DESDOBLE_CODE = 441;

	/** Mensaje - El aula ya está usada como desdoble y no puede asignarse como aula de referencia (fijo) */
	public static final String ERR_ESPACIO_USADO_COMO_DESDOBLE_MESSAGE = "El aula ya está asignada como aula de desdoble a uno o varios bloques, por lo que no puede asignarse a la vez como aula de referencia de un grupo";

	/** Error - No se ha indicado la asignatura a la que asignar el aula de desdoble */
	public static final int ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_CODE = 442;

	/** Mensaje - No se ha indicado la asignatura a la que asignar el aula de desdoble */
	public static final String ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_MESSAGE = "Debes indicar la asignatura del bloque a la que asignar el aula de desdoble";

	/** Error - La asignatura indicada no pertenece al bloque */
	public static final int ERR_DESDOBLE_ASIGNATURA_NO_EN_BLOQUE_CODE = 443;

	/** Mensaje - La asignatura indicada no pertenece al bloque */
	public static final String ERR_DESDOBLE_ASIGNATURA_NO_EN_BLOQUE_MESSAGE = "La asignatura indicada no pertenece al bloque seleccionado";

	/** Error - Se ha superado el tope de aulas de desdoble del bloque (no más aulas que asignaturas) */
	public static final int ERR_DESDOBLE_TOPE_AULAS_CODE = 444;

	/** Mensaje - Se ha superado el tope de aulas de desdoble del bloque (no más aulas que asignaturas) */
	public static final String ERR_DESDOBLE_TOPE_AULAS_MESSAGE = "No se pueden asignar más aulas de desdoble que asignaturas tiene el bloque";

	/** Opción de copia - catálogo curso/etapa del curso académico */
	public static final String OPCION_COPIAR_CURSOS_ETAPAS = "cursos_etapas";

	/** Opción de copia - asignaturas (horas, bloques, flags) de los cursos/etapas del origen */
	public static final String OPCION_COPIAR_ASIGNATURAS = "asignaturas";

	/** Opción de copia - reducciones (con docencia) del curso académico origen */
	public static final String OPCION_COPIAR_REDUCCIONES = "reducciones";

	/**
	 * Prefijo común del nombre sintetizado de una reducción de tipo TUTORÍA. El nombre completo de la tutoría a
	 * nivel curso/etapa (plantilla cargada por CSV) es {@code "Tutoría <curso>º <etapa>"} (p. ej. "Tutoría 1º ESO");
	 * la tutoría por grupo añade además el grupo al final ({@code "Tutoría <curso>º <etapa> <grupo>"}). Se centraliza
	 * aquí para que el parseo CSV (que crea las plantillas) y la sincronización por grupo (que las materializa por
	 * grupo) compartan exactamente el mismo formato.
	 */
	public static final String PREFIJO_REDUCCION_TUTORIA = "Tutoría ";

	/** Error - No se ha seleccionado ninguna opción de copia */
	public static final int ERR_COPIAR_SIN_OPCIONES_CODE = 434;

	/** Mensaje - No se ha seleccionado ninguna opción de copia */
	public static final String ERR_COPIAR_SIN_OPCIONES_MESSAGE = "Selecciona al menos una opción de copia";

	/** Error - esoBachillerato es nulo en la creación de curso/etapa */
	public static final int ERR_ESO_BACHILLERATO_NULO_CODE = 431;

	/** Mensaje - esoBachillerato es nulo en la creación de curso/etapa */
	public static final String ERR_ESO_BACHILLERATO_NULO_MESSAGE = "El indicador esoBachillerato no puede ser nulo";

	/** Error - El curso y etapa ya existen */
	public static final int ERR_CURSO_ETAPA_YA_EXISTE_CODE = 432;

	/** Mensaje - El curso y etapa ya existen */
	public static final String ERR_CURSO_ETAPA_YA_EXISTE_MESSAGE = "El curso y etapa ya existen en la base de datos";

	/** Error - El curso y etapa no existen */
	public static final int ERR_CURSO_ETAPA_NO_EXISTE_CODE = 433;

	/** Mensaje - El curso y etapa no existen */
	public static final String ERR_CURSO_ETAPA_NO_EXISTE_MESSAGE = "El curso y etapa no existen en la base de datos";

	/** Error - El nombre del departamento es nulo o vacío */
	public static final int ERR_DEPARTAMENTO_NOMBRE_NULO_VACIO_CODE = 434;

	/** Mensaje - El nombre del departamento es nulo o vacío */
	public static final String ERR_DEPARTAMENTO_NOMBRE_NULO_VACIO_MESSAGE = "El nombre del departamento no puede ser nulo o vacío";

	/** Error - El departamento ya existe */
	public static final int ERR_DEPARTAMENTO_YA_EXISTE_CODE = 435;

	/** Mensaje - El departamento ya existe */
	public static final String ERR_DEPARTAMENTO_YA_EXISTE_MESSAGE = "El departamento ya existe en la base de datos";

	/** Error - El departamento no existe */
	public static final int ERR_DEPARTAMENTO_NO_EXISTE_CODE = 436;

	/** Mensaje - El departamento no existe */
	public static final String ERR_DEPARTAMENTO_NO_EXISTE_MESSAGE = "El departamento no existe en la base de datos";

	/** Error - Datos de asignatura ad-hoc inválidos (nombre/curso/etapa) */
	public static final int ERR_ASIGNATURA_AD_HOC_DATOS_INVALIDOS_CODE = 437;

	/** Mensaje - Datos de asignatura ad-hoc inválidos */
	public static final String ERR_ASIGNATURA_AD_HOC_DATOS_INVALIDOS_MESSAGE = "El nombre, curso y etapa de la asignatura ad-hoc no pueden ser nulos o vacíos";

	/** Error - La asignatura ad-hoc ya existe */
	public static final int ERR_ASIGNATURA_AD_HOC_YA_EXISTE_CODE = 438;

	/** Mensaje - La asignatura ad-hoc ya existe */
	public static final String ERR_ASIGNATURA_AD_HOC_YA_EXISTE_MESSAGE = "Ya existe una asignatura ad-hoc con ese nombre para el curso y etapa indicados";

	/** Error - La asignatura no es ad-hoc o no existe (no se puede borrar por esta vía) */
	public static final int ERR_ASIGNATURA_NO_AD_HOC_CODE = 439;

	/** Mensaje - La asignatura no es ad-hoc o no existe */
	public static final String ERR_ASIGNATURA_NO_AD_HOC_MESSAGE = "La asignatura no existe o no es ad-hoc, por lo que no puede borrarse por esta vía";

	/** Error - El fichero de carga de alumnos por grupo tiene un formato inválido */
	public static final int ERR_CARGA_ALUMNOS_FICHERO_FORMATO_CODE = 445;

	/** Mensaje - Formato inválido del fichero de carga de alumnos por grupo */
	public static final String ERR_CARGA_ALUMNOS_FICHERO_FORMATO_MESSAGE = "El fichero de alumnos tiene un formato inválido. Se espera por fila: nombreApellidos, curso etapa grupo";

	/** Error - Alumnos del fichero que no existen en los datos brutos del curso/etapa/curso académico */
	public static final int ERR_ALUMNOS_NO_EN_DATOS_BRUTOS_CODE = 446;

	/** Mensaje - Alumnos del fichero que no existen en los datos brutos */
	public static final String ERR_ALUMNOS_NO_EN_DATOS_BRUTOS_MESSAGE = "Los siguientes alumnos del fichero no existen en los datos brutos de matrícula y no se ha importado nada: ";
}
