package es.iesjandula.reaktor.school_manager_server.models.no_jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class SesionAsignatura extends SesionBase
{
	/** Asignatura impartida en la sesion */
    private final Asignatura asignatura ;
    

    /**
     * Constructor que inicializa la lista de restricciones horarias
     * 
     * @param asignatura asignatura
     * @param profesor profesor
     * @param tipoHorario tipo de horario
     * @param restriccionHoraria restricción horaria
     */
    public SesionAsignatura(Asignatura asignatura, Profesor profesor, boolean tipoHorarioMatutino, RestriccionHoraria restriccionHoraria)
    {      
        super(profesor, tipoHorarioMatutino, asignatura.getIdAsignatura().getCursoEtapaGrupo().getEsoBachillerato(), restriccionHoraria) ;

        this.asignatura = asignatura ;
    }
    
    @Override
    public String toString()
    {
    	return this.asignatura.toString() ;
    }

    @Override
    public String getCursoEtapaGrupoString()
    {
        return this.asignatura.getIdAsignatura().getCursoEtapaGrupo().getCursoEtapaGrupoString() ;
    }
}

