package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase Embeddable que representa la clave primaria compuesta para la entidad
 * {@link es.iesjandula.reaktor.school_manager_server.models.EspacioDesdoble}.
 * -----------------------------------------------------------------------------------------------------------------
 * Un aula de desdoble NO se consume: como se usa para optativas, un mismo espacio del catálogo puede asignarse como
 * aula de desdoble a VARIOS bloques. Por ello la clave de la asignación de desdoble combina el espacio (curso
 * académico + nombre, mediante {@link IdEspacio}) con el bloque y la ASIGNATURA concreta del bloque, de modo que el
 * mismo espacio pueda repetirse para N bloques/asignaturas sin violar la clave primaria.
 * <p>
 * La asignación de desdoble es ahora POR ASIGNATURA: cada asignatura de un bloque admite como mucho un aula de
 * desdoble. Por eso la asignatura forma parte de la clave: (cursoAcademico, nombre, bloqueId, asignatura).
 * Al implementar {@link Serializable}, puede ser utilizada como clave embebida en JPA.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdEspacioDesdoble implements Serializable
{
	/** Serialización de la clase para persistencia */
	private static final long serialVersionUID = 5417202302598004695L;

	/**
	 * Espacio del catálogo (curso académico + nombre) asignado como aula de desdoble.
	 */
	private IdEspacio espacioId;

	/**
	 * Identificador del bloque (conjunto de optativas) al que se vincula el aula de desdoble.
	 */
	private Long bloqueId;

	/**
	 * Nombre de la asignatura concreta del bloque a la que se asigna el aula de desdoble. Forma parte de la clave
	 * para que cada asignatura del bloque tenga su propia aula (a lo sumo una por asignatura).
	 */
	private String asignatura;
}
