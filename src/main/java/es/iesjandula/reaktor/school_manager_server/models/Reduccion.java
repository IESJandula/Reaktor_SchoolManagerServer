package es.iesjandula.reaktor.school_manager_server.models;

import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - Reduccion
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa una reducción de horas en la que los profesores pueden estar involucrados. Una reducción 
 * especifica un número de horas a las que un profesor puede acogerse, y puede estar asociada a la decisión de la 
 * dirección en su asignación.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Reduccion")
public class Reduccion 
{
	@EmbeddedId
	private IdReduccion idReduccion;
	
	/**
	 * Indica si la reducción ha sido decidida por la dirección. Este valor es opcional.
	 */
	@Column(nullable = true)
	private boolean decideDireccion;
	
	/**
	 * Relación uno a muchos con la entidad {@link ProfesorReduccion}, que representa los profesores que han sido 
	 * asignados a esta reducción.
	 */
	@OneToMany(mappedBy = "reduccion")
	private List<ProfesorReduccion> profesorReducciones;
}
