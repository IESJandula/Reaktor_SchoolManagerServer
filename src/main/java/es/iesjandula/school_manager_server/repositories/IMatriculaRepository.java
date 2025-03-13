package es.iesjandula.school_manager_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.school_manager_server.dtos.MatriculaDto;
import es.iesjandula.school_manager_server.models.Matricula;
import es.iesjandula.school_manager_server.models.ids.IdMatricula;

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
	
	@Query("SELECT new es.iesjandula.school_manager_server.dtos.MatriculaDto(alu.nombre, alu.apellidos, a.idAsignatura.curso, a.idAsignatura.etapa, a.idAsignatura.grupo, a.idAsignatura.nombre) "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "JOIN idM.alumno alu "
			+ "JOIN idM.asignatura a "
			+ "WHERE alu.nombre = :nombre AND alu.apellidos = :apellidos")
	List<MatriculaDto> encontrarAlumnoPorNombreYApellidos(@Param("nombre") String nombre,
														  @Param("apellidos") String apellidos);

	@Query("SELECT m.idMatricula.alumno.id "
			+ "FROM Matricula m "
			+ "JOIN m.idMatricula idM "
			+ "JOIN idM.asignatura a "
			+ "WHERE idM.asignatura.idAsignatura.curso = :curso AND idM.asignatura.idAsignatura.etapa = :etapa AND idM.asignatura.idAsignatura.grupo = :grupo")
	List<Integer> encontrarIdAlumnoPorCursoEtapaYGrupo(@Param("curso") Integer curso,
										  @Param("etapa") String etapa,
										  @Param("grupo") Character grupo);

}
