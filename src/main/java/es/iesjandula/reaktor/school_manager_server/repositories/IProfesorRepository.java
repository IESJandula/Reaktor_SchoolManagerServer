package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Profesor findByEmail(String email);

    @Query("SELECT p.departamento.nombre " +
            "FROM Profesor p " +
            "WHERE p.email = :email")
    String buscarDepartamentoPorEmail(@Param("email") String email);


    /**
     * Método que busca los profesores con la suma de horas de docencia y reducciones con el mínimo legal
     * @return - Lista de profesores con la suma de horas de docencia y reducciones con el mínimo legal
     */
    @Query("SELECT p " +
           "FROM Profesor p " +
           "WHERE p.email IN (" +
           "    SELECT DISTINCT i.profesor.email " +
           "    FROM Impartir i " +
           "    WHERE ((" +
           "        SELECT SUM(i2.cupoHoras) " +
           "        FROM Impartir i2 " +
           "        WHERE i2.profesor.email = i.profesor.email" +
           "    ) + COALESCE((" +
           "        SELECT SUM(pr.reduccion.idReduccion.horas) " +
           "        FROM ProfesorReduccion pr " +
           "        WHERE pr.profesor.email = i.profesor.email" +
           "    ), 0)) < 18 OR ((" +
           "        SELECT SUM(i2.cupoHoras) " +
           "        FROM Impartir i2 " +
           "        WHERE i2.profesor.email = i.profesor.email" +
           "    ) + COALESCE((" +
           "        SELECT SUM(pr.reduccion.idReduccion.horas) " +
           "        FROM ProfesorReduccion pr " +
           "        WHERE pr.profesor.email = i.profesor.email" +
           "    ), 0)) > 21" +
           ")")
    Optional<List<Profesor>> profesorConSumaHorasDocenciaReduccionesIncorrectas();    
}
