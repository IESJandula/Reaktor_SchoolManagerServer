package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacio;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Data;

/**
 * Entidad - Espacio
 * -----------------------------------------------------------------------------------------------------------------
 * Clase abstracta que representa un espacio dentro del sistema. Define la clave primaria compuesta común a todos
 * los tipos de espacio mediante la clase {@link IdEspacio}.
 * <p>Utiliza la estrategia de herencia {@link InheritanceType#TABLE_PER_CLASS}, de modo que cada subtipo de espacio
 * (sin docencia, fijo y desdoble) se mapea en su propia tabla.</p>
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Espacio
{
	/**
	 * Clave primaria compuesta del espacio.
	 */
	@EmbeddedId
	private IdEspacio espacioId;
}
