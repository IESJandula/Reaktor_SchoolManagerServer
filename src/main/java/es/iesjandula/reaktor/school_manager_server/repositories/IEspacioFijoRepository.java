package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.dtos.EspacioFijoDto;
import es.iesjandula.reaktor.school_manager_server.models.EspacioFijo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacio;
import jakarta.transaction.Transactional;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link EspacioFijo}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite trabajar con la entidad {@link EspacioFijo}
 * y su clave primaria {@link IdEspacio}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IEspacioFijoRepository extends JpaRepository<EspacioFijo, IdEspacio>
{
	/**
	 * Obtiene todos los espacios fijos en formato DTO para un curso académico.
	 *
	 * @param cursoAcademico - El curso académico del que se desean obtener los espacios fijos.
	 * @return La lista de espacios fijos en formato DTO.
	 */
	@Query("""
			    SELECT new es.iesjandula.reaktor.school_manager_server.dtos.EspacioFijoDto(
			        e.espacioId.cursoAcademico,
			        e.espacioId.nombre,
			        e.cursoEtapaGrupo.idCursoEtapaGrupo.curso,
			        e.cursoEtapaGrupo.idCursoEtapaGrupo.etapa,
			        e.cursoEtapaGrupo.idCursoEtapaGrupo.grupo
			    )
			    FROM EspacioFijo e
			    WHERE e.espacioId.cursoAcademico = :cursoAcademico
			""")
	List<EspacioFijoDto> buscarPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Indica si un grupo (curso, etapa y grupo) está asignado a algún espacio fijo.
	 *
	 * @param curso - El curso del grupo.
	 * @param etapa - La etapa del grupo.
	 * @param grupo - El grupo.
	 * @return true si el grupo está asignado a un espacio fijo, false en caso contrario.
	 */
	@Query("""
			    SELECT COUNT(e) > 0
			    FROM EspacioFijo e
			    WHERE e.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso
			      AND e.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa
			      AND e.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo
			""")
	boolean existeGrupoAsignado(@Param("curso") Integer curso, @Param("etapa") String etapa, @Param("grupo") String grupo);

	/**
	 * Obtiene los espacios fijos asignados a un curso, etapa y grupo concretos dentro de un curso académico.
	 * <p>
	 * Se utiliza en la lógica de swap del aula de referencia para localizar el espacio que tenía asignado el grupo
	 * antes de asignarle uno nuevo.
	 *
	 * @param cursoAcademico - El curso académico.
	 * @param curso          - El curso del grupo.
	 * @param etapa          - La etapa del grupo.
	 * @param grupo          - El grupo.
	 * @return La lista de espacios fijos asignados al grupo indicado.
	 */
	@Query("""
			    SELECT e
			    FROM EspacioFijo e
			    WHERE e.espacioId.cursoAcademico = :cursoAcademico
			      AND e.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso
			      AND e.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa
			      AND e.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo
			""")
	List<EspacioFijo> buscarPorCursoEtapaGrupo(@Param("cursoAcademico") String cursoAcademico,
											   @Param("curso") Integer curso,
											   @Param("etapa") String etapa,
											   @Param("grupo") String grupo);

	/**
	 * Borra todos los espacios fijos asociados a un curso académico.
	 *
	 * @param cursoAcademico - El curso académico cuyos espacios fijos se desean borrar.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM EspacioFijo e WHERE e.espacioId.cursoAcademico = :cursoAcademico")
	void borrarPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);
}
