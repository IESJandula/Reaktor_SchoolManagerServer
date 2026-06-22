package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta (@IdClass) para la entidad
 * {@link es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales}.
 * <p>
 * Tras hacer compuesta la clave de {@code Profesor} ({@code cursoAcademico, email}), la relación uno a uno con el
 * profesor exige una FK compuesta, por lo que la PK de las observaciones pasa a estar formada por
 * {@code (profesorCursoAcademico, profesorEmail)}.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdObservacionesAdicionales implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Curso académico del profesor. */
	private String profesorCursoAcademico;

	/** Email del profesor. */
	private String profesorEmail;
}
