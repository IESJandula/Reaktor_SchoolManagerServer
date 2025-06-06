package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
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

	@Query("SELECT DISTINCT m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "WHERE m.idMatricula.alumno.nombre = :nombre AND m.idMatricula.alumno.apellidos = :apellidos AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso "
			+ "AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa "
			+ "ORDER BY m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo ASC ")
	List<String> encontrarGrupoPorNombreAndApellidosAndCursoAndEtapa(@Param("nombre") String nombre,
															   @Param("apellidos") String apellidos,
															   @Param("curso") Integer curso,
															   @Param("etapa") String etapa);

	@Query("SELECT DISTINCT  new es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto(m.idMatricula.alumno.nombre, m.idMatricula.alumno.apellidos) "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "WHERE m.idMatricula.alumno.nombre = :nombre AND m.idMatricula.alumno.apellidos = :apellidos AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso "
			+ "AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa ")
	AlumnoDto encontrarAlumnoPorNombreAndApellidosAndCursoAndEtapa(@Param("nombre") String nombre,
																   @Param("apellidos") String apellidos,
																   @Param("curso") Integer curso,
																   @Param("etapa") String etapa);

	@Query("SELECT m "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "WHERE m.idMatricula.alumno.nombre = :nombre AND m.idMatricula.alumno.apellidos = :apellidos AND m.idMatricula.asignatura.idAsignatura.nombre = :nombreAsignatura "
			+ "AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa ")
	Matricula encontrarMatriculaPorNombreAndApellidosAndCursoAndEtapa(@Param("nombre") String nombre,
																	  @Param("apellidos") String apellidos,
																	  @Param("nombreAsignatura") String nombreAsignatura,
																	  @Param("curso") Integer curso,
																	  @Param("etapa") String etapa);

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.MatriculaDto(alu.nombre, alu.apellidos, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo, a.idAsignatura.nombre, a.horas, a.esoBachillerato, a.bloqueId.id) "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "JOIN idM.alumno alu "
			+ "JOIN idM.asignatura a "
			+ "WHERE alu.nombre = :nombre AND alu.apellidos = :apellidos AND( a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo OR a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = '" + Constants.GRUPO_OPTATIVAS + "')")
	List<MatriculaDto> encontrarAlumnoPorNombreYApellidosYGrupo(@Param("nombre") String nombre,
																@Param("apellidos") String apellidos,
																@Param("grupo") String grupo);

	@Query("SELECT COUNT(m.idMatricula.asignatura.idAsignatura.nombre) "
			+ "FROM Matricula m "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.nombre = :nombreAsignatura AND m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo AND m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa ")
	Integer numeroAsignaturasPorNombreYGrupo(@Param("nombreAsignatura") String nombreAsignatura,
											 @Param("curso") Integer curso,
											 @Param("etapa") String etapa,
											 @Param("grupo") String grupo);

	//Al estar en un metodo transaccional hay que limpiar manualmente cualquier registro de matricula para que deje hacer flush al final
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE "
			+ "FROM Matricula m "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND m.idMatricula.asignatura.idAsignatura.nombre = :nombre AND m.idMatricula.alumno.id = :idAlumno")
	void borrarPorTodo(@Param("curso") int curso,
					   @Param("etapa") String etapa,
					   @Param("nombre") String nombre,
					   @Param("idAlumno") Integer idAlumno);

	@Query("SELECT DISTINCT m.idMatricula.alumno.id "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "JOIN idM.asignatura a "
			+ "WHERE idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo")
	List<Integer> encontrarIdAlumnoPorCursoEtapaYGrupo(@Param("curso") Integer curso,
													   @Param("etapa") String etapa,
													   @Param("grupo") String grupo);

	@Query("SELECT alumno.id "
      + "FROM Matricula m "
      + "JOIN m.idMatricula.alumno alumno "
      + "JOIN m.idMatricula.asignatura asig "
      + "JOIN asig.idAsignatura .cursoEtapaGrupo .idCursoEtapaGrupo ceg "
      + "WHERE ceg.curso = :curso AND ceg.etapa = :etapa AND ceg.grupo = :grupo AND asig.idAsignatura.nombre = :nombreAsignatura AND alumno.nombre = :nombre AND alumno.apellidos = :apellidos")
	List<Integer> encontrarIdAlumnoPorCursoEtapaGrupoYNombre(@Param("curso") Integer curso,
													   		 @Param("etapa") String etapa,
													   		 @Param("grupo") String grupo,
													   		 @Param("nombreAsignatura") String nombreAsignatura,
															 @Param("nombre") String nombre,
															 @Param("apellidos") String apellidos);
	@Transactional
	@Modifying
	@Query("DELETE " +
			"FROM Matricula m " +
			"WHERE m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
	void borrarPorCursoYEtapa(@Param("curso") Integer curso,
							  @Param("etapa") String etapa);

	@Query("SELECT COUNT(DISTINCT m.idMatricula.alumno.id) "
			+ "FROM Matricula m "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo AND m.idMatricula.asignatura.idAsignatura.nombre = :asignatura")
	Long numeroAlumnosPorGrupoYAsignatura(@Param("curso") Integer curso,
										  @Param("etapa") String etapa,
										  @Param("grupo") String grupo,
										  @Param("asignatura") String asignatura);

	@Query("SELECT m.idMatricula.alumno "
			+ "FROM Matricula m JOIN m.idMatricula idM "
			+ 				   "JOIN idM.asignatura a "
			+ "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo AND idM.alumno.nombre = :nombreAlumno AND idM.alumno.apellidos = :apellidosAlumno")
	Optional<Alumno> buscarAlumnoPorCursoEtapaNombreApellidos(@Param("curso") Integer curso,
															  @Param("etapa") String etapa,
															  @Param("grupo") String grupo,
															  @Param("nombreAlumno") String nombreAlumno,
															  @Param("apellidosAlumno") String apellidosAlumno) ;

	@Query("SELECT COUNT(DISTINCT m.idMatricula.alumno.id) " +
			"FROM Matricula m")
	Long numeroAlumnos();

	@Query("SELECT m "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "WHERE m.idMatricula.asignatura.idAsignatura.nombre = :nombreAsignatura AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa ")
	List<Matricula> encontrarMatriculaPorNombreAsignaturaAndCursoAndEtapa(@Param("nombreAsignatura") String nombreAsignatura,
																		  @Param("curso") Integer curso,
																		  @Param("etapa") String etapa);

	@Query("SELECT DISTINCT m.idMatricula.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "WHERE idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso "
			+ "AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND idM.alumno.id = :idAlumno AND idM.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo"
			+ " <> '" + Constants.GRUPO_OPTATIVAS + "'")
	String encontrarGrupoPorMatricula(@Param("curso") Integer curso,
									  @Param("etapa") String etapa,
									  @Param("idAlumno") Integer idAlumno);
}
