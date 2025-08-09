package es.iesjandula.reaktor.school_manager_server.models.ids;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdPreferenciasHorariasProfesor implements Serializable
{
    /** Identificador de la selección */
    private Integer idSeleccion;

	/** Email del profesor que imparte la asignatura */
	@Column(name = "profesor_email")
	private String profesorEmail;

	/** ID del día, tramo y tipo horario */
	@Column(name = "dia_tramo_tipo_horario_id")
	private Long diaTramoTipoHorarioId;
}
