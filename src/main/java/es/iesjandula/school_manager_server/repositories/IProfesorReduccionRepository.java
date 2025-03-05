package es.iesjandula.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.school_manager_server.models.ProfesorReduccion;
import es.iesjandula.school_manager_server.models.ids.IdProfesorReduccion;

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

}
