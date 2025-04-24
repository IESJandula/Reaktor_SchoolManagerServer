package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.MatriculaDto;
import es.iesjandula.reaktor.school_manager_server.models.Alumno;
import es.iesjandula.reaktor.school_manager_server.models.Matricula;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Matricula}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Matricula}. La clave primaria de la entidad {@link Matricula} está compuesta por el tipo {@link IdMatricula}.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IMatriculaRepository extends JpaRepository<Matricula, IdMatricula>
{
	
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.MatriculaDto(alu.nombre, alu.apellidos, a.idAsignatura.curso, a.idAsignatura.etapa, a.idAsignatura.grupo, a.idAsignatura.nombre, a.horas) "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "JOIN idM.alumno alu "
			+ "JOIN idM.asignatura a "
			+ "WHERE alu.nombre = :nombre AND alu.apellidos = :apellidos")
	List<MatriculaDto> encontrarAlumnoPorNombreYApellidos(@Param("nombre") String nombre,
														  @Param("apellidos") String apellidos);
	
	@Query("SELECT COUNT(m.idMatricula.asignatura.idAsignatura.nombre) "
			+ "FROM Matricula m "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.nombre = :nombreAsignatura")
	Long numeroAsignaturasPorNombre(@Param("nombreAsignatura") String nombreAsignatura);
	
	@Modifying
	@Transactional
	@Query("DELETE "
			+ "FROM Matricula m "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.curso = :curso AND m.idMatricula.asignatura.idAsignatura.etapa = :etapa AND m.idMatricula.asignatura.idAsignatura.nombre = :nombre AND m.idMatricula.alumno.id = :idAlumno")
	void borrarPorTodo(@Param("curso") int curso,
					   @Param("etapa") String etapa,
					   @Param("nombre") String nombre,
					   @Param("idAlumno") Integer idAlumno);

	@Query("SELECT DISTINCT m.idMatricula.alumno.id "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "JOIN idM.asignatura a "
			+ "WHERE idM.asignatura.idAsignatura.curso = :curso AND idM.asignatura.idAsignatura.etapa = :etapa AND idM.asignatura.idAsignatura.grupo = :grupo")
	List<Integer> encontrarIdAlumnoPorCursoEtapaYGrupo(@Param("curso") Integer curso,
													   @Param("etapa") String etapa,
													   @Param("grupo") Character grupo);

	@Query("SELECT m.idMatricula.alumno.id "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "JOIN idM.asignatura a "
			+ "WHERE idM.asignatura.idAsignatura.curso = :curso AND idM.asignatura.idAsignatura.etapa = :etapa AND idM.asignatura.idAsignatura.grupo = :grupo AND idM.alumno.nombre = :nombre")
	List<Integer> encontrarIdAlumnoPorCursoEtapaGrupoYNombre(@Param("curso") Integer curso,
													   @Param("etapa") String etapa,
													   @Param("grupo") Character grupo,
													   @Param("nombre") String nombre);
	@Transactional
	@Modifying
	@Query("DELETE " +
			"FROM Matricula m " +
			"WHERE m.idMatricula.asignatura.idAsignatura.curso = :curso AND m.idMatricula.asignatura.idAsignatura.etapa = :etapa")
	void borrarPorCursoYEtapa(@Param("curso") Integer curso,
							  @Param("etapa") String etapa);

	@Query("SELECT COUNT(DISTINCT m.idMatricula.alumno.id) "
			+ "FROM Matricula m "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.curso = :curso AND m.idMatricula.asignatura.idAsignatura.etapa = :etapa AND m.idMatricula.asignatura.idAsignatura.grupo = :grupo")
	Long numeroAlumnosPorGrupo(@Param("curso") Integer curso,
							   @Param("etapa") String etapa,
							   @Param("grupo") Character grupo);

	@Query("SELECT COUNT(DISTINCT m.idMatricula.alumno.id) "
			+ "FROM Matricula m "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.curso = :curso AND m.idMatricula.asignatura.idAsignatura.etapa = :etapa AND m.idMatricula.asignatura.idAsignatura.grupo = :grupo AND m.idMatricula.asignatura.idAsignatura.nombre = :asignatura")
	Long numeroAlumnosPorGrupoYAsignatura(@Param("curso") Integer curso,
										  @Param("etapa") String etapa,
										  @Param("grupo") Character grupo,
										  @Param("asignatura") String asignatura);

	@Query("SELECT m.idMatricula.alumno "
			+ "FROM Matricula m JOIN m.idMatricula idM "
			+ 				   "JOIN idM.asignatura a "
			+ "WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.grupo = :grupo AND idM.alumno.nombre = :nombreAlumno AND idM.alumno.apellidos = :apellidosAlumno")
	Optional<Alumno> buscarAlumnoPorCursoEtapaNombreApellidos(@Param("curso") Integer curso,
															  @Param("etapa") String etapa,
															  @Param("grupo") Character grupo,
															  @Param("nombreAlumno") String nombreAlumno,
															  @Param("apellidosAlumno") String apellidosAlumno) ;
}
