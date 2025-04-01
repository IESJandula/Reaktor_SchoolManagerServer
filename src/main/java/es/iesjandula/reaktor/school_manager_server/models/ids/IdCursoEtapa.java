package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link CursoEtapa}.
 * -----------------------------------------------------------------------------------------------------------------
 * La clase {@link IdCursoEtapa} es utilizada como clave primaria compuesta para la entidad {@link CursoEtapa}. 
 * Esta clave primaria consta de dos atributos: el curso y la etapa educativa, que juntos identifican de manera única una 
 * asignatura dentro de un contexto específico.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdCursoEtapa implements Serializable
{
	/**
	 * Serialización de la clase para persistencia.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Año o número del curso al que pertenece la etapa.
	 */
	@Column
	private int curso;
	
	/**
	 * Etapa educativa asociada (por ejemplo, "Primaria", "Secundaria").
	 */
	@Column(length = 50)
	private String etapa;
}
