package es.iesjandula.reaktor.school_manager_server.models.ids;

import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
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

	/** Profesor que imparte la asignatura */
	@ManyToOne
	private Profesor profesor;

	/** Día de la semana y tramo horario */
	@ManyToOne
	private DiaTramoTipoHorario diaTramoTipoHorario ;
}
