package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaConDepartamentoDto;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDtoSinGrupo;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaHorasDto;
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
	
	@Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDto(a.idAsignatura.curso, a.idAsignatura.etapa, a.idAsignatura.nombre, a.horas, a.bloqueId.id) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa "
			+ "GROUP BY a.idAsignatura.curso, a.idAsignatura.etapa, a.idAsignatura.nombre, a.horas, a.bloqueId.id")
	List<AsignaturaDto> findByCursoAndEtapa(@Param("curso") Integer curso, 
										    @Param("etapa") String etapa);
	
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDtoSinGrupo(a.idAsignatura.curso, a.idAsignatura.etapa, a.idAsignatura.nombre, a.horas) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa "
			+ "GROUP BY a.idAsignatura.curso, a.idAsignatura.etapa, a.idAsignatura.nombre, a.horas")
	List<AsignaturaDtoSinGrupo> findByCursoAndEtapaDistinct(@Param("curso") Integer curso,
															@Param("etapa") String etapa);

	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.nombre = :nombre")
	List<Optional<Asignatura>> findAsignaturasByCursoEtapaAndNombre(@Param("curso") int curso, 
														      		@Param("etapa") String etapa, 
														      		@Param("nombre") String nombre);

	@Transactional
	@Modifying
	@Query("DELETE "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa")
	void borrarPorCursoYEtapa(@Param("curso") int curso, 
							  @Param("etapa") String etapa);
	
	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.nombre = :nombre AND a.idAsignatura.grupo = :grupo")
	Optional<Asignatura> encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(@Param("curso") int curso, 
																		@Param("etapa") String etapa, 
																		@Param("nombre") String nombre,
																		@Param("grupo") Character grupo);
	
	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.nombre = :nombres")
	List<Asignatura> findNombreByCursoEtapaAndNombres(@Param("curso") int curso,
													  @Param("etapa") String etapa, 
													  @Param("nombres") String nombres);
	
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaHorasDto(a.idAsignatura.nombre, a.horas) "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa")
	List<AsignaturaHorasDto> findNombreAndHorasByCursoEtapaAndNombres(@Param("curso") Integer curso,
																	  @Param("etapa") String etapa);
	
	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.nombre = :nombres")
	Asignatura encontrarPorCursoYEtapaYNombre(@Param("curso") int curso,
											  @Param("etapa") String etapa, 
											  @Param("nombres") String nombres);
	
	@Query("SELECT DISTINCT c "
			+ "FROM CursoEtapaGrupo c "
			+ "JOIN c.idCursoEtapaGrupo idC")
	List<CursoEtapaGrupo> distinctCursoEtapaGrupo();
	
	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.grupo = :grupo AND a.departamentoPropietario is null")
	List<Asignatura> asignaturasPorCursoEtapaGrupo(@Param("curso") int curso, 
												   @Param("etapa") String etapa, 
												   @Param("grupo") Character grupo);
	
	
	@Query("SELECT a "
			+ "FROM Asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.grupo = :grupo AND a.idAsignatura.nombre = :nombre")
	Optional<Asignatura> findAsignaturasByCursoEtapaGrupoAndNombre(@Param("curso") int curso,
																   @Param("etapa") String etapa, 
																   @Param("grupo") Character grupo, 
																   @Param("nombre") String nombre);

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaConDepartamentoDto(a.departamentoPropietario.nombre, a.departamentoPropietario.plantilla, (depto.plantilla*18), SUM(a.horas), ((depto.plantilla*18) - SUM(a.horas)) as desfase) " +
			"FROM Asignatura a " +
			"JOIN a.departamentoPropietario depto " +
			"GROUP BY a.departamentoPropietario.nombre")
	List<AsignaturaConDepartamentoDto> encontrarAsignaturasConDepartamento();

}
