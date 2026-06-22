package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDesdobleAsignadoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDesdobleDto;
import es.iesjandula.reaktor.school_manager_server.models.EspacioDesdoble;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacioDesdoble;
import jakarta.transaction.Transactional;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link EspacioDesdoble}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite trabajar con la entidad {@link EspacioDesdoble}
 * y su clave primaria {@link IdEspacioDesdoble} (espacio + bloque).
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IEspacioDesdobleRepository extends JpaRepository<EspacioDesdoble, IdEspacioDesdoble>
{
	/**
	 * Obtiene los nombres DISTINTOS de las aulas usadas como desdoble en un curso académico.
	 * <p>
	 * Como un mismo espacio puede estar asignado a varios bloques, se deduplican por nombre para devolver el
	 * catálogo de aulas que actúan como desdoble (sin repetir).
	 *
	 * @param cursoAcademico - El curso académico del que se desean obtener las aulas de desdoble.
	 * @return La lista de aulas de desdoble (distintas) en formato DTO.
	 */
	@Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.EspacioDesdobleDto(e.idEspacioDesdoble.espacioId.cursoAcademico, e.idEspacioDesdoble.espacioId.nombre) "
			+ "FROM EspacioDesdoble e "
			+ "WHERE e.idEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico")
	List<EspacioDesdobleDto> buscarPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Obtiene las asignaciones de aulas de desdoble (espacio + bloque) para un curso académico.
	 *
	 * @param cursoAcademico - El curso académico del que se desean obtener las asignaciones de desdoble.
	 * @return La lista de asignaciones de desdoble en formato DTO (una por cada pareja espacio-bloque).
	 */
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.EspacioDesdobleAsignadoDto(e.idEspacioDesdoble.espacioId.cursoAcademico, e.idEspacioDesdoble.espacioId.nombre, e.idEspacioDesdoble.bloqueId, e.idEspacioDesdoble.asignatura) "
			+ "FROM EspacioDesdoble e "
			+ "WHERE e.idEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico")
	List<EspacioDesdobleAsignadoDto> buscarAsignadosPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Obtiene las asignaciones de desdoble de una ASIGNATURA concreta dentro de un bloque (puede ser 0 o 1, ya que
	 * cada asignatura admite como mucho un aula). Se usa para reemplazar la asignación previa de la asignatura.
	 *
	 * @param cursoAcademico - El curso académico.
	 * @param bloqueId       - El bloque.
	 * @param asignatura     - El nombre de la asignatura.
	 * @return La lista de asignaciones de desdoble de esa asignatura en ese bloque.
	 */
	@Query("SELECT e FROM EspacioDesdoble e "
			+ "WHERE e.idEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico "
			+ "AND e.idEspacioDesdoble.bloqueId = :bloqueId "
			+ "AND e.idEspacioDesdoble.asignatura = :asignatura")
	List<EspacioDesdoble> buscarPorBloqueYAsignatura(@Param("cursoAcademico") String cursoAcademico,
			@Param("bloqueId") Long bloqueId, @Param("asignatura") String asignatura);

	/**
	 * Cuenta el número de aulas de desdoble ya asignadas a un bloque (una por asignatura como mucho). Se usa como
	 * salvaguarda del TOPE: nunca puede haber más aulas asignadas que asignaturas tiene el bloque.
	 *
	 * @param cursoAcademico - El curso académico.
	 * @param bloqueId       - El bloque.
	 * @return El número de asignaciones de desdoble del bloque.
	 */
	@Query("SELECT COUNT(e) FROM EspacioDesdoble e "
			+ "WHERE e.idEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico "
			+ "AND e.idEspacioDesdoble.bloqueId = :bloqueId")
	long contarPorBloque(@Param("cursoAcademico") String cursoAcademico, @Param("bloqueId") Long bloqueId);

	/**
	 * Indica si un aula (curso académico + nombre) está asignada como desdoble a ALGÚN bloque.
	 * <p>
	 * Se utiliza para impedir que un aula usada como desdoble pueda asignarse a la vez como aula de referencia
	 * (fijo) y viceversa.
	 *
	 * @param cursoAcademico - El curso académico del aula.
	 * @param nombre         - El nombre del aula.
	 * @return true si el aula está asignada como desdoble a algún bloque, false en caso contrario.
	 */
	@Query("SELECT COUNT(e) > 0 FROM EspacioDesdoble e "
			+ "WHERE e.idEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico "
			+ "AND e.idEspacioDesdoble.espacioId.nombre = :nombre")
	boolean existePorEspacio(@Param("cursoAcademico") String cursoAcademico, @Param("nombre") String nombre);

	/**
	 * Borra todas las asignaciones de desdoble de un aula concreta (todas sus parejas espacio-bloque).
	 * <p>
	 * Se utiliza al borrar un aula del catálogo del instituto: además de quitar el aula del catálogo, hay que
	 * eliminar sus usos como desdoble en todos los bloques.
	 *
	 * @param cursoAcademico - El curso académico del aula.
	 * @param nombre         - El nombre del aula.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM EspacioDesdoble e "
			+ "WHERE e.idEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico "
			+ "AND e.idEspacioDesdoble.espacioId.nombre = :nombre")
	void borrarPorEspacio(@Param("cursoAcademico") String cursoAcademico, @Param("nombre") String nombre);

	/**
	 * Borra todas las asignaciones de desdoble asociadas a un curso académico.
	 * <p>
	 * Antes de invocar este método deben haberse borrado las ocupaciones (Ocupa_Espacio_Desdoble) que apuntan a
	 * estos espacios para no violar la clave foránea.
	 *
	 * @param cursoAcademico - El curso académico cuyas asignaciones de desdoble se desean borrar.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM EspacioDesdoble e WHERE e.idEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico")
	void borrarPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);
}
