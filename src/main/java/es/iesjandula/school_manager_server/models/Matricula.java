package es.iesjandula.school_manager_server.models;

import es.iesjandula.school_manager_server.models.ids.IdMatricula;
import jakarta.persistence.*;
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

    @ManyToOne
    @MapsId("asignatura") // Este valor debe coincidir con el nombre del atributo en IdMatricula
    @JoinColumns({
            @JoinColumn(name = "asignatura_curso", referencedColumnName = "curso"),
            @JoinColumn(name = "asignatura_etapa", referencedColumnName = "etapa"),
            @JoinColumn(name = "asignatura_grupo", referencedColumnName = "grupo"),
            @JoinColumn(name = "asignatura_nombre", referencedColumnName = "nombre")
    })
    private Asignatura asignatura;
}
