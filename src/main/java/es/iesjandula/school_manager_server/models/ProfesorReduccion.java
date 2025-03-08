package es.iesjandula.school_manager_server.models;

import es.iesjandula.school_manager_server.models.ids.IdProfesorReduccion;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - ProfesorReduccion
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa una reducción de horas de un profesor. Cada registro en esta clase establece una relación entre
 * un profesor y una reducción de horas específica. Utiliza una clave compuesta para identificar el registro.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Profesor_Reduccion")
public class ProfesorReduccion 
{
	/**
	 * Clave compuesta que identifica el registro de reducción de horas de un profesor.
	 * Se compone de las entidades {@link Profesor} y {@link Reduccion}.
	 */
	@EmbeddedId
	private IdProfesorReduccion idProfesorReduccion;
	
	/**
	 * Profesor al que se le asigna la reducción de horas. Relación de muchos a uno con la entidad {@link Profesor}.
	 */
	@MapsId(value = "profesor")
	@ManyToOne
	private Profesor profesor;
	
	/**
	 * Reducción de horas que se asigna al profesor. Relación de muchos a uno con la entidad {@link Reduccion}.
	 */
	@MapsId(value = "reduccion")
	@ManyToOne
	private Reduccion reduccion;

}
