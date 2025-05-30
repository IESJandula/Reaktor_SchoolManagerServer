package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite realizar operaciones CRUD sobre la tabla que representa
 * la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 * El uso de esta interfaz facilita la persistencia y la manipulación de objetos {@link Asignatura} en la base de datos,
 * sin necesidad de implementar manualmente los métodos.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz utiliza {@link IdAsignatura} como tipo para la clave primaria de la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IAsignaturaRepository extends JpaRepository<Asignatura, IdAsignatura>
{

	@Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDto(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo" +
			".idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas, a.bloqueId.id, a.sinDocencia) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa "
			+ "GROUP BY a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas, a.bloqueId.id, a.sinDocencia")
	List<AsignaturaDto> findByCursoAndEtapa(@Param("curso") Integer curso,
										    @Param("etapa") String etapa);

	@Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturasUnicasDto(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa "
			+ "GROUP BY a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas")
	List<AsignaturasUnicasDto> findByCursoAndEtapaDistinct(@Param("curso") Integer curso,
														   @Param("etapa") String etapa);

	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre")
	List<Asignatura> findAsignaturasByCursoEtapaAndNombre(@Param("curso") int curso,
														      		@Param("etapa") String etapa,
														      		@Param("nombre") String nombre);

	@Transactional
	@Modifying
	@Query("DELETE "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
	void borrarPorCursoYEtapa(@Param("curso") int curso,
							  @Param("etapa") String etapa);

	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo")
	Optional<Asignatura> encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(@Param("curso") int curso,
																		@Param("etapa") String etapa,
																		@Param("nombre") String nombre,
																		@Param("grupo") Character grupo);

	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombres")
	List<Asignatura> findNombreByCursoEtapaAndNombres(@Param("curso") int curso,
													  @Param("etapa") String etapa,
													  @Param("nombres") String nombres);

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaHorasDto(a.idAsignatura.nombre, a.horas) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
	List<AsignaturaHorasDto> findNombreAndHorasByCursoEtapa(@Param("curso") Integer curso,
															@Param("etapa") String etapa);

	@Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaSinGrupoDto(a.horas, a.esoBachillerato, a.sinDocencia, a.desdoble) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre")
	AsignaturaSinGrupoDto encontrarPorCursoYEtapaYNombre(@Param("curso") int curso,
											    	     @Param("etapa") String etapa,
											  		     @Param("nombre") String nombre);

	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo AND a.departamentoPropietario is null")
	List<Asignatura> asignaturasPorCursoEtapaGrupo(@Param("curso") int curso,
												   @Param("etapa") String etapa,
												   @Param("grupo") Character grupo);


	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo AND a.idAsignatura.nombre = :nombre")
	Optional<Asignatura> findAsignaturasByCursoEtapaGrupoAndNombre(@Param("curso") int curso,
																   @Param("etapa") String etapa,
																   @Param("grupo") Character grupo,
																   @Param("nombre") String nombre);

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaConDepartamentoDto(a.departamentoReceptor.nombre, a.departamentoReceptor.plantilla, (depto.plantilla*18), SUM(a.horas), (SUM(a.horas) - (depto.plantilla*18)) as desfase) " +
			"FROM Asignatura a " +
			"JOIN a.departamentoReceptor depto " +
			"GROUP BY a.departamentoReceptor.nombre")
	List<AsignaturaConDepartamentoDto> encontrarAsignaturasConDepartamento();

	@Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.HorasYBloquesDto(a.horas, a.bloqueId.id, a.sinDocencia, a.desdoble) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombres")
	Optional<HorasYBloquesDto> encontrarAsignaturaPorCursoEtapaNombre(@Param("curso") int curso,
																	  @Param("etapa") String etapa,
																	  @Param("nombres") String nombres);

	@Query("SELECT a.bloqueId.id " +
			"FROM Asignatura a " +
			"WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
	List<Long> encontrarBloquePorCursoEtapa(@Param("curso") int curso,
									 		@Param("etapa") String etapa);

    @Query("SELECT a " +
            "FROM Asignatura a " +
            "WHERE a.bloqueId = :#{#asignatura.bloqueId} AND a != :asignatura")
    List<Asignatura> buscaOptativasRelacionadas(@Param("asignatura") Asignatura asignatura);

	@Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.ImpartirAsignaturaDto(a.idAsignatura.nombre, a.horas, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a" +
			".idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo)" +
			"FROM Asignatura a " +
			"WHERE a.departamentoReceptor.nombre = :departamento AND a.sinDocencia = false AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = 'A'")
	List<ImpartirAsignaturaDto>  encontrarAsignaturasPorDepartamento(@Param("departamento") String departamento);

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.GrupoAsignaturaDto(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
			"FROM Asignatura a " +
			"WHERE a.idAsignatura.nombre = :nombre AND a.horas = :horas AND  a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
	List<GrupoAsignaturaDto> encontrarGrupoPorNombreAndHorasAndCursoAndEtapa(@Param("nombre") String nombre,
																			 @Param("horas") Integer horas,
																			 @Param("curso") Integer curso,
																			 @Param("etapa") String etapa);

	@Query("SELECT a " +
			"FROM Asignatura a " +
			"WHERE a.idAsignatura.nombre = :nombre")
	List<Asignatura> encontrarAsignaturaPorNombre(@Param("nombre") String nombre);

	@Query("SELECT COUNT(DISTINCT a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
			"FROM Asignatura a " +
			"WHERE a.idAsignatura.nombre = :nombre " +
			"AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
	Long contarGruposPorAsignatura(@Param("nombre") String nombre,
								   @Param("curso") Integer curso,
								   @Param("etapa") String etapa);

    @Query("SELECT DISTINCT a.desdoble " +
            "FROM Asignatura  a " +
            "WHERE a.idAsignatura.nombre = :nombre " +
            "AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    Boolean isDesabilitado(@Param("nombre") String nombre,
                           @Param("curso") Integer curso,
                           @Param("etapa") String etapa);

}
