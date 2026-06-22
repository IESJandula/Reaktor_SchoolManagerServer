package es.iesjandula.reaktor.school_manager_server.rest.espacios;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturasUnicasDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CargaCsvResultDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CopiarCursoAcademicoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto;
import es.iesjandula.reaktor.school_manager_server.services.manager.CopiarCursoAcademicoService;
import es.iesjandula.reaktor.school_manager_server.models.CursoAcademico;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoAcademicoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver;
import es.iesjandula.reaktor.school_manager_server.services.manager.ParseoCsvConfiguracionBasicaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST encargado de la administración del catálogo curso/etapa por curso académico.
 * <p>
 * Persistencia: {@link CursoEtapa} con PK {@code (cursoAcademico, curso, etapa)} y fila espejo en
 * {@link CursoEtapaGrupo} con grupo {@link Constants#GRUPO_CATALOGO_CURSO_ETAPA}. Los grupos docentes reales
 * (A, B, Optativas…) siguen usando {@link Constants#CURSO_ACADEMICO_GLOBAL} en su PK.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/espacios/admin/cursosEtapasGrupos")
public class CursosEtapasGruposAdminController
{
	@Autowired
	private ICursoAcademicoRepository cursoAcademicoRepository;

	@Autowired
	private ICursoEtapaRepository cursoEtapaRepository;

	@Autowired
	private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository;

	@Autowired
	private CopiarCursoAcademicoService copiarCursoAcademicoService;

	@Autowired
	private IAsignaturaRepository asignaturaRepository;

	@Autowired
	private IReduccionRepository reduccionRepository;

	@Autowired
	private CursoAcademicoResolver cursoAcademicoResolver;

	@Autowired
	private ParseoCsvConfiguracionBasicaService parseoCsvConfiguracionBasicaService;

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> obtenerCursosEtapasGrupos()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			List<CursoEtapaGrupoDto> cursosEtapasDto = this.cursoEtapaRepository.findAllDtoByCursoAcademico(cursoAcademico);

			return ResponseEntity.ok(cursosEtapasDto);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener los cursos y etapas";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Lista las asignaturas únicas (curso, etapa, nombre, horas) del curso académico activo. El curso académico se
	 * resuelve internamente (seleccionado = true); el cliente no lo envía. Se usa en el listado de la configuración
	 * básica al elegir la categoría "Asignaturas".
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
	public ResponseEntity<?> obtenerAsignaturasCursoAcademico()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			List<AsignaturasUnicasDto> asignaturas = this.asignaturaRepository.findAsignaturasUnicasByCursoAcademico(cursoAcademico);

			return ResponseEntity.ok(asignaturas);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener las asignaturas del curso académico";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Lista las reducciones visibles en el listado de la configuración básica del curso académico activo: las
	 * reducciones globales sin docencia (incluidas las cargadas por CSV de tutorías / no tutorías) y las que tienen
	 * docencia vinculada a un grupo de ese curso académico. El curso académico se resuelve internamente
	 * (seleccionado = true); el cliente no lo envía. Se usa en el listado de la configuración básica al elegir el
	 * modo "Reducciones".
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/reducciones")
	public ResponseEntity<?> obtenerReduccionesCursoAcademico()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			List<ReduccionDto> reducciones = this.reduccionRepository.findAllParaListadoByCursoAcademico(cursoAcademico).stream()
					.map((Reduccion reduccion) -> new ReduccionDto(
							reduccion.getIdReduccion().getNombre(),
							reduccion.getIdReduccion().getHoras(),
							reduccion.isDecideDireccion(),
							reduccion.getCursoEtapaGrupo()))
					.collect(Collectors.toList());

			return ResponseEntity.ok(reducciones);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener las reducciones del curso académico";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Borra TODAS las asignaturas del curso académico activo. El curso académico se resuelve internamente
	 * (seleccionado = true); el cliente no lo envía. Se usa en el "borrar todos" del catálogo de asignaturas de la
	 * configuración básica.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/asignaturas/borrarTodos")
	public ResponseEntity<?> borrarTodasAsignaturas()
	{
		try
		{
			// Solo se borran las asignaturas del curso académico activo (seleccionado = true)
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			this.asignaturaRepository.borrarTodasPorCursoAcademico(cursoAcademico);

			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron borrar todas las asignaturas del curso académico";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<?> crearCursoEtapaGrupo(@RequestBody CursoEtapaGrupoDto cursoEtapaGrupoDto)
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();
			this.validarCursoEtapaCreacionDto(cursoEtapaGrupoDto);

			IdCursoEtapa idCursoEtapa = this.validarCreacionCursoEtapa(cursoAcademico, cursoEtapaGrupoDto);

			CursoEtapa cursoEtapa = new CursoEtapa();
			cursoEtapa.setIdCursoEtapa(idCursoEtapa);
			cursoEtapa.setEsoBachillerato(Boolean.TRUE.equals(cursoEtapaGrupoDto.getEsBachillerato()));
			this.cursoEtapaRepository.saveAndFlush(cursoEtapa);

			this.crearCursoEtapaGrupoCatalogo(cursoAcademico, cursoEtapaGrupoDto);

			log.info("INFO - Curso y etapa creados correctamente para el curso académico " + cursoAcademico);

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo crear el curso y etapa";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Carga cursos y etapas de forma masiva desde un fichero CSV de 2 columnas (la primera fila es la cabecera y se
	 * ignora como dato): columna 1 = curso (entero), columna 2 = etapa (texto). Reutiliza las mismas reglas de
	 * persistencia que el alta manual de cursos y etapas. El curso académico activo se resuelve internamente
	 * (seleccionado = true); el cliente no lo envía. La carga es idempotente: los cursos/etapas ya existentes se omiten.
	 *
	 * @param archivoCsv el fichero CSV con curso y etapa por fila.
	 * @return una {@link ResponseEntity} con:
	 * - 200 (OK) y {@link CargaCsvResultDto} si el procesamiento finaliza correctamente.
	 * - 400 (Bad Request) si el archivo está vacío, no es .csv o no contiene filas de datos.
	 * - 500 (Internal Server Error) si ocurre un error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> cargarCursosEtapasDesdeCsv(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv)
	{
		try
		{
			CargaCsvResultDto resultado = this.parseoCsvConfiguracionBasicaService.cargarCursosEtapasDesdeCsv(archivoCsv);

			return ResponseEntity.ok().body(resultado);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron cargar los cursos y etapas desde el CSV";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/copiar", consumes = "application/json")
	public ResponseEntity<?> copiarCursosEtapasGrupos(@RequestHeader(value = "cursoAcademicoOrigen") String cursoAcademicoOrigen,
													  @RequestHeader(value = "cursoAcademicoDestino") String cursoAcademicoDestino,
													  @RequestBody CopiarCursoAcademicoDto copiarCursoAcademicoDto)
	{
		try
		{
			this.validarCursoAcademico(cursoAcademicoOrigen);
			this.validarCursoAcademico(cursoAcademicoDestino);

			if (cursoAcademicoOrigen.equals(cursoAcademicoDestino))
			{
				log.error(Constants.ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_CODE, Constants.ERR_CURSO_ACADEMICO_ORIGEN_DESTINO_IGUALES_MESSAGE);
			}

			this.copiarCursoAcademicoService.copiar(cursoAcademicoOrigen, cursoAcademicoDestino, copiarCursoAcademicoDto);

			log.info("INFO - Copia de " + cursoAcademicoOrigen + " a " + cursoAcademicoDestino + " completada con opciones " + copiarCursoAcademicoDto.getOpciones());

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron copiar los cursos y etapas";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/borrarTodos")
	public ResponseEntity<?> borrarTodosCursosEtapasGrupos()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			this.cursoEtapaGrupoRepository.deleteAllCatalogoByCursoAcademico(cursoAcademico);
			this.cursoEtapaRepository.deleteAllByCursoAcademico(cursoAcademico);

			log.info("INFO - Cursos y etapas borrados para el curso académico " + cursoAcademico);

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron borrar los cursos y etapas";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, consumes = "application/json")
	public ResponseEntity<?> borrarCursoEtapaGrupo(@RequestBody CursoEtapaGrupoDto cursoEtapaGrupoDto)
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();
			this.validarCursoEtapaIdentificadorDto(cursoEtapaGrupoDto);

			IdCursoEtapa idCursoEtapa = new IdCursoEtapa(cursoAcademico, cursoEtapaGrupoDto.getCurso(), cursoEtapaGrupoDto.getEtapa());

			if (!this.cursoEtapaRepository.existsById(idCursoEtapa))
			{
				log.error(Constants.ERR_CURSO_ETAPA_NO_EXISTE_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_CURSO_ETAPA_NO_EXISTE_CODE, Constants.ERR_CURSO_ETAPA_NO_EXISTE_MESSAGE);
			}

			this.borrarCursoEtapaCatalogo(cursoAcademico, cursoEtapaGrupoDto);

			log.info("INFO - Curso y etapa borrados correctamente");

			return ResponseEntity.noContent().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo borrar el curso y etapa";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	private void crearCursoEtapaGrupoCatalogo(String cursoAcademico, CursoEtapaGrupoDto cursoEtapaGrupoDto)
	{
		IdCursoEtapaGrupo idCursoEtapaGrupo = this.resolverIdCursoEtapaGrupoCatalogo(cursoAcademico, cursoEtapaGrupoDto);

		if (!this.cursoEtapaGrupoRepository.existsById(idCursoEtapaGrupo))
		{
			CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
			cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
			cursoEtapaGrupo.setEsoBachillerato(cursoEtapaGrupoDto.getEsBachillerato());
			this.cursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);
		}
	}

	private void borrarCursoEtapaCatalogo(String cursoAcademico, CursoEtapaGrupoDto cursoEtapaGrupoDto)
	{
		IdCursoEtapa idCursoEtapa = new IdCursoEtapa(cursoAcademico, cursoEtapaGrupoDto.getCurso(), cursoEtapaGrupoDto.getEtapa());
		IdCursoEtapaGrupo idCursoEtapaGrupo = this.resolverIdCursoEtapaGrupoCatalogo(cursoAcademico, cursoEtapaGrupoDto);

		if (this.cursoEtapaGrupoRepository.existsById(idCursoEtapaGrupo))
		{
			this.cursoEtapaGrupoRepository.deleteById(idCursoEtapaGrupo);
		}

		if (this.cursoEtapaRepository.existsById(idCursoEtapa))
		{
			this.cursoEtapaRepository.deleteById(idCursoEtapa);
		}
	}

	private IdCursoEtapaGrupo resolverIdCursoEtapaGrupoCatalogo(String cursoAcademico, CursoEtapaGrupoDto cursoEtapaGrupoDto)
	{
		String grupo = cursoEtapaGrupoDto.getGrupo();

		if (grupo == null || grupo.isEmpty())
		{
			grupo = Constants.GRUPO_CATALOGO_CURSO_ETAPA;
		}

		return new IdCursoEtapaGrupo(cursoAcademico, cursoEtapaGrupoDto.getCurso(), cursoEtapaGrupoDto.getEtapa(), grupo);
	}

	private IdCursoEtapa validarCreacionCursoEtapa(String cursoAcademico, CursoEtapaGrupoDto cursoEtapaGrupoDto) throws SchoolManagerServerException
	{
		IdCursoEtapa idCursoEtapa = new IdCursoEtapa(cursoAcademico, cursoEtapaGrupoDto.getCurso(), cursoEtapaGrupoDto.getEtapa());

		if (this.cursoEtapaRepository.existsById(idCursoEtapa))
		{
			log.error(Constants.ERR_CURSO_ETAPA_YA_EXISTE_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_CURSO_ETAPA_YA_EXISTE_CODE, Constants.ERR_CURSO_ETAPA_YA_EXISTE_MESSAGE);
		}

		return idCursoEtapa;
	}

	private void validarCursoEtapaCreacionDto(CursoEtapaGrupoDto cursoEtapaGrupoDto) throws SchoolManagerServerException
	{
		this.validarCursoEtapaIdentificadorDto(cursoEtapaGrupoDto);

		if (cursoEtapaGrupoDto.getEsBachillerato() == null)
		{
			log.error(Constants.ERR_ESO_BACHILLERATO_NULO_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_ESO_BACHILLERATO_NULO_CODE, Constants.ERR_ESO_BACHILLERATO_NULO_MESSAGE);
		}
	}

	private void validarCursoEtapaIdentificadorDto(CursoEtapaGrupoDto cursoEtapaGrupoDto) throws SchoolManagerServerException
	{
		if (cursoEtapaGrupoDto.getEtapa() == null || cursoEtapaGrupoDto.getEtapa().isEmpty())
		{
			log.error(Constants.ERR_ETAPA_NULO_VACIO_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_ETAPA_NULO_VACIO_CODE, Constants.ERR_ETAPA_NULO_VACIO_MESSAGE);
		}
	}

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
