package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link es.iesjandula.reaktor.school_manager_server.models.CursoEtapa}.
 * <p>
 * Incluye {@code cursoAcademico}: cadena vacía ({@link Constants#CURSO_ACADEMICO_GLOBAL}) para registros globales de
 * matrículas/horarios; valor real (p. ej. {@code 2025/2026}) para el catálogo gestionado desde espacios.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IdCursoEtapa implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Curso académico al que pertenece el registro. {@link Constants#CURSO_ACADEMICO_GLOBAL} para datos globales.
	 */
	@Column(length = 9)
	private String cursoAcademico;

	/**
	 * Año o número del curso al que pertenece la etapa.
	 */
	@Column
	private int curso;

	/**
	 * Etapa educativa asociada (por ejemplo, "Primaria", "Secundaria").
	 */
	@Column(length = 50)
	private String etapa;
}
