package es.iesjandula.reaktor.school_manager_server.rest.espacios;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.CargaCsvResultDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoAcademicoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDesdobleDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioFijoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioSinDocenciaDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoAcademico;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.EspacioFijo;
import es.iesjandula.reaktor.school_manager_server.models.EspacioSinDocencia;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacio;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoAcademicoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioDesdobleRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioFijoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioSinDocenciaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IOcupaEspacioDesdobleRepository;
import es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver;
import es.iesjandula.reaktor.school_manager_server.services.manager.ParseoCsvConfiguracionBasicaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST encargado de la administración de cursos académicos y espacios
 * (sin docencia, fijo y desdoble).
 */
@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/espacios/admin")
public class EspaciosAdminController
{
	/** Repositorio de cursos académicos */
	@Autowired
	private ICursoAcademicoRepository cursoAcademicoRepository;

	/** Repositorio de curso etapa grupo */
	@Autowired
	private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository;

	/** Repositorio de espacios sin docencia */
	@Autowired
	private IEspacioSinDocenciaRepository espacioSinDocenciaRepository;

	/** Repositorio de espacios fijos */
	@Autowired
	private IEspacioFijoRepository espacioFijoRepository;

	/** Repositorio de espacios desdobles */
	@Autowired
	private IEspacioDesdobleRepository espacioDesdobleRepository;

	/** Repositorio de ocupaciones de espacios desdoble (relación grupo - espacio desdoble) */
	@Autowired
	private IOcupaEspacioDesdobleRepository ocupaEspacioDesdobleRepository;

	/** Resolutor del curso académico efectivo (elegido por defecto, cabecera como override) */
	@Autowired
	private CursoAcademicoResolver cursoAcademicoResolver;

	/** Servicio de carga por fichero CSV de la configuración básica */
	@Autowired
	private ParseoCsvConfiguracionBasicaService parseoCsvConfiguracionBasicaService;

