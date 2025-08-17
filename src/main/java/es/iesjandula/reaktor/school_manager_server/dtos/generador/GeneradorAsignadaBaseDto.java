package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneradorAsignadaBaseDto
{
    /**
     * Descripción del día.
     */
    private String diaDesc ;

    /**
     * Descripción del tramo horario.
     */
    private String tramoDesc ;

    /**
     * Indica si el grupo es matutino.
     */
    private Boolean horarioMatutino ;
}
