package es.iesjandula.reaktor.school_manager_server.rest.espacios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoAcademicoRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST encargado de exponer el curso académico seleccionado en el sistema.
 */
@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/espacios/cursoAcademico")
public class CursoAcademicoController
{
	@Autowired
	private ICursoAcademicoRepository cursoAcademicoRepository;

	/**
	 * Obtiene el curso académico seleccionado actualmente en el sistema.
	 *
	 * @return una {@link ResponseEntity} con:
	 * - 200 (OK) y el curso académico seleccionado si existe.
	 * - 400 (BAD_REQUEST) si no hay ningún curso académico seleccionado.
	 * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_APLICACION_SCHOOL_BASE + "')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> obtenerCursoAcademicoSeleccionado()
	{
		try
		{
			// Obtenemos el curso académico seleccionado
			String cursoAcademico = this.cursoAcademicoRepository.obtenerCursoAcademicoSeleccionado();

			// Si no hay ningún curso académico seleccionado, lanzamos una excepción
			if (cursoAcademico == null || cursoAcademico.isEmpty())
			{
				log.error(Constants.ERR_NO_CURSO_ACADEMICO_SELECCIONADO_MESSAGE);
				throw new SchoolManagerServerException(Constants.ERR_NO_CURSO_ACADEMICO_SELECCIONADO_CODE, Constants.ERR_NO_CURSO_ACADEMICO_SELECCIONADO_MESSAGE);
			}

			// Devolvemos el curso académico seleccionado
			return ResponseEntity.ok().body(cursoAcademico);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudo obtener el curso académico seleccionado";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}
}
