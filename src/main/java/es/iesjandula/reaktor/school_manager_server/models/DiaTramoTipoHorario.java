package es.iesjandula.reaktor.school_manager_server.models;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DiaTramoTipoHorario")
public class DiaTramoTipoHorario
{
    /**
     * Identificador de la entidad.
     * <p>Este campo se mapea como la columna "id" de la tabla "DiaTramoTipoHorario".</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    /**
     * Día de la semana.
     * <p>Este campo se mapea como la columna "dia" de la tabla "DiaTramoTipoHorario".</p>
     */
    @Column(length = 10)
    private int dia ;

    /**
     * Tramo de la jornada.
     * <p>Este campo se mapea como la columna "tramo" de la tabla "DiaTramoTipoHorario".</p>
     */
    @Column(length = 1)
    private int tramo ;

    /**
     * Descripción del día.
     * <p>Este campo se mapea como la columna "diaDesc" de la tabla "DiaTramoTipoHorario".</p>
     */
    @Column(name = "diaDesc")
    private String diaDesc;

    /**
     * Descripción del tramo.
     * <p>Este campo se mapea como la columna "tramoDesc" de la tabla "DiaTramoTipoHorario".</p>
     */
    @Column(name = "tramoDesc")
    private String tramoDesc;

    /**
     * Indica si el horario es matutino.
     * <p>Este campo se mapea como la columna "horarioMatutino" de la tabla "DiaTramoTipoHorario".</p>
     */
    @Column(name = "horarioMatutino", nullable = true)
    private Boolean horarioMatutino;
}
