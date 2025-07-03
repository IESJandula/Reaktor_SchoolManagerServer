package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ObservacionesDto
{
    /**
     * Indica si el profesor está conciliando horarios
     */
    private Boolean conciliacion ;

    /**
     * Indica si el profesor no quiere hacer clase la primera hora
     */
    private Boolean sinClasePrimeraHora ;

    /**
     * Indica otras observaciones del profesor
     */
    private String otrasObservaciones ;

    /**
     * Indica los tramos horarios que el profesor no quiere hacer
     */
    private List<DiaTramoTipoHorarioDto> tramosHorarios ;

    /**
     * Constructor por defecto
     */
    public ObservacionesDto()
    {
        this.conciliacion        = false ;
        this.sinClasePrimeraHora = false ;
        this.otrasObservaciones  = "" ;
        this.tramosHorarios      = new ArrayList<>() ;
    }
}
