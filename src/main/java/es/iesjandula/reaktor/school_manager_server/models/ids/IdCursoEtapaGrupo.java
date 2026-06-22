package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo}.
 * <p>
 * Incluye {@code cursoAcademico}: {@link Constants#CURSO_ACADEMICO_GLOBAL} para grupos docentes globales (A, B, Optativas…);
 * valor real del curso académico para filas de catálogo curso/etapa (grupo {@link Constants#GRUPO_CATALOGO_CURSO_ETAPA}).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IdCursoEtapaGrupo implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Curso académico al que pertenece el registro. {@link Constants#CURSO_ACADEMICO_GLOBAL} para grupos docentes globales.
	 */
	@Column(length = 9)
	private String cursoAcademico;

	/**
	 * Número o año del curso al que pertenece la etapa.
	 */
	@Column(length = 1)
	private int curso;

	/**
	 * Etapa educativa asociada (por ejemplo, "Bachillerato").
	 */
	@Column(length = 50)
	private String etapa;

	/**
	 * Grupo específico dentro de la etapa (por ejemplo, "A", "B") o catálogo ({@link Constants#GRUPO_CATALOGO_CURSO_ETAPA}).
	 */
	@Column(length = 20)
	private String grupo;
}
