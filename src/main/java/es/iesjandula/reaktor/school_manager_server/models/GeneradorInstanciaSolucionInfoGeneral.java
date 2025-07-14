package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorInstanciaSolucionInfoGeneral;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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

    /** Puntuación matutina */
    private double puntuacionMatutina ;

    /** Puntuación vespertina */
    private double puntuacionVespertina ;

    /** Porcentaje matutina */
    private double porcentajeMatutina ;

    /** Porcentaje vespertina */
    private double porcentajeVespertina ;
}
