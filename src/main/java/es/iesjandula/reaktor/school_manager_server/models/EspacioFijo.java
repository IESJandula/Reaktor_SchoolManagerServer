package es.iesjandula.reaktor.school_manager_server.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad - EspacioFijo
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa un espacio fijo asociado a un grupo concreto (curso, etapa y grupo). Hereda la clave
 * primaria compuesta de {@link Espacio} y mapea la tabla "Espacio_Fijo".
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@Entity
@Table(name = "Espacio_Fijo")
@EqualsAndHashCode(callSuper = true)
public class EspacioFijo extends Espacio
{
	/**
	 * Curso académico al que pertenece el espacio.
	 */
	@ManyToOne
	@JoinColumn(name = "cursoAcademico", referencedColumnName = "cursoAcademico", insertable = false, updatable = false)
	private CursoAcademico cursoAcademico;

	/**
	 * Curso, etapa y grupo asociado al espacio fijo.
	 */
	@ManyToOne
	@JoinColumns({
			@JoinColumn(name = "grupoCursoAcademico", referencedColumnName = "cursoAcademico"),
			@JoinColumn(name = "curso", referencedColumnName = "curso"),
			@JoinColumn(name = "etapa", referencedColumnName = "etapa"),
			@JoinColumn(name = "grupo", referencedColumnName = "grupo")
	})
	private CursoEtapaGrupo cursoEtapaGrupo;
}
