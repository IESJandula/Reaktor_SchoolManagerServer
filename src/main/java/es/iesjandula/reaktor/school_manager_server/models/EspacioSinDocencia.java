package es.iesjandula.reaktor.school_manager_server.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad - EspacioSinDocencia
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa un espacio sin docencia. Hereda la clave primaria compuesta de {@link Espacio} y mapea
 * la tabla "Espacio_Sin_Docencia".
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@Entity
@Table(name = "Espacio_Sin_Docencia")
@EqualsAndHashCode(callSuper = true)
public class EspacioSinDocencia extends Espacio
{
	/**
	 * Curso académico al que pertenece el espacio.
	 */
	@ManyToOne
	@JoinColumn(name = "cursoAcademico", referencedColumnName = "cursoAcademico", insertable = false, updatable = false)
	private CursoAcademico cursoAcademico;
}
