package es.iesjandula.reaktor.school_manager_server.services.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.repositories.ICursoAcademicoRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio encargado de resolver internamente el curso académico activo sobre el que opera una petición.
 * <p>
 * La fuente única es el curso académico marcado como seleccionado (columna {@code seleccionado = true}) en la base
 * de datos. De este modo el frontend NO envía el curso académico en ninguna petición: el backend lo deduce siempre
 * del estado del sistema, reutilizando la misma consulta que emplea {@code CursoAcademicoController}
 * ({@link ICursoAcademicoRepository#obtenerCursoAcademicoSeleccionado()}, que filtra por {@code seleccionado = true}).
 * </p>
 * <p>
 * Como la aplicación se configura una vez al año por una sola persona (sin concurrencia multi-usuario), mantener
 * este estado en el servidor es aceptable. El curso activo se cambia exclusivamente desde el endpoint selector
 * ({@code POST /schoolManager/espacios/admin/cursosAcademicos}).
 * </p>
 */
@Slf4j
@Service
public class CursoAcademicoResolver
{
	@Autowired
	private ICursoAcademicoRepository cursoAcademicoRepository;

	/**
	 * Resuelve internamente el curso académico activo (seleccionado = true) del sistema.
	 *
	 * @return el curso académico seleccionado en BBDD.
	 * @throws SchoolManagerServerException si no hay ningún curso académico seleccionado.
	 */
	public String resolver() throws SchoolManagerServerException
	{
		// Fuente única: el curso académico activo (seleccionado = true) en BBDD
		String cursoAcademicoSeleccionado = this.cursoAcademicoRepository.obtenerCursoAcademicoSeleccionado();

		if (cursoAcademicoSeleccionado == null || cursoAcademicoSeleccionado.isEmpty())
		{
			log.error(Constants.ERR_NO_CURSO_ACADEMICO_SELECCIONADO_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_NO_CURSO_ACADEMICO_SELECCIONADO_CODE, Constants.ERR_NO_CURSO_ACADEMICO_SELECCIONADO_MESSAGE);
		}

		return cursoAcademicoSeleccionado;
	}
}
