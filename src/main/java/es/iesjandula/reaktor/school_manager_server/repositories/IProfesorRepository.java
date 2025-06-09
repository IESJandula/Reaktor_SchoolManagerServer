package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.dtos.DiasTramosTipoHorarioDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;

import java.util.List;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Profesor}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Profesor}. La clave primaria de la entidad {@link Profesor} está compuesta por un {@link String}, que
 * representa el {@code email} del profesor.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IProfesorRepository extends JpaRepository<Profesor, String>
{

    Profesor findByEmail(String email);

    @Query("SELECT p.departamento.nombre " +
            "FROM Profesor p " +
            "WHERE p.email = :email")
    String buscarDepartamentoPorEmail(@Param("email") String email);

}
