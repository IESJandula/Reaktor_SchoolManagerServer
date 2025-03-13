package es.iesjandula.school_manager_server.models;

import es.iesjandula.school_manager_server.models.ids.IdMatricula;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - Matricula
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la matrícula de un alumno en una asignatura específica. La relación entre las entidades se
 * maneja a través de un identificador compuesto {@link IdMatricula}, que está formado por varios atributos relacionados
 * con la asignatura y el alumno.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Matricula")
public class Matricula {

    /**
     * Identificador compuesto para la matrícula, que incluye la asignatura y el alumno.
     * La clave primaria se compone de diferentes atributos relacionados con la asignatura.
     */
    @EmbeddedId
    private IdMatricula idMatricula;

}
