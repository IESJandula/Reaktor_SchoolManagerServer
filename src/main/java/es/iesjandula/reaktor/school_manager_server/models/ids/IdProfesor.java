package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta (@IdClass) para la entidad
 * {@link es.iesjandula.reaktor.school_manager_server.models.Profesor}.
 * <p>
 * La clave primaria del profesor pasa a ser compuesta {@code (cursoAcademico, email)} de modo que un mismo profesor
 * (mismo email) puede existir de forma independiente en distintos cursos académicos. El {@code cursoAcademico} NO
 * viaja en el CSV ni en los DTO de Firebase: lo resuelve internamente el servidor (curso académico seleccionado,
 * vía {@code CursoAcademicoResolver}) en el momento de persistir.
 * </p>
 * <p>
 * Se usa {@code @IdClass} en lugar de {@code @EmbeddedId} para que la entidad mantenga {@code email} como campo
 * directo y no romper las numerosas consultas JPQL ({@code p.email}, {@code i.profesor.email}, …) ni los accesos
 * {@code getEmail()/setEmail()} existentes (mismo patrón que {@link IdDepartamento}).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdProfesor implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Curso académico al que pertenece el profesor. {@link Constants#CURSO_ACADEMICO_GLOBAL} para datos globales,
	 * aunque los profesores se persisten bajo el curso académico seleccionado (p. ej. {@code 2025/2026}).
	 */
	private String cursoAcademico;

	/**
	 * Correo electrónico del profesor.
	 */
	private String email;
}
