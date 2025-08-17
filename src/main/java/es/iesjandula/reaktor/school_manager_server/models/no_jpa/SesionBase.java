package es.iesjandula.reaktor.school_manager_server.models.no_jpa;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHoraria;

@Slf4j
@Data
public abstract class SesionBase
{   
    /** Profesor que imparte la asignatura impartida en la sesion */
    private final Profesor profesor ;
    
    /** Tipo de horario matutino o vespertino */
    private final boolean tipoHorarioMatutino ;

    /** Indica si la sesión es de ESO o BACH */
    private final boolean esEsoBachillerato ;

    /** Propuesta de dia y/o tramo a forzar */
    private RestriccionHoraria restriccionHoraria ;

    /**
     * Constructor que inicializa la lista de restricciones horarias
     * 
     * @param profesor profesor
     * @param tipoHorario tipo de horario
     * @param esEsoBachillerato indica si la sesión es de ESO o BACH (si es false, es FP)
     * @param restriccionHoraria restricción horaria
     */
    public SesionBase(Profesor profesor, boolean tipoHorarioMatutino, boolean esEsoBachillerato, RestriccionHoraria restriccionHoraria)
    {      
        this.profesor            = profesor ;
        this.tipoHorarioMatutino = tipoHorarioMatutino ;
        this.esEsoBachillerato   = esEsoBachillerato ;
        this.restriccionHoraria  = restriccionHoraria ;
    }

    /**
     * @return true si la sesión es de asignatura, false en caso contrario
     */
    public boolean isTipoHorarioMatutino()
    {
        return this.tipoHorarioMatutino ;
    }

    /**
     * @return true si la sesión es de ESO o BACH, false en caso contrario
     */
    public boolean isEsoBachillerato()
    {
        return this.esEsoBachillerato ;
    }

    /**
     * @return el curso etapa grupo string
     */
    public abstract String getCursoEtapaGrupoString() ;
}

