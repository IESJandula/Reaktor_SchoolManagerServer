package es.iesjandula.reaktor.school_manager_server.rest.espacios;

import java.util.List;
import java.util.Optional;

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
import es.iesjandula.reaktor.school_manager_server.dtos.CargaCsvResultDto;
import es.iesjandula.reaktor.school_manager_server.dtos.DepartamentoDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoAcademico;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDepartamento;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoAcademicoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver;
import es.iesjandula.reaktor.school_manager_server.services.manager.ParseoCsvConfiguracionBasicaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST encargado de la administración del catálogo de departamentos por curso académico.
 * <p>
 * Persistencia: {@link Departamento} con PK {@code (cursoAcademico, nombre)}. Las filas de catálogo usan el curso
 * académico real (p. ej. {@code 2025/2026}); además se mantiene una fila global ({@link Constants#CURSO_ACADEMICO_GLOBAL})
 * para compatibilidad con profesores, asignaturas y reducciones que referencian departamentos sin curso académico.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/espacios/admin/departamentos")
public class DepartamentosAdminController
{
	@Autowired
	private ICursoAcademicoRepository cursoAcademicoRepository;

	@Autowired
	private IDepartamentoRepository departamentoRepository;

	@Autowired
	private CursoAcademicoResolver cursoAcademicoResolver;

	@Autowired
	private ParseoCsvConfiguracionBasicaService parseoCsvConfiguracionBasicaService;

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> obtenerDepartamentos()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			List<DepartamentoDto> departamentosDto = this.departamentoRepository.findAllDtoByCursoAcademico(cursoAcademico);

			return ResponseEntity.ok(departamentosDto);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener los departamentos";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<?> crearDepartamento(@RequestBody DepartamentoDto departamentoDto)
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();
			this.validarDepartamentoDto(departamentoDto);

			IdDepartamento idDepartamento = new IdDepartamento(cursoAcademico, departamentoDto.getNombre());

			if (this.departamentoRepository.existsById(idDepartamento))
			{
				log.error(Constants.ERR_DEPARTAMENTO_YA_EXISTE_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_DEPARTAMENTO_YA_EXISTE_CODE, Constants.ERR_DEPARTAMENTO_YA_EXISTE_MESSAGE);
			}

			Departamento departamento = new Departamento();
			departamento.setCursoAcademico(cursoAcademico);
			departamento.setNombre(departamentoDto.getNombre());
			this.departamentoRepository.saveAndFlush(departamento);

			this.sincronizarDepartamentoGlobal(departamentoDto.getNombre());

			log.info("INFO - Departamento creado correctamente para el curso académico " + cursoAcademico);

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo crear el departamento";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Carga departamentos de forma masiva desde un fichero CSV de 1 columna (la primera fila es la cabecera y se
	 * ignora como dato; cada fila restante es el nombre de un departamento). Reutiliza las mismas reglas de
	 * persistencia que el alta manual de departamentos (fila por curso académico + sincronización de la fila
	 * global). El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía.
	 * La carga es idempotente: los departamentos ya existentes se omiten.
	 *
	 * @param archivoCsv el fichero CSV con los nombres de departamentos.
	 * @return una {@link ResponseEntity} con:
	 * - 200 (OK) y {@link CargaCsvResultDto} si el procesamiento finaliza correctamente.
	 * - 400 (Bad Request) si el archivo está vacío, no es .csv o no contiene filas de datos.
	 * - 500 (Internal Server Error) si ocurre un error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> cargarDepartamentosDesdeCsv(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv)
	{
		try
		{
			CargaCsvResultDto resultado = this.parseoCsvConfiguracionBasicaService.cargarDepartamentosDesdeCsv(archivoCsv);

			return ResponseEntity.ok().body(resultado);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron cargar los departamentos desde el CSV";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/copiar")
	public ResponseEntity<?> copiarDepartamentos(@RequestHeader(value = "cursoAcademicoOrigen") String cursoAcademicoOrigen,
												 @RequestHeader(value = "cursoAcademicoDestino") String cursoAcademicoDestino)
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

			List<DepartamentoDto> catalogoOrigen = this.departamentoRepository.findAllDtoByCursoAcademico(cursoAcademicoOrigen);

			for (DepartamentoDto departamentoDto : catalogoOrigen)
			{
				IdDepartamento idDepartamentoDestino = new IdDepartamento(cursoAcademicoDestino, departamentoDto.getNombre());

				if (!this.departamentoRepository.existsById(idDepartamentoDestino))
				{
					Departamento departamento = new Departamento();
					departamento.setCursoAcademico(cursoAcademicoDestino);
					departamento.setNombre(departamentoDto.getNombre());
					this.departamentoRepository.saveAndFlush(departamento);
				}

				this.sincronizarDepartamentoGlobal(departamentoDto.getNombre());
			}

			log.info("INFO - Departamentos copiados de " + cursoAcademicoOrigen + " a " + cursoAcademicoDestino);

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron copiar los departamentos";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/borrarTodos")
	public ResponseEntity<?> borrarTodosDepartamentos()
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();

			this.departamentoRepository.deleteAllByCursoAcademico(cursoAcademico);

			log.info("INFO - Departamentos borrados para el curso académico " + cursoAcademico);

			return ResponseEntity.ok().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron borrar los departamentos";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, consumes = "application/json")
	public ResponseEntity<?> borrarDepartamento(@RequestBody DepartamentoDto departamentoDto)
	{
		try
		{
			// El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
			String cursoAcademico = this.cursoAcademicoResolver.resolver();
			this.validarDepartamentoDto(departamentoDto);

			IdDepartamento idDepartamento = new IdDepartamento(cursoAcademico, departamentoDto.getNombre());

			if (!this.departamentoRepository.existsById(idDepartamento))
			{
				log.error(Constants.ERR_DEPARTAMENTO_NO_EXISTE_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_DEPARTAMENTO_NO_EXISTE_CODE, Constants.ERR_DEPARTAMENTO_NO_EXISTE_MESSAGE);
			}

			this.departamentoRepository.deleteById(idDepartamento);

			log.info("INFO - Departamento borrado correctamente");

			return ResponseEntity.noContent().build();
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo borrar el departamento";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	/**
	 * Mantiene un registro global del departamento para compatibilidad con profesores, asignaturas y reducciones.
	 *
	 * @param nombre - Nombre del departamento.
	 */
	private void sincronizarDepartamentoGlobal(String nombre)
	{
		IdDepartamento idDepartamentoGlobal = new IdDepartamento(Constants.CURSO_ACADEMICO_GLOBAL, nombre);

		if (!this.departamentoRepository.existsById(idDepartamentoGlobal))
		{
			Departamento departamentoGlobal = new Departamento(nombre);
			this.departamentoRepository.saveAndFlush(departamentoGlobal);
		}
	}

	private void validarDepartamentoDto(DepartamentoDto departamentoDto) throws SchoolManagerServerException
	{
		if (departamentoDto == null || departamentoDto.getNombre() == null || departamentoDto.getNombre().isEmpty())
		{
			log.error(Constants.ERR_DEPARTAMENTO_NOMBRE_NULO_VACIO_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_DEPARTAMENTO_NOMBRE_NULO_VACIO_CODE, Constants.ERR_DEPARTAMENTO_NOMBRE_NULO_VACIO_MESSAGE);
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
