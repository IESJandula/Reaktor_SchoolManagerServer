package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDisponibleDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioSinDocenciaDto;
import es.iesjandula.reaktor.school_manager_server.models.EspacioSinDocencia;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacio;
import jakarta.transaction.Transactional;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link EspacioSinDocencia}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite trabajar con la entidad {@link EspacioSinDocencia}
 * y su clave primaria {@link IdEspacio}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IEspacioSinDocenciaRepository extends JpaRepository<EspacioSinDocencia, IdEspacio>
{
	/**
	 * Obtiene todos los espacios sin docencia en formato DTO para un curso académico.
	 *
	 * @param cursoAcademico - El curso académico del que se desean obtener los espacios sin docencia.
	 * @return La lista de espacios sin docencia en formato DTO.
	 */
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.EspacioSinDocenciaDto(e.espacioId.cursoAcademico, e.espacioId.nombre) "
			+ "FROM EspacioSinDocencia e "
			+ "WHERE e.espacioId.cursoAcademico = :cursoAcademico")
	List<EspacioSinDocenciaDto> buscarPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Obtiene los espacios disponibles (catálogo del instituto sin asignar a ningún grupo) para un curso académico.
	 * <p>
	 * Los espacios "sin docencia" representan el catálogo de espacios que todavía no se han asignado como aula de
	 * referencia a un curso, etapa y grupo. Cuando un espacio se asigna a un grupo pasa a ser un espacio fijo (y se
	 * elimina de esta tabla), por lo que esta consulta devuelve únicamente los espacios disponibles.
	 *
	 * @param cursoAcademico - El curso académico del que se desean obtener los espacios disponibles.
	 * @return La lista de espacios disponibles en formato DTO, ordenada por nombre.
	 */
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.EspacioDisponibleDto(e.espacioId.cursoAcademico, e.espacioId.nombre) "
			+ "FROM EspacioSinDocencia e "
			+ "WHERE e.espacioId.cursoAcademico = :cursoAcademico "
			+ "ORDER BY e.espacioId.nombre ASC")
	List<EspacioDisponibleDto> buscarDisponiblesPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Borra todos los espacios sin docencia asociados a un curso académico.
	 *
	 * @param cursoAcademico - El curso académico cuyos espacios sin docencia se desean borrar.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM EspacioSinDocencia e WHERE e.espacioId.cursoAcademico = :cursoAcademico")
	void borrarPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);
}
