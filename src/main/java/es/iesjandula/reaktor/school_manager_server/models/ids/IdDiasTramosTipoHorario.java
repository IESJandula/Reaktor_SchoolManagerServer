package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdDiasTramosTipoHorario implements Serializable
{
    /** Día de la semana */
    @Column(length = 10)
    private int dia ;

    /** Tramo de la jornada */
    @Column(length = 1)
    private int tramo ;

    /** Indica si el horario es matutino */
    @Column(length = 1)
    private boolean horarioMatutino ;
}
