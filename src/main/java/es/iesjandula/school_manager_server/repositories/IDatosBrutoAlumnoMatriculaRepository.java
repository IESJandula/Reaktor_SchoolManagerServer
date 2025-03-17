package es.iesjandula.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import es.iesjandula.school_manager_server.dtos.AlumnoDto3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.school_manager_server.dtos.CursoEtapaDto;
import es.iesjandula.school_manager_server.dtos.DatosMatriculaDto;
import es.iesjandula.school_manager_server.models.CursoEtapa;
import es.iesjandula.school_manager_server.models.DatosBrutoAlumnoMatricula;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link DatosBrutoAlumnoMatricula}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link DatosBrutoAlumnoMatricula}.
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
     * @param curso  		   - El curso específico.
     * @param etapa 		   - La etapa específica.
     * @return List<AlumnoDto> - La lista de AlumnoDto con nombres y apellidos únicos.
     */
    @Query("SELECT new es.iesjandula.school_manager_server.dtos.AlumnoDto3(d.nombre, d.apellidos, d.asignado) " +
           "FROM DatosBrutoAlumnoMatricula d " +
           "WHERE d.cursoEtapa.idCursoEtapa.curso = :curso " +
           "AND d.cursoEtapa.idCursoEtapa.etapa = :etapa " +
           "GROUP BY d.nombre, d.apellidos")
    List<AlumnoDto3> findDistinctAlumnosByCursoEtapa(@Param("curso") Integer curso, 
    												 @Param("etapa") String etapa);
    
    @Query("SELECT DISTINCT new es.iesjandula.school_manager_server.dtos.CursoEtapaDto (c.idCursoEtapa.curso, c.idCursoEtapa.etapa) "
    		+ "FROM DatosBrutoAlumnoMatricula d "
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa AND d.nombre IS NOT NULL")
    List<CursoEtapaDto> encontrarAlumnosMatriculaPorEtapaYCurso(@Param("curso") Integer curso, 
            													@Param("etapa") String etapa);
    
    @Query("SELECT DISTINCT new es.iesjandula.school_manager_server.dtos.CursoEtapaDto (c.idCursoEtapa.curso, c.idCursoEtapa.etapa) "
    		+ "FROM DatosBrutoAlumnoMatricula d "
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE d.nombre IS NOT NULL")
    List<CursoEtapaDto> encontrarAlumnosMatriculaPorEtapaYCurso();
    
    @Modifying
    @Transactional
    void deleteDistinctByCursoEtapa(@Param("curso") CursoEtapa cursoEtapa);
    
    @Query("SELECT new es.iesjandula.school_manager_server.dtos.DatosMatriculaDto(d.nombre, d.apellidos, d.asignatura) "
    		+ "FROM DatosBrutoAlumnoMatricula d "
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa")
    List<DatosMatriculaDto> encontrarDatosMatriculaPorCursoYEtapa(@Param("curso") Integer curso, 
																  @Param("etapa") String etapa);
    
    @Query("SELECT d "
    		+ "FROM DatosBrutoAlumnoMatricula d "
    		+ "JOIN d.cursoEtapa c "
    		+ "WHERE d.nombre = :nombre AND d.apellidos = :apellidos AND d.asignatura = :asignatura AND c.idCursoEtapa.curso = :curso AND c.idCursoEtapa.etapa = :etapa")
    DatosBrutoAlumnoMatricula encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(@Param("nombre") String nombre, 
																						 	@Param("apellidos") String apellidos,
																						 	@Param("asignatura") String asignatura,
																						 	@Param("curso") Integer curso, 
			 																				@Param("etapa") String etapa);

//    @Query("SELEC ")
//    List<DatosBrutoAlumnoMatricula> encontrarAsignatuasPorCursoYEtapa(@Param("curso") Integer curso, 
//			  														  @Param("etapa") String etapa);
    
    List<DatosBrutoAlumnoMatricula> findDistinctAsignaturaByCursoEtapa(CursoEtapa cursoEtapa);
}
