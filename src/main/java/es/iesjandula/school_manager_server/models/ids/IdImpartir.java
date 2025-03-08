package es.iesjandula.school_manager_server.models.ids;

import es.iesjandula.school_manager_server.models.Asignatura;
import es.iesjandula.school_manager_server.models.Profesor;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link Impartir}.
 * -----------------------------------------------------------------------------------------------------------------
 * La clase {@link IdImpartir} es utilizada como clave primaria compuesta para la entidad {@link Impartir}.
 * Esta clave primaria está compuesta por dos atributos: {@link Asignatura} y {@link Profesor}, los cuales juntos identifican 
 * de manera única la relación entre un profesor y una asignatura específica que imparte.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdImpartir 
{
	/**
	 * Asignatura que está siendo impartida por un profesor.
	 */
	@ManyToOne
	private Asignatura asignatura;

	/**
	 * Profesor que imparte la asignatura.
	 */
	@ManyToOne
	private Profesor profesor;
}
