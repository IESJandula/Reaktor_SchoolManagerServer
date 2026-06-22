package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO - BloqueAsignaturaDto
 * -----------------------------------------------------------------------------------------------------------------
 * Proyección plana (fila a fila) que asocia el identificador de un bloque con el nombre de una de sus asignaturas.
 * Se utiliza como paso intermedio para agrupar las asignaturas por bloque en {@link BloqueConAsignaturasDto}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloqueAsignaturaDto
{
	/** Identificador del bloque. */
	private Long bloqueId;

	/** Nombre de la asignatura asociada al bloque. */
	private String asignatura;
}
