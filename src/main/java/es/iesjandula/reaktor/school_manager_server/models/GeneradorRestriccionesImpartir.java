package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorRestriccionesImpartir;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "GeneradorRestriccionesImpartir")
public class GeneradorRestriccionesImpartir
{
    @EmbeddedId
    private IdGeneradorRestriccionesImpartir idGeneradorRestriccionesImpartir;

    /**
     * Impartir que está siendo generado por el profesor.
     * Relación de muchos a uno con la entidad {@link Impartir}.
     */
    @MapsId(value = "impartir")
    @ManyToOne
    private Impartir impartir;

    /**
     * Día de la semana y tramo horario.
     * Relación de muchos a uno con la entidad {@link DiaTramoTipoHorario}.
     */
    @MapsId(value = "diaTramoTipoHorario")
    @ManyToOne
    private DiaTramoTipoHorario diaTramoTipoHorario;
}