	/**
	 * Obtiene la lista de cursos académicos.
	 *
	 * @return una {@link ResponseEntity} con la lista de cursos académicos o un error genérico.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/cursosAcademicos")
	public ResponseEntity<?> obtenerCursosAcademicos()
	{
		try
		{
			List<CursoAcademicoDto> cursosAcademicosDto = this.cursoAcademicoRepository.findAllDto();

			return ResponseEntity.ok(cursosAcademicosDto);
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener los cursos académicos";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Selecciona un curso académico como el activo del sistema.
	 *
	 * @param cursoAcademico - El curso académico a seleccionar (cabecera).
	 * @return una {@link ResponseEntity} con el resultado de la operación.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/cursosAcademicos")
	public ResponseEntity<?> seleccionarCursoAcademico(@RequestHeader(value = "cursoAcademico") String cursoAcademico)
	{
		try
		{
			// Validamos el curso académico
			CursoAcademico cursoAcademicoEntity = this.validarCursoAcademico(cursoAcademico);

			// Quitamos la selección de todos los cursos académicos
			this.cursoAcademicoRepository.deseleccionarTodosLosCursosAcademicos();

			// Seteamos el curso académico seleccionado
			cursoAcademicoEntity.setSeleccionado(true);

			// Guardamos el curso académico en la base de datos
			this.cursoAcademicoRepository.saveAndFlush(cursoAcademicoEntity);

			log.info("INFO - Curso académico seleccionado correctamente");

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo seleccionar el curso académico";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Crea un espacio sin docencia a partir del DTO.
	 *
	 * @param espacioSinDocenciaDto - El DTO del espacio sin docencia a crear.
	 * @return una {@link ResponseEntity} con el resultado de la operación.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/espacios/sinDocencia", consumes = "application/json")
	public ResponseEntity<?> crearEspacioSinDocencia(@RequestBody EspacioSinDocenciaDto espacioSinDocenciaDto)
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			espacioSinDocenciaDto.setCursoAcademico(this.cursoAcademicoResolver.resolver());

			// Validamos el DTO del espacio
			this.validarEspacioDto(espacioSinDocenciaDto);

			IdEspacio idEspacio = new IdEspacio(espacioSinDocenciaDto.getCursoAcademico(), espacioSinDocenciaDto.getNombre());

			Optional<EspacioSinDocencia> existente = this.espacioSinDocenciaRepository.findById(idEspacio);

			if (existente.isPresent())
			{
				log.error(Constants.ERR_ESPACIO_YA_EXISTE_EN_SIN_DOCENCIA_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_ESPACIO_YA_EXISTE_EN_SIN_DOCENCIA_CODE, Constants.ERR_ESPACIO_YA_EXISTE_EN_SIN_DOCENCIA_MESSAGE);
			}

			// Si existe como fijo, lo borramos para romper la relación con el grupo (vuelve a estar disponible)
			if (this.espacioFijoRepository.existsById(idEspacio))
			{
				this.espacioFijoRepository.deleteById(idEspacio);
			}

			// El uso como desdoble NO se toca: un aula del catálogo (sin docencia) puede estar asignada a la vez como
			// desdoble a uno o varios bloques (el desdoble no consume el aula).

			EspacioSinDocencia espacio = new EspacioSinDocencia();
			espacio.setEspacioId(idEspacio);
			this.espacioSinDocenciaRepository.saveAndFlush(espacio);

			log.info("INFO - Espacio sin docencia creado correctamente");

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo crear el espacio sin docencia";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Carga espacios (aulas) de forma masiva desde un fichero CSV de 1 columna (la primera fila es la cabecera y se
	 * ignora como dato; cada fila restante es el nombre de un espacio). Reutiliza las mismas reglas de persistencia
	 * que el alta manual de espacios sin docencia. El curso académico activo se resuelve internamente
	 * (seleccionado = true); el cliente no lo envía. La carga es idempotente: los espacios ya existentes se omiten.
	 *
	 * @param archivoCsv el fichero CSV con los nombres de espacios.
	 * @return una {@link ResponseEntity} con:
	 * - 200 (OK) y {@link CargaCsvResultDto} si el procesamiento finaliza correctamente.
	 * - 400 (Bad Request) si el archivo está vacío, no es .csv o no contiene filas de datos.
	 * - 500 (Internal Server Error) si ocurre un error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/espacios/sinDocencia/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> cargarEspaciosDesdeCsv(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv)
	{
		try
		{
			CargaCsvResultDto resultado = this.parseoCsvConfiguracionBasicaService.cargarEspaciosDesdeCsv(archivoCsv);

			return ResponseEntity.ok().body(resultado);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron cargar los espacios desde el CSV";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Obtiene el catálogo COMPLETO de espacios del instituto para el curso académico activo: tanto los espacios
	 * disponibles (sin docencia) como los que ya están asignados como aula fija o como aula de desdoble.
	 * <p>
	 * La ventana de "configuración básica" muestra siempre todos los espacios del instituto, estén libres o
	 * asignados. El catálogo de aulas se reparte entre "sin docencia" (disponibles) y "fijo" (asignadas como aula de
	 * referencia), que es lo único que consume un aula: al asignar un fijo el aula se mueve de sin docencia a fijo.
	 * El uso como desdoble NO mueve el aula de tabla (un aula de desdoble permanece en sin docencia), por lo que la
	 * unión de sin docencia + fijo ya cubre todo el catálogo; se añade además la consulta de desdoble (nombres
	 * distintos) de forma defensiva y se deduplica por nombre. El desplegable de "disponibles" (creación de grupos)
	 * sigue mostrando solo las aulas no asignadas como fijo.
	 *
	 * @return una {@link ResponseEntity} con la lista del catálogo completo de espacios.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/espacios/sinDocencia")
	public ResponseEntity<?> obtenerEspaciosSinDocencia()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			// Unimos las tres tablas (sin docencia + fijo + desdoble) deduplicando por nombre para obtener el
			// catálogo completo de espacios del instituto del curso académico.
			Map<String, EspacioSinDocenciaDto> espaciosPorNombre = new LinkedHashMap<>();

			for (EspacioSinDocenciaDto espacioSinDocencia : this.espacioSinDocenciaRepository.buscarPorCursoAcademico(cursoAcademico))
			{
				espaciosPorNombre.putIfAbsent(espacioSinDocencia.getNombre(), espacioSinDocencia);
			}

			for (EspacioFijoDto espacioFijo : this.espacioFijoRepository.buscarPorCursoAcademico(cursoAcademico))
			{
				espaciosPorNombre.putIfAbsent(espacioFijo.getNombre(), new EspacioSinDocenciaDto(cursoAcademico, espacioFijo.getNombre()));
			}

			for (EspacioDesdobleDto espacioDesdoble : this.espacioDesdobleRepository.buscarPorCursoAcademico(cursoAcademico))
			{
				espaciosPorNombre.putIfAbsent(espacioDesdoble.getNombre(), new EspacioSinDocenciaDto(cursoAcademico, espacioDesdoble.getNombre()));
			}

			List<EspacioSinDocenciaDto> espaciosSinDocenciaDto = new ArrayList<>(espaciosPorNombre.values());

			return ResponseEntity.ok(espaciosSinDocenciaDto);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener los espacios sin docencia";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Borra un espacio sin docencia a partir del DTO.
	 *
	 * @param espacioSinDocenciaDto - El DTO del espacio sin docencia a borrar.
	 * @return una {@link ResponseEntity} con el resultado de la operación.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/espacios/sinDocencia", consumes = "application/json")
	public ResponseEntity<?> borrarEspacioSinDocencia(@RequestBody EspacioSinDocenciaDto espacioSinDocenciaDto)
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			espacioSinDocenciaDto.setCursoAcademico(this.cursoAcademicoResolver.resolver());

			// Validamos el DTO del espacio
			this.validarEspacioDto(espacioSinDocenciaDto);

			IdEspacio idEspacio = new IdEspacio(espacioSinDocenciaDto.getCursoAcademico(), espacioSinDocenciaDto.getNombre());

			// Configuración básica gestiona el catálogo completo. Un aula vive en el catálogo como disponible (sin
			// docencia) o como aula de referencia (fijo); además puede tener asignaciones de desdoble a uno o varios
			// bloques. Al borrar el aula del catálogo eliminamos su registro de catálogo (sin docencia o fijo) y,
			// adicionalmente, todas sus asignaciones de desdoble (relación espacio ↔ bloque).
			boolean borrado = false;

			if (this.espacioSinDocenciaRepository.existsById(idEspacio))
			{
				this.espacioSinDocenciaRepository.deleteById(idEspacio);
				borrado = true;
			}
			else if (this.espacioFijoRepository.existsById(idEspacio))
			{
				this.espacioFijoRepository.deleteById(idEspacio);
				borrado = true;
			}

			// Borramos también las asignaciones de desdoble del aula, si las hubiera
			if (this.espacioDesdobleRepository.existePorEspacio(idEspacio.getCursoAcademico(), idEspacio.getNombre()))
			{
				this.espacioDesdobleRepository.borrarPorEspacio(idEspacio.getCursoAcademico(), idEspacio.getNombre());
				borrado = true;
			}

			if (!borrado)
			{
				log.error(Constants.ERR_ESPACIO_NO_EXISTE_EN_SIN_DOCENCIA_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NO_EXISTE_EN_SIN_DOCENCIA_CODE, Constants.ERR_ESPACIO_NO_EXISTE_EN_SIN_DOCENCIA_MESSAGE);
			}

			log.info("INFO - Espacio borrado correctamente del catálogo");

			return ResponseEntity.noContent().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo borrar el espacio sin docencia";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Borra TODOS los espacios del catálogo del instituto asociados a un curso académico.
	 * <p>
	 * Como la ventana de configuración básica muestra el catálogo completo (espacios disponibles y ya asignados),
	 * "borrar todos" elimina los espacios de las tres tablas: sin docencia, fijo y desdoble. Antes de borrar los
	 * desdoble se eliminan sus ocupaciones (relación grupo - espacio desdoble) para no violar la clave foránea. La
	 * operación es transaccional para que el borrado sea atómico.
	 *
	 * @param cursoAcademico - El curso académico (cabecera).
	 * @return una {@link ResponseEntity} con:
	 * - 200 (OK) si el borrado se realiza correctamente.
	 * - 400 (BAD_REQUEST) si el curso académico no existe.
	 * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
	 */
	@Transactional
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/espacios/borrarTodos")
	public ResponseEntity<?> borrarTodosEspaciosSinDocencia()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			// Borramos las ocupaciones de desdoble primero (FK) y después los espacios de las tres tablas
			this.ocupaEspacioDesdobleRepository.borrarPorCursoAcademico(cursoAcademico);
			this.espacioDesdobleRepository.borrarPorCursoAcademico(cursoAcademico);
			this.espacioFijoRepository.borrarPorCursoAcademico(cursoAcademico);
			this.espacioSinDocenciaRepository.borrarPorCursoAcademico(cursoAcademico);

