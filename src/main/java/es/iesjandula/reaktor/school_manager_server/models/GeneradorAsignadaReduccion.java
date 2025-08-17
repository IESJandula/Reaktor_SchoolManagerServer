package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorAsignadaReduccion;
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
@Table(name = "GeneradorAsignadaReduccion")
public class GeneradorAsignadaReduccion
{
    @EmbeddedId
    private IdGeneradorAsignadaReduccion idGeneradorAsignadaReduccion;

    /**
     * Identificador de la instancia del generador a la que pertenece esta asignación.
     * Relación de muchos a uno con la entidad {@link GeneradorInstancia}.
     */
    @MapsId(value = "idGeneradorInstancia")
    @ManyToOne
    private GeneradorInstancia generadorInstancia;

    /**
     * Reducción que está siendo aplicada al profesor.
     * Relación de muchos a uno con la entidad {@link Reduccion}.
     */
    @MapsId(value = "reduccion")
    @ManyToOne
    private Reduccion reduccion;
    
    /**
     * Profesor que está asignado para aplicar la reducción.
     * Relación de muchos a uno con la entidad {@link Profesor}.
     */
    @MapsId(value = "profesor")
    @ManyToOne
    private Profesor profesor;

    /**
     * Día de la semana y tramo horario.
     * Relación de muchos a uno con la entidad {@link DiaTramoTipoHorario}.
     */
    @MapsId(value = "diaTramoTipoHorario")
    @ManyToOne
    private DiaTramoTipoHorario diaTramoTipoHorario;
}
