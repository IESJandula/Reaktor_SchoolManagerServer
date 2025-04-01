package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;

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

}
