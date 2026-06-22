package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO - CursoAcademicoDto
 * -----------------------------------------------------------------------------------------------------------------
 * Objeto de transferencia de datos que representa un curso académico y si está seleccionado.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursoAcademicoDto
{
	/** Cadena que representa el curso académico (por ejemplo, "2025/2026"). */
	private String cursoAcademico;

	/** Indica si el curso académico es el seleccionado actualmente. */
	private boolean seleccionado;
}
