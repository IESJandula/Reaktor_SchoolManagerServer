package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.Reduccion;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Reduccion}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Reduccion}. La clave primaria de la entidad {@link Reduccion} está compuesta por un {@link String}, que
 * representa el {@code nombre} de la reducción.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IReduccionRepository extends JpaRepository<Reduccion, String>
{

}
