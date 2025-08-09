package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdPreferenciasHorariasProfesor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PreferenciasHorariasProfesor
{
    @EmbeddedId
    private IdPreferenciasHorariasProfesor idPreferenciasHorariasProfesor;
    
    /**
     * Profesor asociado a estas preferencias horarias.
     * Esta propiedad se mantiene sincronizada con el profesor en la clave compuesta.
     */
    @ManyToOne
    @JoinColumn(name = "profesor_email", referencedColumnName = "email", insertable = false, updatable = false)
    private Profesor profesor;
    
    /**
     * Día y tramo horario de la preferencia.
     * Esta propiedad se mantiene sincronizada con el diaTramoTipoHorario en la clave compuesta.
     */
    @ManyToOne
    @JoinColumn(name = "dia_tramo_tipo_horario_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DiaTramoTipoHorario diaTramoTipoHorario;
}