			log.info("INFO - Espacios del catálogo borrados correctamente para el curso académico " + cursoAcademico);

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron borrar los espacios sin docencia";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Obtiene la lista de espacios fijos de un curso académico.
	 *
	 * @param cursoAcademico - El curso académico (cabecera).
	 * @return una {@link ResponseEntity} con la lista de espacios fijos.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/espacios/fijo")
	public ResponseEntity<?> obtenerEspaciosFijo()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			List<EspacioFijoDto> espaciosFijoDto = this.espacioFijoRepository.buscarPorCursoAcademico(cursoAcademico);

			return ResponseEntity.ok(espaciosFijoDto);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener los espacios fijos";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Obtiene la lista de aulas usadas como desdoble (nombres distintos) de un curso académico.
	 * <p>
	 * Con el nuevo modelo, "desdoble" es una RELACIÓN espacio ↔ bloque (un mismo aula puede ser desdoble de varios
	 * bloques), por lo que aquí se devuelven los nombres distintos de las aulas que actúan como desdoble. Lo usan
	 * las vistas de automatizaciones para poblar listas de ubicaciones (deduplican junto a fijo y sin docencia).
	 * <p>
	 * La administración del catálogo de espacios (alta/baja) se hace mediante "sin docencia"; las altas/bajas de
	 * desdoble dejaron de gestionarse aquí porque el desdoble ya no es un "tipo" de espacio del catálogo, sino una
	 * asignación a bloques que se gestiona en la ventana de Creación de grupos.
	 *
	 * @return una {@link ResponseEntity} con la lista de aulas de desdoble (distintas).
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/espacios/desdoble")
	public ResponseEntity<?> obtenerEspaciosDesdoble()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			List<EspacioDesdobleDto> espaciosDesdobleDto = this.espacioDesdobleRepository.buscarPorCursoAcademico(cursoAcademico);

