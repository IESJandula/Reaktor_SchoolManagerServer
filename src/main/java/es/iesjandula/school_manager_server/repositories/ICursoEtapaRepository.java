package es.iesjandula.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.school_manager_server.models.CursoEtapa;
import es.iesjandula.school_manager_server.models.ids.IdCursoEtapa;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link CursoEtapa}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla 
 * correspondiente a la entidad {@link CursoEtapa}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface ICursoEtapaRepository extends JpaRepository<CursoEtapa, IdCursoEtapa>
{	
	
}
