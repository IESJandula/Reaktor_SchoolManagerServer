package es.iesjandula.matriculas_horarios_server.models.ids;

import java.io.Serializable;

import es.iesjandula.matriculas_horarios_server.models.Alumno;
import es.iesjandula.matriculas_horarios_server.models.Asignatura;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link Matricula}.
 * -----------------------------------------------------------------------------------------------------------------
 * La clase {@link IdMatricula} define una clave primaria compuesta que es utilizada en la entidad {@link Matricula}.
 * Esta clave está compuesta por dos atributos: {@link Asignatura} y {@link Alumno}. Juntos, estos atributos identifican 
 * de manera única la matrícula de un alumno en una asignatura.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdMatricula implements Serializable {

    /**
     * Atributo único de serialización para la clase.
     */
    private static final long serialVersionUID = 1L;

    /**
     * La asignatura en la que el alumno está matriculado.
     * Representa una relación con la entidad {@link Asignatura}.
     */
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "asignatura_curso", referencedColumnName = "curso"),
        @JoinColumn(name = "asignatura_etapa", referencedColumnName = "etapa"),
        @JoinColumn(name = "asignatura_grupo", referencedColumnName = "grupo"),
            @JoinColumn(name = "asignatura_horas", referencedColumnName = "horas"),
        @JoinColumn(name = "asignatura_nombre", referencedColumnName = "nombre")
    })
    private Asignatura asignatura;

    /**
     * El alumno que está matriculado en la asignatura.
     * Representa una relación con la entidad {@link Alumno}.
     */
    @ManyToOne
    @JoinColumn(name = "alumno_id", referencedColumnName = "id")
    private Alumno alumno;
}
