package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO - BloqueConAsignaturasDto
 * -----------------------------------------------------------------------------------------------------------------
 * Representa un bloque con el listado de nombres de las asignaturas que contiene. Se usa en el paso de creación de
 * grupos para que dirección seleccione un bloque y vea las asignaturas asociadas antes de asignarle un aula de
 * desdoble.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloqueConAsignaturasDto
{
	/** Identificador del bloque. */
	private Long bloqueId;

	/** Nombre legible del bloque (por ejemplo, "Bloque 3"). */
	private String nombre;

	/** Nombres de las asignaturas asociadas al bloque. */
	private List<String> asignaturas;
}
