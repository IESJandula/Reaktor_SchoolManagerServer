package es.iesjandula.reaktor.school_manager_server.generator.models;

import es.iesjandula.reaktor.school_manager_server.generator.models.enums.TipoHorario;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Sesion
{
	/** Asignatura impartida en la sesion */
    private final Asignatura asignatura ;
    
    /** Profesor que imparte la asignatura impartida en la sesion */
    private final Profesor profesor ;
    
    /** Tipo de horario */
    private final TipoHorario tipoHorario ;

    /** Propuesta de dia y/o tramo a forzar */
    private RestriccionHoraria restriccionHoraria ;

    /**
     * Constructor que inicializa la lista de restricciones horarias
     * 
     * @param asignatura asignatura
     * @param profesor profesor
     * @param tipoHorario tipo de horario
     * @param restriccionHoraria restricción horaria
     */
    public Sesion(Asignatura asignatura, Profesor profesor, TipoHorario tipoHorario, RestriccionHoraria restriccionHoraria)
    {      
        this.asignatura         = asignatura ;
        this.profesor           = profesor ;
        this.tipoHorario        = tipoHorario ;
        this.restriccionHoraria = restriccionHoraria ;
    }
    
    @Override
    public String toString()
    {
    	return this.asignatura.toString() ;
    }
}

