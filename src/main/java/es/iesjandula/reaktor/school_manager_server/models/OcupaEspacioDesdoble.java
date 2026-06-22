package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdOcupaEspacioDesdoble;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

/**
 * Entidad - OcupaEspacioDesdoble
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la relación entre un curso, etapa y grupo y un espacio desdoble, indicando qué grupos
 * ocupan un espacio desdoble. Mapea la tabla "Ocupa_Espacio_Desdoble".
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@Entity
@Table(name = "Ocupa_Espacio_Desdoble")
public class OcupaEspacioDesdoble
{
	/**
	 * Clave primaria compuesta de la relación.
	 */
	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "idCursoEtapaGrupo.cursoAcademico", column = @Column(name = "grupoCursoAcademico", length = 9)),
			@AttributeOverride(name = "idCursoEtapaGrupo.curso", column = @Column(name = "curso")),
			@AttributeOverride(name = "idCursoEtapaGrupo.etapa", column = @Column(name = "etapa", length = 50)),
			@AttributeOverride(name = "idCursoEtapaGrupo.grupo", column = @Column(name = "grupo", length = 20)),
			@AttributeOverride(name = "espacioId.cursoAcademico", column = @Column(name = "cursoAcademico", length = 9)),
			@AttributeOverride(name = "espacioId.nombre", column = @Column(name = "nombre", length = 100))
	})
	private IdOcupaEspacioDesdoble idOcupaEspacioDesdoble;

	/**
	 * Curso, etapa y grupo que ocupa el espacio desdoble.
	 */
	@ManyToOne
	@JoinColumns({
			@JoinColumn(name = "grupoCursoAcademico", referencedColumnName = "cursoAcademico", insertable = false, updatable = false),
			@JoinColumn(name = "curso", referencedColumnName = "curso", insertable = false, updatable = false),
			@JoinColumn(name = "etapa", referencedColumnName = "etapa", insertable = false, updatable = false),
			@JoinColumn(name = "grupo", referencedColumnName = "grupo", insertable = false, updatable = false)
	})
	private CursoEtapaGrupo cursoEtapaGrupo;

	// NOTA: ya no se mantiene una asociación @ManyToOne directa a EspacioDesdoble. Desde que el desdoble es una
	// relación espacio ↔ bloque, la clave primaria de EspacioDesdoble es (cursoAcademico, nombre, bloque_id) y no
	// puede referenciarse únicamente por (cursoAcademico, nombre). El espacio ocupado queda identificado por las
	// columnas cursoAcademico/nombre de la clave embebida (idOcupaEspacioDesdoble.espacioId).
}
