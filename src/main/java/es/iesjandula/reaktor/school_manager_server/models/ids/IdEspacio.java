package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase Embeddable que representa la clave primaria compuesta para la entidad {@link Espacio}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria identifica de manera única un espacio mediante el curso académico y el nombre del espacio.
 * Al implementar {@link Serializable}, puede ser utilizada como clave embebida en JPA.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdEspacio implements Serializable
{
	/** Serialización de la clase para persistencia */
	private static final long serialVersionUID = 8937202302598004694L;

	/**
	 * Curso académico del espacio. Forma parte de la clave primaria compuesta.
	 */
	@Column(length = 9)
	private String cursoAcademico;

	/**
	 * Nombre del espacio. Forma parte de la clave primaria compuesta.
	 */
	@Column(length = 100)
	private String nombre;
}
