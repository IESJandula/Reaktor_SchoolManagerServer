package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.CursoAcademicoDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoAcademico;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link CursoAcademico}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite trabajar con la entidad {@link CursoAcademico}
 * y su clave primaria {@link Long}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface ICursoAcademicoRepository extends JpaRepository<CursoAcademico, Long>
{
	/**
	 * Obtiene un curso académico por su cadena de curso académico.
	 *
	 * @param cursoAcademico - El curso académico a buscar.
	 * @return El curso académico encontrado.
	 */
	@Query("SELECT c FROM CursoAcademico c WHERE c.cursoAcademico = :cursoAcademico")
	Optional<CursoAcademico> findByCursoAcademico(String cursoAcademico);

	/**
	 * Obtiene todos los cursos académicos en formato DTO.
	 *
	 * @return La lista de cursos académicos en formato DTO.
	 */
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoAcademicoDto(c.cursoAcademico, c.seleccionado) "
			+ "FROM CursoAcademico c "
			+ "ORDER BY c.cursoAcademico ASC")
	List<CursoAcademicoDto> findAllDto();

	/**
	 * Deselecciona todos los cursos académicos.
	 */
	@Transactional
	@Modifying
	@Query("UPDATE CursoAcademico SET seleccionado = false")
	void deseleccionarTodosLosCursosAcademicos();

	/**
	 * Obtiene el curso académico seleccionado.
	 *
	 * @return El curso académico seleccionado en formato String.
	 */
	@Query("SELECT c.cursoAcademico FROM CursoAcademico c WHERE c.seleccionado = true")
	String obtenerCursoAcademicoSeleccionado();
}
