package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesor;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Profesor}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Profesor}. La clave primaria de la entidad {@link Profesor} es compuesta {@link IdProfesor}
 * ({@code cursoAcademico, email}).
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IProfesorRepository extends JpaRepository<Profesor, IdProfesor>
{

    /**
     * Busca un profesor por su clave primaria compuesta (curso académico + email).
     * @param cursoAcademico - Curso académico seleccionado.
     * @param email - Email del profesor.
     * @return el profesor encontrado o {@code null}.
     */
    Profesor findByCursoAcademicoAndEmail(String cursoAcademico, String email);

    @Query("SELECT p.departamento.nombre " +
            "FROM Profesor p " +
            "WHERE p.cursoAcademico = :cursoAcademico AND p.email = :email")
    String buscarDepartamentoPorEmail(@Param("cursoAcademico") String cursoAcademico, @Param("email") String email);


    /**
     * Método que busca los profesores (del curso académico indicado) con la suma de horas de docencia y reducciones
     * fuera del mínimo/máximo legal. Las correlaciones se hacen por la entidad {@code profesor} completa (no solo por
     * email) para respetar la clave primaria compuesta y no mezclar profesores de distintos cursos académicos.
     * @param cursoAcademico - Curso académico seleccionado.
     * @return - Lista de profesores con la suma de horas de docencia y reducciones incorrectas.
     */
    @Query("SELECT p " +
           "FROM Profesor p " +
           "WHERE p.cursoAcademico = :cursoAcademico AND (" +
           "p NOT IN (SELECT i.profesor FROM Impartir i) OR " +
           "p IN (" +
           "    SELECT DISTINCT i.profesor " +
           "    FROM Impartir i " +
           "    WHERE ((" +
           "        SELECT SUM(i2.cupoHoras) " +
           "        FROM Impartir i2 " +
           "        WHERE i2.profesor = i.profesor" +
           "    ) + COALESCE((" +
           "        SELECT SUM(pr.reduccion.idReduccion.horas) " +
           "        FROM ProfesorReduccion pr " +
           "        WHERE pr.profesor = i.profesor" +
           "    ), 0)) < 18 OR ((" +
           "        SELECT SUM(i2.cupoHoras) " +
           "        FROM Impartir i2 " +
           "        WHERE i2.profesor = i.profesor" +
           "    ) + COALESCE((" +
           "        SELECT SUM(pr.reduccion.idReduccion.horas) " +
           "        FROM ProfesorReduccion pr " +
           "        WHERE pr.profesor = i.profesor" +
           "    ), 0)) > 21" +
           "))")
    Optional<List<Profesor>> profesorConSumaHorasDocenciaReduccionesIncorrectas(@Param("cursoAcademico") String cursoAcademico);    
}
