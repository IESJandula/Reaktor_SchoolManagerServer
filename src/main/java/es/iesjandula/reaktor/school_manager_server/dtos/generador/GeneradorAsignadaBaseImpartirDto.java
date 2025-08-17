package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneradorAsignadaBaseImpartirDto extends GeneradorAsignadaBaseDto
{
    /**
     * Nombre de la asignatura.
     */
    private String asignatura ;

    /**
     * Constructor de la clase GeneradorAsignadaBaseImpartirDto.
     * @param diaDesc - Descripción del día.
     * @param tramoDesc - Descripción del tramo horario.
     * @param horarioMatutino - Indica si el grupo es matutino.
     * @param asignatura - Nombre de la asignatura.
     */
    public GeneradorAsignadaBaseImpartirDto(String diaDesc, String tramoDesc, Boolean horarioMatutino, String asignatura)
    {
        super(diaDesc, tramoDesc, horarioMatutino);

        this.asignatura = asignatura;
    }
}
