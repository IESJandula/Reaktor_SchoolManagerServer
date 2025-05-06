package es.iesjandula.reaktor.school_manager_server.models.ids;

import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdPreferenciasHorariasProfesor implements Serializable
{
    @ManyToOne
    @JoinColumn(name = "profesor_email", referencedColumnName = "email")
    private Profesor profesor;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dia", referencedColumnName = "dia"),
            @JoinColumn(name = "tramo", referencedColumnName = "tramo"),
            @JoinColumn(name = "tipo_horario", referencedColumnName = "tipoHorario")
    })
    private DiasTramosTipoHorario diasTramosTipoHorario;
}
