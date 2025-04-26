package es.iesjandula.reaktor.school_manager_server.models.ids;

import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo; // Import necesario
import jakarta.persistence.*; // Import necesario
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Clase Embeddable que representa la clave primaria compuesta para la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase se utiliza con la anotación @EmbeddedId en la entidad Asignatura.
 * Contiene los campos que forman la clave primaria: la relación con CursoEtapaGrupo
 * y el nombre de la asignatura.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class IdAsignatura implements Serializable
{
	/** Serialización de la clase para persistencia */
	private static final long serialVersionUID = 2L;

	/**
	 * Relación con CursoEtapaGrupo que forma parte de la clave primaria.
	 * Mapeada aquí dentro de la clase Embeddable.
	 */
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "curso", referencedColumnName = "curso"),
        @JoinColumn(name = "etapa", referencedColumnName = "etapa"),
        @JoinColumn(name = "grupo", referencedColumnName = "grupo")
    })
	private CursoEtapaGrupo cursoEtapaGrupo ;

	/** Nombre de la asignatura que forma parte de la clave primaria */
	@Column(length = 100)
	private String nombre ;
}
