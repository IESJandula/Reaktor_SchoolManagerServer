package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.models.Alumno;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Alumno}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite realizar operaciones CRUD sobre la tabla que representa
 * la entidad {@link Alumno} sin necesidad de implementar manualmente los métodos.
 * -----------------------------------------------------------------------------------------------------------------
 * El uso de esta interfaz permite realizar operaciones de persistencia de forma sencilla, como guardar, eliminar, 
 * actualizar y buscar objetos de tipo {@link Alumno} en la base de datos.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IAlumnoRepository extends JpaRepository<Alumno, Integer>
{
	
	@Modifying
	@Transactional
	void deleteByNombreAndApellidos(@Param("nombre") String nombre,
									@Param("apellidos") String apellidos);

	@Modifying
	@Transactional
	void deleteByNombreAndApellidosAndId(@Param("nombre") String nombre,
									@Param("apellidos") String apellidos,
										 @Param("id") Integer id);
	
}
