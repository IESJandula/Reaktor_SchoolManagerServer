package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorInstanciaSolucionInfoGeneral;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "GeneradorInstanciaSolucionInfoGeneral")
public class GeneradorInstanciaSolucionInfoGeneral
{
    /** Identificador compuesto para la relación entre {@link GeneradorInstancia} y {@link Profesor}.
     * El identificador se compone de la {@link GeneradorInstancia}.
     */
    @EmbeddedId
    private IdGeneradorInstanciaSolucionInfoGeneral idGeneradorInstanciaSolucionInfoGeneral ;

    /** Puntuación */
    private int puntuacion ;

    /** Porcentaje con respecto al total */
    private double porcentaje ;
}
