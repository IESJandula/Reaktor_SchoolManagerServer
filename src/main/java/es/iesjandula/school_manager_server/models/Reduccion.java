package es.iesjandula.school_manager_server.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
	/**
	 * Nombre de la reducción. Este es un identificador único de la reducción.
	 */
	@Id
	@Column(length = 100)
	private String nombre;
	
	/**
	 * Número de horas que se reducen para el profesor. Este valor es obligatorio.
	 */
	@Column(nullable = false)
	private int horas;
	
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
