package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacioDesdoble;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

/**
 * Entidad - EspacioDesdoble
 * -----------------------------------------------------------------------------------------------------------------
 * Representa la ASIGNACIÓN de un aula del catálogo del instituto como aula de desdoble a un bloque de optativas.
 * <p>
 * A diferencia de {@link EspacioFijo} (aula de referencia, que SÍ consume el espacio), un aula de desdoble NO se
 * consume: el mismo espacio del catálogo puede asignarse como desdoble a varios bloques (las optativas de bloques
 * distintos pueden compartir aula). Por eso esta entidad NO forma parte de la jerarquía de {@link Espacio} (ya no
 * es un "tipo" de espacio del catálogo que mueva el aula de tabla), sino que es una entidad de RELACIÓN
 * espacio ↔ bloque ↔ asignatura con clave primaria compuesta {@link IdEspacioDesdoble} =
 * (cursoAcademico, nombre, bloqueId, asignatura). La asignación es por asignatura: cada asignatura de un bloque
 * admite como mucho un aula de desdoble.
 * <p>
 * Al asignar un desdoble, el espacio permanece en el catálogo (tabla Espacio_Sin_Docencia), de modo que sigue
 * estando disponible para asignarse como desdoble a otros bloques. Lo único que "consume" un aula del pool es
 * pasar a ser aula de referencia ({@link EspacioFijo}).
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@Entity
@Table(name = "Espacio_Desdoble")
public class EspacioDesdoble
{
	/**
	 * Clave primaria compuesta de la asignación: espacio (cursoAcademico + nombre) + bloque.
	 */
	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "espacioId.cursoAcademico", column = @Column(name = "cursoAcademico", length = 9)),
			@AttributeOverride(name = "espacioId.nombre", column = @Column(name = "nombre", length = 100)),
			@AttributeOverride(name = "bloqueId", column = @Column(name = "bloque_id")),
			@AttributeOverride(name = "asignatura", column = @Column(name = "asignatura", length = 100))
	})
	private IdEspacioDesdoble idEspacioDesdoble;

	/**
	 * Curso académico al que pertenece el espacio (FK sobre la columna de la clave).
	 */
	@ManyToOne
	@JoinColumn(name = "cursoAcademico", referencedColumnName = "cursoAcademico", insertable = false, updatable = false)
	@ToString.Exclude
	private CursoAcademico cursoAcademico;

	/**
	 * Bloque (conjunto de optativas) al que se vincula el aula de desdoble (FK sobre la columna de la clave).
	 */
	@ManyToOne
	@JoinColumn(name = "bloque_id", referencedColumnName = "id", insertable = false, updatable = false)
	@ToString.Exclude
	private Bloque bloque;
}
