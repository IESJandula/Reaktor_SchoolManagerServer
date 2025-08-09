package es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions;

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

    /** Desea evitar profesor */
    private Boolean deseaEvitarProfesor ;

    /**
     * @param indiceDia Índice Día
     * @param tramoHorario Índice Tramo horario
     */
    public RestriccionHorariaItem(int indiceDia, int tramoHorario)
    {
        this.indiceDia           = indiceDia ;
        this.tramoHorario        = tramoHorario ;
        this.deseaEvitarProfesor = false ;
    }

    /**
     * Este método se debe llamar cuando se desea evitar que se elimine esta restricción horaria
     */
    public void deseaEvitarProfesor()
    {
        this.deseaEvitarProfesor = true ;
    }

    /**
     * @return true si se desea evitar, false en caso contrario
     */
    public boolean isDeseaEvitarProfesor()
    {
        return this.deseaEvitarProfesor ;
    }
}
