package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto3;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.DatosMatriculaDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link DatosBrutoAlumnoMatricula}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Todas las consultas del conjunto de trabajo se filtran por el curso académico activo ({@code cursoAcademico}), que el
 * backend resuelve desde {@code seleccionado = true}, de modo que cada curso académico tiene sus propias matrículas.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IDatosBrutoAlumnoMatriculaRepository extends JpaRepository<DatosBrutoAlumnoMatricula, Integer>
{

    /**
     * Recupera los registros de {@link DatosBrutoAlumnoMatricula} filtrados por nombre y apellidos.
     * 
     * @param nombre	 								 - El nombre del alumno a buscar.
     * @param apellidos 								 - Los apellidos del alumno a buscar.
     * @return List<<DatosBrutoAlumnoMatricula> - Una lista de {@link Optional} de {@link DatosBrutoAlumnoMatricula} que coinciden con el nombre y apellidos.
     */
    public List<DatosBrutoAlumnoMatricula> findByNombreAndApellidosAndCursoEtapa(String nombre, String apellidos, CursoEtapa cursoEtapa);
    
    /**
     * Método para obtener nombres y apellidos únicos y mapearlos al DTO AlumnoDto.
     *
     * @param cursoAcademico - El curso académico activo.
     * @param curso  		   - El curso específico.
     * @param etapa 		   - La etapa específica.
     * @return List<AlumnoDto> - La lista de AlumnoDto con nombres y apellidos únicos.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto3(d.nombre, d.apellidos, d.asignado) " +
           "FROM DatosBrutoAlumnoMatricula d " +
           "WHERE d.cursoEtapa.idCursoEtapa.cursoAcademico = :cursoAcademico AND d.cursoEtapa.idCursoEtapa.curso = :curso AND d.cursoEtapa.idCursoEtapa.etapa = :etapa AND d.estadoMatricula = 'MATR'" +
           "GROUP BY d.nombre, d.apellidos, d.asignado")
    List<AlumnoDto3> findDistinctAlumnosByCursoEtapa(@Param("cursoAcademico") String cursoAcademico,
                                                     @Param("curso") Integer curso,
    												 @Param("etapa") String etapa);
    
    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaDto (c.idCursoEtapa.curso, c.idCursoEtapa.etapa) "
    		+ "FROM DatosBrutoAlumnoMatricula d "
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE c.idCursoEtapa.cursoAcademico = :cursoAcademico AND c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa AND d.nombre IS NOT NULL")
    List<CursoEtapaDto> encontrarAlumnosMatriculaPorEtapaYCurso(@Param("cursoAcademico") String cursoAcademico,
                                                                @Param("curso") Integer curso, 
            													@Param("etapa") String etapa);
    
    /**
     * Lista los pares (curso, etapa) que tienen matrículas cargadas para el curso académico activo.
     *
     * @param cursoAcademico - El curso académico activo.
     * @return Lista de curso/etapa con matrículas.
     */
    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaDto (c.idCursoEtapa.curso, c.idCursoEtapa.etapa) "
    		+ "FROM DatosBrutoAlumnoMatricula d "
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE c.idCursoEtapa.cursoAcademico = :cursoAcademico AND d.nombre IS NOT NULL")
    List<CursoEtapaDto> encontrarAlumnosMatriculaPorEtapaYCurso(@Param("cursoAcademico") String cursoAcademico);
    
    @Modifying
    @Transactional
    void deleteDistinctByCursoEtapa(@Param("curso") CursoEtapa cursoEtapa);

    /**
     * Borra todos los registros de {@link DatosBrutoAlumnoMatricula} asociados a un curso y etapa del curso académico activo.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DatosBrutoAlumnoMatricula d "
            + "WHERE d.cursoEtapa.idCursoEtapa.cursoAcademico = :cursoAcademico AND d.cursoEtapa.idCursoEtapa.curso = :curso AND d.cursoEtapa.idCursoEtapa.etapa = :etapa")
    void borrarPorCursoYEtapa(@Param("cursoAcademico") String cursoAcademico, @Param("curso") Integer curso, @Param("etapa") String etapa);
    
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.DatosMatriculaDto(d.nombre, d.apellidos, d.asignatura, d.estadoMatricula, "
    		+ "CASE WHEN EXISTS ("
    		+ "   SELECT 1 FROM Asignatura a "
    		+ "   WHERE a.idAsignatura.nombre = d.asignatura "
    		+ "     AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
    		+ "     AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso "
    		+ "     AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa "
    		+ "     AND a.esAdHoc = true) THEN true ELSE false END) "
    		+ "FROM DatosBrutoAlumnoMatricula d "
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE c.idCursoEtapa.cursoAcademico = :cursoAcademico AND c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa")
    List<DatosMatriculaDto> encontrarDatosMatriculaPorCursoYEtapa(@Param("cursoAcademico") String cursoAcademico,
                                                                  @Param("curso") Integer curso, 
																  @Param("etapa") String etapa);

    /**
     * Obtiene los pares (nombre, apellidos) distintos de alumnos de un curso y etapa del curso académico activo.
     * <p>Se usa para materializar filas NO_MATR al crear una asignatura ad-hoc.</p>
     *
     * @param cursoAcademico - El curso académico activo.
     * @param curso - El curso.
     * @param etapa - La etapa.
     * @return Lista de arrays [nombre, apellidos] distintos.
     */
    @Query("SELECT DISTINCT d.nombre, d.apellidos "
            + "FROM DatosBrutoAlumnoMatricula d "
            + "JOIN d.cursoEtapa c "
            + "WHERE c.idCursoEtapa.cursoAcademico = :cursoAcademico AND c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa AND d.nombre IS NOT NULL")
    List<Object[]> encontrarAlumnosDistintosPorCursoYEtapa(@Param("cursoAcademico") String cursoAcademico,
                                                           @Param("curso") Integer curso,
                                                           @Param("etapa") String etapa);

    /**
     * Borra los registros de datos brutos asociados a una asignatura en un curso y etapa concretos del curso académico activo.
     * <p>Se usa al borrar una asignatura ad-hoc para limpiar las filas NO_MATR materializadas.</p>
     *
     * @param cursoAcademico - El curso académico activo.
     * @param asignatura - El nombre de la asignatura.
     * @param curso      - El curso.
     * @param etapa      - La etapa.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DatosBrutoAlumnoMatricula d "
            + "WHERE d.asignatura = :asignatura "
            + "AND d.cursoEtapa.idCursoEtapa.cursoAcademico = :cursoAcademico AND d.cursoEtapa.idCursoEtapa.curso = :curso AND d.cursoEtapa.idCursoEtapa.etapa = :etapa")
    void borrarPorAsignaturaYCursoYEtapa(@Param("cursoAcademico") String cursoAcademico,
                                         @Param("asignatura") String asignatura,
                                         @Param("curso") Integer curso,
                                         @Param("etapa") String etapa);
    
    @Query("SELECT d "
    		+ "FROM DatosBrutoAlumnoMatricula d " 
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE d.nombre = :nombre AND d.apellidos = :apellidos AND d.asignatura = :asignatura AND c.idCursoEtapa.cursoAcademico = :cursoAcademico AND c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa")
    DatosBrutoAlumnoMatricula encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(@Param("cursoAcademico") String cursoAcademico,
                                                                                            @Param("nombre") String nombre, 
																						 	@Param("apellidos") String apellidos,
																						 	@Param("asignatura") String asignatura,
																						 	@Param("curso") Integer curso, 
			 																				@Param("etapa") String etapa);

	@Query("SELECT d "
			+ "FROM DatosBrutoAlumnoMatricula d "
			+ "JOIN d.cursoEtapa c "
			+ "WHERE d.nombre = :nombre AND d.apellidos = :apellidos AND d.asignatura = :asignatura AND c.idCursoEtapa.cursoAcademico = :cursoAcademico AND c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa AND d.estadoMatricula = :estadoMatricula")
	DatosBrutoAlumnoMatricula encontrarAlumnoPorNombreYApellidosYAsignaturaYCursoYEtapaYEstado(@Param("cursoAcademico") String cursoAcademico,
                                                                                               @Param("nombre") String nombre,
																							   @Param("apellidos") String apellidos,
																							   @Param("asignatura") String asignatura,
																							   @Param("curso") Integer curso,
																							   @Param("etapa") String etapa,
																							   @Param("estadoMatricula") String estadoMatricula);

    List<DatosBrutoAlumnoMatricula> findDistinctAsignaturaByCursoEtapa(CursoEtapa cursoEtapa);
    
    @Transactional
   	@Modifying
    void deleteByNombreAndApellidosAndAsignaturaAndEstadoMatriculaAndCursoEtapa(@Param("nombre") String nombre, 
																			 	@Param("apellidos") String apellidos,
																			 	@Param("asignatura") String asignatura,
																			 	@Param("estadoMatricula") String estadoMatricula,
																			 	@Param("cursoEtapa") CursoEtapa cursoEtapa);


}
