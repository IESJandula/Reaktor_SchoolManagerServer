package es.iesjandula.reaktor.school_manager_server.models;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDiasTramosTipoHorario;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DiasTramosTipoHorario")
public class DiasTramosTipoHorario
{
    /**
     * Identificador compuesto que representa el dia, tramo y tipo de horario.
     * <p>Este campo se mapea como la clave primaria compuesta de la entidad {@link DiasTramosTipoHorario} mediante la clase {@link IdDiasTramosTipoHorario}.</p>
     */
    @EmbeddedId
    private IdDiasTramosTipoHorario idDiasTramosTipoHorario;

    /**
     * Descripción del día.
     * <p>Este campo se mapea como la columna "diasDesc" de la tabla "DiasTramosTipoHorario".</p>
     */
    @Column(name = "diasDesc")
    private String diasDesc;

    /**
     * Descripción del tramo.
     * <p>Este campo se mapea como la columna "tramosDesc" de la tabla "DiasTramosTipoHorario".</p>
     */
    @Column(name = "tramosDesc")
    private String tramosDesc;
}
