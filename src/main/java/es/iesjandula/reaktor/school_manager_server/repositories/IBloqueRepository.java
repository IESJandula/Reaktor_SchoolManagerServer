package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.models.Bloque;

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

	/**
	 * Borra los bloques que se han quedado sin asignaturas asociadas (huérfanos).
	 * <p>
	 * Pensado para limpiar tras un borrado en cascada de Asignaturas, en el que un mismo bloque
	 * puede ser compartido por varias asignaturas y, por tanto, no podemos cascadearlo desde la FK.
	 */
	@Modifying
	@Transactional
	@Query("DELETE FROM Bloque b WHERE NOT EXISTS (SELECT 1 FROM Asignatura a WHERE a.bloqueId = b)")
	void deleteBloquesSinAsignaturas();
}
