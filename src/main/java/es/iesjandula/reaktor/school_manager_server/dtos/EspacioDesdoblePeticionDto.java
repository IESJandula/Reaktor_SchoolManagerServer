package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO - EspacioDesdoblePeticionDto
 * -----------------------------------------------------------------------------------------------------------------
 * Objeto de transferencia de datos para asignar o desasignar un aula de desdoble. Contiene el nombre del espacio
 * y, en la asignación, el identificador del bloque al que se vincula. El curso académico se resuelve internamente
 * en el backend (seleccionado = true), por lo que el cliente no lo envía.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EspacioDesdoblePeticionDto
{
	/** Nombre del espacio. */
	private String nombre;

	/** Identificador del bloque al que se vincula el aula de desdoble (solo en la asignación). */
	private Long bloqueId;

	/** Nombre de la asignatura concreta del bloque a la que se asigna/desasigna el aula de desdoble. */
	private String asignatura;
}
