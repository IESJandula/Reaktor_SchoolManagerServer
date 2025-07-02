package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;
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
@Table(name = "GeneradorSesionAsignada")
public class GeneradorSesionAsignada
{
    @EmbeddedId
    private IdGeneradorSesionAsignada idGeneradorSesionAsignada;

    /**
     * Identificador de la instancia del generador a la que pertenece esta asignación.
     * Relación de muchos a uno con la entidad {@link GeneradorInstancia}.
     */
    @MapsId(value = "idGeneradorInstancia")
    @ManyToOne
    private GeneradorInstancia generadorInstancia;

    /**
     * Asignatura que está siendo impartida por el profesor.
     * Relación de muchos a uno con la entidad {@link Asignatura}.
     */
    @MapsId(value = "asignatura")
    @ManyToOne
    private Asignatura asignatura;
    
    /**
     * Profesor que está asignado para impartir la asignatura.
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
