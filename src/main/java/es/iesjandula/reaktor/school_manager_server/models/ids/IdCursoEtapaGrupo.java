package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link CursoEtapaGrupoDto}.
 * -----------------------------------------------------------------------------------------------------------------
 * La clase {@link IdCursoEtapaGrupo} es utilizada como clave primaria compuesta para la entidad {@link CursoEtapaGrupoDto}.
 * Esta clave primaria consta de tres atributos: curso, etapa y grupo, los cuales juntos identifican de manera única una 
 * combinación específica de curso, etapa educativa y grupo dentro del sistema.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdCursoEtapaGrupo implements Serializable
{
	/**
	 * Serialización de la clase para persistencia.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Número o año del curso al que pertenece la etapa.
	 */
	@Column(length = 1)
	private int curso;
	
	/**
	 * Etapa educativa asociada (por ejemplo, "Bachillerato").
	 */
	@Column(length = 20)
	private String etapa;
	
	/**
	 * Grupo específico dentro de la etapa (por ejemplo, "A", "B").
	 */
	@Column(length = 1)
	private char grupo;
}
