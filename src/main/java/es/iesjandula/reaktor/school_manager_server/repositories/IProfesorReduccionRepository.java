package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.dtos.ProfesorReduccionesDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.ProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesorReduccion;

import java.util.List;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link ProfesorReduccion}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link ProfesorReduccion}. La clave primaria de la entidad {@link ProfesorReduccion} está compuesta por el tipo {@link IdProfesorReduccion}.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IProfesorReduccionRepository extends JpaRepository<ProfesorReduccion, IdProfesorReduccion>
{

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ProfesorReduccionesDto(pR.idProfesorReduccion.profesor.nombre, pR.idProfesorReduccion.profesor.apellidos, pR.idProfesorReduccion.reduccion.idReduccion.nombre, pR.idProfesorReduccion.reduccion.idReduccion.horas, pR.idProfesorReduccion.profesor.email)" +
            "FROM ProfesorReduccion pR")
    List<ProfesorReduccionesDto> encontrarTodosProfesoresReducciones();

    @Query("SELECT pR " +
            "FROM ProfesorReduccion pR " +
            "WHERE pR.idProfesorReduccion.reduccion.idReduccion.nombre = :nombre AND pR.idProfesorReduccion.reduccion.idReduccion.horas = :horas")
    ProfesorReduccion encontrarProfesorReduccion(String nombre, Integer horas);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto(pR.idProfesorReduccion.reduccion.idReduccion.nombre, pR.idProfesorReduccion.reduccion.idReduccion.horas) " +
            "FROM ProfesorReduccion pR " +
            "WHERE pR.idProfesorReduccion.profesor.email = :email")
    List<ReduccionProfesoresDto> encontrarReudccionesPorProfesor(@Param("email") String email);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto(pR.idProfesorReduccion.reduccion.idReduccion.nombre, pR.idProfesorReduccion.reduccion.idReduccion.horas) " +
            "FROM ProfesorReduccion pR " +
            "WHERE pR.idProfesorReduccion.profesor.email = :email AND pR.idProfesorReduccion.reduccion.idReduccion.nombre = :nombre AND pR.idProfesorReduccion.reduccion.idReduccion.horas = :horas")
    ReduccionProfesoresDto encontrarReudccionPorProfesor(@Param("email") String email,
                                                         @Param("nombre") String nombre,
                                                         @Param("horas") Integer horas);

}
