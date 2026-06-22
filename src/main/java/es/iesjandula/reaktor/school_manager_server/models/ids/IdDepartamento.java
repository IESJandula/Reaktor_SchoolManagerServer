package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta (@IdClass) para la entidad
 * {@link es.iesjandula.reaktor.school_manager_server.models.Departamento}.
 * <p>
 * Incluye {@code cursoAcademico}: cadena vacía ({@link Constants#CURSO_ACADEMICO_GLOBAL}) para los departamentos
 * globales usados por matrículas/horarios/profesores; valor real (p. ej. {@code 2025/2026}) para el catálogo
 * gestionado desde espacios.
 * </p>
 * <p>
 * Se usa {@code @IdClass} en lugar de {@code @EmbeddedId} para que la entidad mantenga {@code nombre} como campo
 * directo y no romper las numerosas consultas JPQL ({@code d.nombre}, {@code a.departamentoReceptor.nombre}, …)
 * ni los accesos {@code getNombre()/setNombre()} existentes (cambio de menor impacto).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdDepartamento implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Curso académico al que pertenece el registro. {@link Constants#CURSO_ACADEMICO_GLOBAL} para datos globales.
	 */
	private String cursoAcademico;

	/**
	 * Nombre del departamento.
	 */
	private String nombre;

	/**
	 * Constructor de compatibilidad para departamentos globales (profesores, asignaturas, reducciones).
	 *
	 * @param nombre - Nombre del departamento.
	 */
	public IdDepartamento(String nombre)
	{
		this(Constants.CURSO_ACADEMICO_GLOBAL, nombre);
	}
}
