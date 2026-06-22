package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación/borrado de asignaturas ad-hoc (a medida) desde la vista de matrículas.
 * <p>
 * Una asignatura ad-hoc pertenece a un curso, etapa y curso académico (los grupos no se tienen en cuenta aquí).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaAdHocDto
{
	/** Nombre de la asignatura. */
	private String nombre;

	/** Curso al que pertenece la asignatura. */
	private Integer curso;

	/** Etapa educativa a la que pertenece la asignatura. */
	private String etapa;
}
