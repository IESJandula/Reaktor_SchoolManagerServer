package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 * La clase {@link IdAsignatura} es utilizada como clave primaria compuesta para la entidad {@link Asignatura}. 
 * Esta clave primaria consta de varios atributos que identifican de manera única una asignatura en un curso, etapa 
 * y grupo determinado.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdAsignatura implements Serializable
{
	/**
	 * Serialización de la clase para persistencia.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Año o número del curso al que pertenece la asignatura.
	 */
	@Column
	private int curso;
	
	/**
	 * Etapa educativa a la que pertenece la asignatura (por ejemplo, "Primaria", "Secundaria").
	 */
	@Column(length = 50)
	private String etapa;
	
	/**
	 * Grupo al que pertenece la asignatura (por ejemplo, "A", "B").
	 */
	@Column
	private Character grupo;

	
	/**
	 * Nombre de la asignatura (por ejemplo, "Matemáticas", "Física").
	 */
	@Column(length = 100)
	private String nombre;


}
