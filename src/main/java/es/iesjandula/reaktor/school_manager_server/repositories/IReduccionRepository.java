package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la
 * entidad {@link Reduccion}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de
 * operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Reduccion}. La clave primaria de la entidad
 * {@link Reduccion} está compuesta por un {@link String}, que
 * representa el {@code nombre} de la reducción.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IReduccionRepository extends JpaRepository<Reduccion, IdReduccion>
{
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto(r.idReduccion.nombre, r.idReduccion.horas, r.decideDireccion, r.cursoEtapaGrupo) "
			+ "FROM Reduccion r LEFT JOIN r.cursoEtapaGrupo")
	List<ReduccionDto> encontrarTodasReducciones();

	/**
	 * Lista las reducciones del curso académico indicado (todas llevan su propio {@code cursoAcademico} en la PK:
	 * tanto las sin docencia —tutorías plantilla y no tutorías— como las con docencia por grupo).
	 *
	 * @param cursoAcademico curso académico activo.
	 * @return lista de {@link ReduccionDto} del curso académico.
	 */
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto(r.idReduccion.nombre, r.idReduccion.horas, r.decideDireccion, r.cursoEtapaGrupo) "
			+ "FROM Reduccion r LEFT JOIN r.cursoEtapaGrupo "
			+ "WHERE r.idReduccion.cursoAcademico = :cursoAcademico")
	List<ReduccionDto> encontrarReduccionesPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Borra TODAS las reducciones del curso académico indicado.
	 *
	 * @param cursoAcademico curso académico cuyas reducciones se borran.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM Reduccion r WHERE r.idReduccion.cursoAcademico = :cursoAcademico")
	void borrarTodasPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Borra las reducciones del curso académico cuyo nombre coincide (LIKE) con el patrón indicado. Se usa para el
	 * "borrar todos" de TUTORÍAS (patrón {@code "Tutoría %"}), que incluye las plantilla y las materializadas por grupo.
	 *
	 * @param cursoAcademico curso académico.
	 * @param patronNombre   patrón LIKE para el nombre.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM Reduccion r WHERE r.idReduccion.cursoAcademico = :cursoAcademico AND r.idReduccion.nombre LIKE :patronNombre")
	void borrarPorCursoAcademicoYNombreLike(@Param("cursoAcademico") String cursoAcademico, @Param("patronNombre") String patronNombre);

	/**
	 * Borra las reducciones del curso académico cuyo nombre NO coincide (NOT LIKE) con el patrón indicado. Se usa
	 * para el "borrar todos" de NO TUTORÍAS (todo lo que no empieza por {@code "Tutoría "}).
	 *
	 * @param cursoAcademico curso académico.
	 * @param patronNombre   patrón LIKE para el nombre a excluir.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM Reduccion r WHERE r.idReduccion.cursoAcademico = :cursoAcademico AND r.idReduccion.nombre NOT LIKE :patronNombre")
	void borrarPorCursoAcademicoYNombreNotLike(@Param("cursoAcademico") String cursoAcademico, @Param("patronNombre") String patronNombre);

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto(r.idReduccion.nombre, r.idReduccion.horas) " +
			"FROM Reduccion r " +
			"WHERE r.decideDireccion = false ")
	List<ReduccionProfesoresDto> encontrarReduccionesParaProfesores();

	@Query("SELECT r " +
			"FROM Reduccion r " +
			"WHERE r.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico " +
			  "AND r.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso " +
			  "AND r.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa " +
			  "AND r.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo " +
			  "AND r.idReduccion.nombre = :nombreReduccion")
    Optional<Reduccion> findReduccionesByCursoEtapaGrupoAndNombre(@Param("cursoAcademico") String cursoAcademico, @Param("curso") int curso, @Param("etapa") String etapa, @Param("grupo") String grupo, @Param("nombreReduccion") String nombreReduccion);

	/**
	 * Devuelve las reducciones con docencia (vinculadas a un {@code cursoEtapaGrupo}) del curso académico indicado.
	 *
	 * @param cursoAcademico curso académico cuyas reducciones se buscan.
	 * @return lista de reducciones vinculadas a ese curso académico.
	 */
	@Query("SELECT r " +
			"FROM Reduccion r " +
			"WHERE r.idReduccion.cursoAcademico = :cursoAcademico")
	List<Reduccion> findAllByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Devuelve las reducciones visibles en el listado de la configuración básica para el curso académico indicado:
	 * las reducciones SIN docencia ({@code cursoEtapaGrupo} nulo), que son globales y compartidas (entre ellas las
	 * cargadas por CSV de tutorías / no tutorías), MÁS las reducciones CON docencia vinculadas a un grupo de ese
	 * curso académico. A diferencia de {@link #findAllByCursoAcademico(String)} (pensado para copiar entre cursos,
	 * que solo contempla las que tienen docencia), esta consulta NO descarta las globales mediante el join implícito.
	 *
	 * @param cursoAcademico curso académico cuyas reducciones se listan.
	 * @return lista de reducciones globales y las con docencia del curso académico indicado.
	 */
	@Query("SELECT r " +
			"FROM Reduccion r " +
			"WHERE r.idReduccion.cursoAcademico = :cursoAcademico")
	List<Reduccion> findAllParaListadoByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

	/**
	 * Devuelve las reducciones de TUTORÍA "plantilla" (a nivel curso/etapa, sin docencia) cuyo nombre coincide con el
	 * indicado. Las tutorías cargadas por CSV se persisten sin docencia ({@code cursoEtapaGrupo} nulo) con el nombre
	 * sintetizado {@code "Tutoría <curso>º <etapa>"}; pueden existir varias con el mismo nombre y horas distintas. Se
	 * usa al materializar las tutorías por grupo: por cada plantilla se crea una tutoría por cada grupo del curso/etapa.
	 *
	 * @param nombre nombre sintetizado de la tutoría a nivel curso/etapa (p. ej. "Tutoría 1º ESO").
	 * @return lista de tutorías plantilla (sin docencia) con ese nombre.
	 */
	@Query("SELECT r " +
			"FROM Reduccion r " +
			"WHERE r.cursoEtapaGrupo IS NULL " +
			  "AND r.idReduccion.cursoAcademico = :cursoAcademico " +
			  "AND r.idReduccion.nombre = :nombre")
	List<Reduccion> findTutoriasPlantillaByNombre(@Param("cursoAcademico") String cursoAcademico, @Param("nombre") String nombre);

}
