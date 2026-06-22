package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Opciones de copia entre cursos académicos desde configuración básica.
 * <p>
 * Valores admitidos en {@link #opciones}: {@code cursos_etapas}, {@code asignaturas}, {@code reducciones}.
 * </p>
 */
@Data
@NoArgsConstructor
public class CopiarCursoAcademicoDto
{
	private List<String> opciones;
}
