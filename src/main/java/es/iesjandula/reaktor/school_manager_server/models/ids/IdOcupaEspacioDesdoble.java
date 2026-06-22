package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase Embeddable que representa la clave primaria compuesta para la entidad {@link OcupaEspacioDesdoble}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria identifica de manera única qué grupo (curso, etapa y grupo) ocupa un espacio desdoble,
 * combinando las claves {@link IdCursoEtapaGrupo} e {@link IdEspacio}.
 * Al implementar {@link Serializable}, puede ser utilizada como clave embebida en JPA.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdOcupaEspacioDesdoble implements Serializable
{
	/** Serialización de la clase para persistencia */
	private static final long serialVersionUID = -669397589408569809L;

	/**
	 * Clave primaria compuesta de la entidad CursoEtapaGrupo.
	 */
	private IdCursoEtapaGrupo idCursoEtapaGrupo;

	/**
	 * Clave primaria compuesta de la entidad Espacio.
	 */
	private IdEspacio espacioId;
}
