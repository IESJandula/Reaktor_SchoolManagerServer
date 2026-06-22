package es.iesjandula.reaktor.school_manager_server.rest.espacios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST con operaciones comunes de consulta para la gestión de espacios.
 */
@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/espacios/common")
public class EspaciosCommonController
{
	@Autowired
	private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository;

	@Autowired
	private es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver cursoAcademicoResolver;

	/**
	 * Obtiene la lista de cursos, etapas y grupos.
	 *
	 * @return una {@link ResponseEntity} con:
	 * - 200 (OK) y la lista de cursos, etapas y grupos.
	 * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/cursosEtapasGrupos")
	public ResponseEntity<?> obtenerCursosEtapasGrupos()
	{
		try
		{
			// Obtenemos todos los cursos, etapas y grupos del curso académico activo en formato DTO
			String cursoAcademico = this.cursoAcademicoResolver.resolver();
			List<CursoEtapaGrupoDto> cursosEtapasGruposDto = this.cursoEtapaGrupoRepository.findAllDto(cursoAcademico);

			// Devolvemos la respuesta
			return ResponseEntity.ok(cursosEtapasGruposDto);
		}
		catch (Exception exception)
		{
			String mensajeError = "ERROR - No se pudieron obtener los cursos, etapas y grupos";
			log.error(mensajeError, exception);

			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}
}
