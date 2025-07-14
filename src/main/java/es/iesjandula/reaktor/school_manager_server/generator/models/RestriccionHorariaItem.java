package es.iesjandula.reaktor.school_manager_server.generator.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Francisco Manuel Benítez Chico
 */
@EqualsAndHashCode
@Getter
public class RestriccionHorariaItem
{
    /** Índice Día */
    private final int indiceDia ;

    /** Índice Tramo horario */
    private final int tramoHorario ;

    /**
     * @param indiceDia Índice Día
     * @param tramoHorario Índice Tramo horario
     */
    public RestriccionHorariaItem(int indiceDia, int tramoHorario)
    {
        this.indiceDia = indiceDia ;
        this.tramoHorario = tramoHorario ;
    }
}
