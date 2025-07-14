package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorInstanciaSolucionInfoProfesor;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "GeneradorInstanciaSolucionInfoProfesor")
public class GeneradorInstanciaSolucionInfoProfesor
{
    /** Identificador compuesto para la relación entre {@link GeneradorInstancia} y {@link Profesor}.
     * El identificador se compone de la {@link GeneradorInstancia} y el {@link Profesor} que imparten la asignatura.
     */
    @EmbeddedId
    private IdGeneradorInstanciaSolucionInfoProfesor idGeneradorInstanciaSolucionInfoProfesor ;

    /** Puntuación matutina */
    private double puntuacionMatutina ;

    /** Puntuación vespertina */
    private double puntuacionVespertina ;

    /** Porcentaje matutina */
    private double porcentajeMatutina ;

    /** Porcentaje vespertina */
    private double porcentajeVespertina ;
}
