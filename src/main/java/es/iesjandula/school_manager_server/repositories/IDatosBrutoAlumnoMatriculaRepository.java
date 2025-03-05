package es.iesjandula.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.school_manager_server.dtos.AlumnoDto2;
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
    public List<DatosBrutoAlumnoMatricula> findByNombreAndApellidos(String nombre, String apellidos);
    
    /**
     * Método para obtener nombres y apellidos únicos y mapearlos al DTO AlumnoDto.
     *
     * @param curso  		   - El curso específico.
     * @param etapa 		   - La etapa específica.
     * @return List<AlumnoDto> - La lista de AlumnoDto con nombres y apellidos únicos.
     */
    @Query("SELECT new es.iesjandula.school_manager_server.dtos.AlumnoDto2(d.nombre, d.apellidos, null) " +
           "FROM DatosBrutoAlumnoMatricula d " +
           "WHERE d.cursoEtapa.idCursoEtapa.curso = :curso " +
           "AND d.cursoEtapa.idCursoEtapa.etapa = :etapa " +
           "GROUP BY d.nombre, d.apellidos")
    List<AlumnoDto2> findDistinctAlumnosByCursoEtapa
    (
            @Param("curso") Integer curso, 
            @Param("etapa") String etapa
    );
}