			return ResponseEntity.ok(espaciosDesdobleDto);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener los espacios desdobles";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Copia los espacios (sin docencia, fijo y desdoble) de un curso académico origen a un curso académico destino.
	 * <p>
	 * Se generan nuevas entidades para el curso académico destino, sin modificar ni mover las del origen. Para
	 * evitar duplicados, los espacios que ya existan en el destino con el mismo nombre se omiten (la operación es
	 * idempotente: puede ejecutarse varias veces sin crear duplicados).
	 * <p>
	 * En el caso de los espacios fijos, la asociación con el grupo (curso, etapa y grupo) es independiente del curso
	 * académico, por lo que se reutiliza el mismo {@link CursoEtapaGrupo} en el destino.
	 *
	 * @param cursoAcademicoOrigen  - El curso académico del que se copian los espacios (cabecera).
	 * @param cursoAcademicoDestino - El curso académico al que se copian los espacios (cabecera).
	 * @return una {@link ResponseEntity} con:
	 * - 200 (OK) si la copia se realiza correctamente.
	 * - 400 (BAD_REQUEST) si algún curso académico no existe o si origen y destino son iguales.
	 * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/espacios/copiar")
	public ResponseEntity<?> copiarEspacios(@RequestHeader(value = "cursoAcademicoOrigen") String cursoAcademicoOrigen,
											@RequestHeader(value = "cursoAcademicoDestino") String cursoAcademicoDestino)
	{
		try
		{
			// Validamos que ambos cursos académicos existan
			this.validarCursoAcademico(cursoAcademicoOrigen);
			this.validarCursoAcademico(cursoAcademicoDestino);

			// Validamos que origen y destino no sean iguales
			if (cursoAcademicoOrigen.equals(cursoAcademicoDestino))
			{
				log.error(Constants.ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_CODE, Constants.ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_MESSAGE);
			}

			// Copiamos los espacios sin docencia del origen al destino
			List<EspacioSinDocenciaDto> espaciosSinDocencia = this.espacioSinDocenciaRepository.buscarPorCursoAcademico(cursoAcademicoOrigen);
			for (EspacioSinDocenciaDto espacioSinDocenciaDto : espaciosSinDocencia)
			{
				IdEspacio idEspacioDestino = new IdEspacio(cursoAcademicoDestino, espacioSinDocenciaDto.getNombre());

				// Evitamos duplicados: solo copiamos si no existe ya en el destino
				if (!this.espacioSinDocenciaRepository.existsById(idEspacioDestino))
				{
					EspacioSinDocencia espacioSinDocencia = new EspacioSinDocencia();
					espacioSinDocencia.setEspacioId(idEspacioDestino);
					this.espacioSinDocenciaRepository.saveAndFlush(espacioSinDocencia);
				}
			}

			// Copiamos los espacios fijos del origen al destino
			List<EspacioFijoDto> espaciosFijo = this.espacioFijoRepository.buscarPorCursoAcademico(cursoAcademicoOrigen);
			for (EspacioFijoDto espacioFijoDto : espaciosFijo)
			{
				IdEspacio idEspacioDestino = new IdEspacio(cursoAcademicoDestino, espacioFijoDto.getNombre());

				// Evitamos duplicados: solo copiamos si no existe ya en el destino
				if (!this.espacioFijoRepository.existsById(idEspacioDestino))
				{
					// El grupo docente es por curso académico: usamos el grupo del curso académico destino
					IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademicoDestino, espacioFijoDto.getCurso(), espacioFijoDto.getEtapa(), espacioFijoDto.getGrupo());

					Optional<CursoEtapaGrupo> cursoEtapaGrupoOptional = this.cursoEtapaGrupoRepository.findById(idCursoEtapaGrupo);

					// Solo copiamos el espacio fijo si el grupo asociado sigue existiendo
					if (cursoEtapaGrupoOptional.isPresent())
					{
						EspacioFijo espacioFijo = new EspacioFijo();
						espacioFijo.setEspacioId(idEspacioDestino);
						espacioFijo.setCursoEtapaGrupo(cursoEtapaGrupoOptional.get());
						this.espacioFijoRepository.saveAndFlush(espacioFijo);
					}
				}
			}

			// Las asignaciones de desdoble (relación espacio ↔ bloque) NO se copian entre cursos académicos: están
			// ligadas a los bloques concretos de un año (que se regeneran por curso académico), por lo que copiarlas
			// apuntaría a bloques del año origen. El catálogo de aulas sí se copia (arriba, vía sin docencia/fijo) y
			// el desdoble se reasigna a los nuevos bloques desde la ventana de Creación de grupos.

			log.info("INFO - Espacios copiados correctamente del curso académico " + cursoAcademicoOrigen + " al curso académico " + cursoAcademicoDestino);

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron copiar los espacios entre cursos académicos";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Valida que el DTO del espacio tenga curso académico y nombre no nulos ni vacíos.
	 *
	 * @param espacioDto - El DTO del espacio a validar.
	 * @return El curso académico encontrado.
	 * @throws SchoolManagerServerException si el espacio es nulo o vacío.
	 */
	private CursoAcademico validarEspacioDto(EspacioDto espacioDto) throws SchoolManagerServerException
	{
		// Validamos el curso académico
		CursoAcademico cursoAcademicoEntity = this.validarCursoAcademico(espacioDto.getCursoAcademico());

		// Validamos el nombre
		if (espacioDto.getNombre() == null || espacioDto.getNombre().isEmpty())
		{
			log.error(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_CODE, Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
		}

		return cursoAcademicoEntity;
	}

	/**
	 * Valida que el curso académico no sea nulo ni vacío y exista en la base de datos.
	 *
	 * @param cursoAcademico - El curso académico a validar.
	 * @return El curso académico encontrado.
	 * @throws SchoolManagerServerException si el curso académico es nulo, vacío o no existe.
	 */
	private CursoAcademico validarCursoAcademico(String cursoAcademico) throws SchoolManagerServerException
	{
		if (cursoAcademico == null || cursoAcademico.isEmpty())
		{
			log.error(Constants.ERR_CURSO_ACADEMICO_NULO_VACIO_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_CURSO_ACADEMICO_NULO_VACIO_CODE, Constants.ERR_CURSO_ACADEMICO_NULO_VACIO_MESSAGE);
		}

		Optional<CursoAcademico> cursoAcademicoEntity = this.cursoAcademicoRepository.findByCursoAcademico(cursoAcademico);

		if (cursoAcademicoEntity.isEmpty())
		{
			log.error(Constants.ERR_CURSO_ACADEMICO_NO_EXISTE_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_CURSO_ACADEMICO_NO_EXISTE_CODE, Constants.ERR_CURSO_ACADEMICO_NO_EXISTE_MESSAGE);
		}

		return cursoAcademicoEntity.get();
	}
}
