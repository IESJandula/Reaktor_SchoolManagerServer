package es.iesjandula.reaktor.school_manager_server.generator.models;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;

@Slf4j
@Data
public class Sesion
{
	/** Asignatura impartida en la sesion */
    private final Asignatura asignatura ;
    
    /** Profesor que imparte la asignatura impartida en la sesion */
    private final Profesor profesor ;
    
    /** Tipo de horario matutino o vespertino */
    private final boolean tipoHorarioMatutino ;

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
    public Sesion(Asignatura asignatura, Profesor profesor, boolean tipoHorarioMatutino, RestriccionHoraria restriccionHoraria)
    {      
        this.asignatura          = asignatura ;
        this.profesor            = profesor ;
        this.tipoHorarioMatutino = tipoHorarioMatutino ;
        this.restriccionHoraria  = restriccionHoraria ;
    }
    
    @Override
    public String toString()
    {
    	return this.asignatura.toString() ;
    }
}

