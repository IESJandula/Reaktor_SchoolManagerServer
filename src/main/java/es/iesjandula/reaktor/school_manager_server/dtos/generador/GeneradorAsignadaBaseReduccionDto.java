package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneradorAsignadaBaseReduccionDto extends GeneradorAsignadaBaseDto
{
    /**
     * Nombre de la reducción.
     */
    private String reduccion ;

    /**
     * Constructor de la clase GeneradorAsignadaBaseReduccionDto.
     * @param diaDesc - Descripción del día.
     * @param tramoDesc - Descripción del tramo horario.
     * @param horarioMatutino - Indica si el grupo es matutino.
     * @param reduccion - Nombre de la reducción.
     */
    public GeneradorAsignadaBaseReduccionDto(String diaDesc, String tramoDesc, Boolean horarioMatutino, String reduccion)
    {
        super(diaDesc, tramoDesc, horarioMatutino);

        this.reduccion = reduccion;
    }
}
