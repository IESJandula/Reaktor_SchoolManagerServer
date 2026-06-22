package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO - EspacioDesdobleAsignadoDto
 * -----------------------------------------------------------------------------------------------------------------
 * Representa un aula de desdoble ya asignada a un bloque dentro de un curso académico. Se usa para listar los
 * desdobles asignados y poder desasignarlos.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EspacioDesdobleAsignadoDto
{
	/** Curso académico del espacio. */
	private String cursoAcademico;

	/** Nombre del espacio. */
	private String nombre;

	/** Identificador del bloque al que está vinculado el aula de desdoble. */
	private Long bloqueId;

	/** Nombre de la asignatura concreta del bloque a la que está asignada el aula de desdoble. */
	private String asignatura;
}
