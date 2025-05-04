package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.models.Alumno;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import jakarta.persistence.CascadeType;
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
public class IdMatricula implements Serializable
{

    /**
     * Atributo único de serialización para la clase.
     */
	private static final long serialVersionUID = 5643290309503852464L;

	/**
     * Asignatura en la que está matriculado el alumno. Relación de muchos a uno con la entidad {@link Asignatura}.
     * Se usa {@link JoinColumns} para mapear varias columnas que corresponden a los atributos de la asignatura.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumns({
    	@JoinColumn(name = "curso", referencedColumnName = "curso"),
		@JoinColumn(name = "etapa", referencedColumnName = "etapa"),
		@JoinColumn(name = "grupo", referencedColumnName = "grupo"),
		@JoinColumn(name = "nombre", referencedColumnName = "nombre")
	})
	private Asignatura asignatura;

    /**
     * El alumno que está matriculado en la asignatura.
     * Representa una relación con la entidad {@link Alumno}.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "alumno_id", referencedColumnName = "id")
    private Alumno alumno;
}
