package es.iesjandula.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.school_manager_server.models.Bloque;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Bloque}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite realizar operaciones CRUD sobre la tabla que representa
 * la entidad {@link Bloque}.
 * -----------------------------------------------------------------------------------------------------------------
 * El uso de esta interfaz facilita la persistencia y la manipulación de objetos {@link Bloque} en la base de datos,
 * sin necesidad de implementar manualmente los métodos.
 * -----------------------------------------------------------------------------------------------------------------
 * La clave primaria de la entidad {@link Bloque} es de tipo {@link String}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IBloqueRepository extends JpaRepository<Bloque, String>
{
	
}
