package es.iesjandula.reaktor.school_manager_server.models.no_jpa;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHorariaInit;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHorariaThread;

@Slf4j
@Data
public abstract class SesionBase
{   
    /** Curso etapa grupo */
    private final CursoEtapaGrupo cursoEtapaGrupo ;

    /** Profesor que imparte la asignatura impartida en la sesion */
    private final Profesor profesor ;
    
    /** Tipo de horario matutino o vespertino */
    private final boolean tipoHorarioMatutino ;

    /** Indica si la sesión es de ESO o BACH */
    private final boolean esoBachillerato ;

    /** Propuesta de restricciones horarias iniciales */
    private RestriccionHorariaInit restriccionHorariaInit ;

    /** Propuesta de restricciones horarias iteracion */
    private RestriccionHorariaThread restriccionHorariaThread ;

    /**
     * Constructor que inicializa la lista de restricciones horarias
     * 
     * @param cursoEtapaGrupo curso etapa grupo
     * @param profesor profesor
     * @param tipoHorario tipo de horario
     * @param esoBachillerato indica si la sesión es de ESO o BACH (si es false, es FP)
     * @param restriccionHorariaInit restricción horaria init
     */
    public SesionBase(CursoEtapaGrupo cursoEtapaGrupo, Profesor profesor, boolean tipoHorarioMatutino,
                      boolean esoBachillerato, RestriccionHorariaInit restriccionHorariaInit)
    {      
        this.cursoEtapaGrupo        = cursoEtapaGrupo ;
        this.profesor               = profesor ;
        this.tipoHorarioMatutino    = tipoHorarioMatutino ;
        this.esoBachillerato        = esoBachillerato ;
        this.restriccionHorariaInit = restriccionHorariaInit ;
    }

    /**
     * @return el curso etapa grupo string
     */
    public String getCursoEtapaGrupoString()
    {
        return this.cursoEtapaGrupo.getCursoEtapaGrupoString() ;
    }

    /**
     * Inicializa la restricción horaria iteracion
     */
    public void inicializarRestriccionHorariaThread()
    {
        this.restriccionHorariaThread = new RestriccionHorariaThread(this.restriccionHorariaInit) ;
    }
}

