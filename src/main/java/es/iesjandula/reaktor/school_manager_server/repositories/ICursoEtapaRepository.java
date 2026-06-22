package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import jakarta.transaction.Transactional;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link CursoEtapa}.
 */
@Repository
public interface ICursoEtapaRepository extends JpaRepository<CursoEtapa, IdCursoEtapa>
{
	/**
	 * Obtiene el catálogo curso/etapa de un curso académico en formato DTO.
	 *
	 * @param cursoAcademico - El curso académico.
	 * @return La lista de cursos y etapas con {@code esBachillerato}.
	 */
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto(ce.idCursoEtapa.cursoAcademico, ce.idCursoEtapa.curso, ce.idCursoEtapa.etapa, null, null, ce.esoBachillerato) "
			+ "FROM CursoEtapa ce "
			+ "WHERE ce.idCursoEtapa.cursoAcademico = :cursoAcademico "
			+ "ORDER BY ce.idCursoEtapa.curso ASC, ce.idCursoEtapa.etapa ASC")
	List<CursoEtapaGrupoDto> findAllDtoByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Obtiene todos los cursos y etapas globales (matrículas/horarios).
	 *
	 * @param cursoAcademico - {@link es.iesjandula.reaktor.school_manager_server.utils.Constants#CURSO_ACADEMICO_GLOBAL}.
	 * @return Lista de cursos y etapas globales.
	 */
	List<CursoEtapa> findAllByIdCursoEtapaCursoAcademico(String cursoAcademico);

	/**
	 * Borra todo el catálogo curso/etapa de un curso académico.
	 *
	 * @param cursoAcademico - El curso académico.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM CursoEtapa ce WHERE ce.idCursoEtapa.cursoAcademico = :cursoAcademico")
	void deleteAllByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Método que busca todos los cursos/etapas/grupos globales sin grupos docentes asociados.
	 *
	 * @param cursoAcademicoGlobal - {@link es.iesjandula.reaktor.school_manager_server.utils.Constants#CURSO_ACADEMICO_GLOBAL}.
	 * @return Lista de cursos/etapas globales.
	 */
	@Query("SELECT ce FROM CursoEtapa ce "
			+ "WHERE ce.idCursoEtapa.cursoAcademico = :cursoAcademicoGlobal AND EXISTS ("
			+ "   SELECT 1 FROM CursoEtapaGrupo ceg2 "
			+ "   WHERE ceg2.idCursoEtapaGrupo.cursoAcademico = :cursoAcademicoGlobal "
			+ "         AND ceg2.idCursoEtapaGrupo.curso = ce.idCursoEtapa.curso "
			+ "         AND ceg2.idCursoEtapaGrupo.etapa = ce.idCursoEtapa.etapa "
			+ "   GROUP BY ceg2.idCursoEtapaGrupo.curso, ceg2.idCursoEtapaGrupo.etapa "
			+ "   HAVING COUNT(ceg2) = 1)")
	Optional<List<CursoEtapa>> buscarCursosEtapasSinCursosEtapasGrupo(@Param("cursoAcademicoGlobal") String cursoAcademicoGlobal);
}
