package es.iesjandula.reaktor.school_manager_server.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - CursoAcademico
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad "CursoAcademico" en la base de datos, que mapea la tabla "Curso_Academico".
 * <p>Se utiliza para representar un curso académico (por ejemplo, "2025/2026") y conocer cuál es el seleccionado
 * actualmente en el sistema para la gestión de espacios.</p>
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Curso_Academico")
public class CursoAcademico
{
	/**
	 * Identificador único del curso académico.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Cadena que representa el curso académico (por ejemplo, "2025/2026").
	 */
	@Column(length = 9, unique = true)
	private String cursoAcademico;

	/**
	 * Indica si el curso académico es el seleccionado actualmente.
	 */
	@Column
	private Boolean seleccionado;

	/**
	 * Lista de espacios sin docencia asociados al curso académico.
	 */
	@OneToMany(mappedBy = "cursoAcademico")
	private List<EspacioSinDocencia> espaciosSinDocencia;

	/**
	 * Lista de espacios fijos asociados al curso académico.
	 */
	@OneToMany(mappedBy = "cursoAcademico")
	private List<EspacioFijo> espaciosFijos;

	/**
	 * Lista de espacios desdobles asociados al curso académico.
	 */
	@OneToMany(mappedBy = "cursoAcademico")
	private List<EspacioDesdoble> espaciosDesdobles;
}
