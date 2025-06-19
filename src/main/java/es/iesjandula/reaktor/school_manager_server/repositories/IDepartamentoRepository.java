package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.Departamento;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Departamento}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Departamento}.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IDepartamentoRepository extends JpaRepository<Departamento, String>
{
	
	/**
     * Busca un Departamento por su nombre.
     * @param nombre Nombre del departamento.
     * @return Departamento encontrado o null si no existe.
     */
    @Query("SELECT d FROM Departamento d WHERE d.nombre = :nombre")
    Optional<Departamento> findByNombre(@Param("nombre") String nombre);

    /**
     * Método que busca los departamentos con número de profesores en plantilla incorrecto
     * @return - Lista de departamentos con número de profesores en plantilla incorrecto
     */
    @Query("SELECT d FROM Departamento d " +
           "WHERE d.plantilla != (" +
           "   SELECT COUNT(p) FROM Profesor p " +
           "   WHERE p.departamento = d)")
    Optional<List<Departamento>> departamentoConNumeroProfesoresEnPlantillaIncorrecto();
}
