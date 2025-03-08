package es.iesjandula.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
