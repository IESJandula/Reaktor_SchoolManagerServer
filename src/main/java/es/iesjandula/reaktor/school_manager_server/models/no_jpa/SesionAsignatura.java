package es.iesjandula.reaktor.school_manager_server.models.no_jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;

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
     * @param cursoEtapaGrupo curso etapa grupo
     * @param asignatura asignatura
     * @param profesor profesor
     * @param tipoHorario tipo de horario
     * @param restriccionHoraria restricción horaria
     */
    public SesionAsignatura(CursoEtapaGrupo cursoEtapaGrupo, Asignatura asignatura, Profesor profesor,
                            boolean tipoHorarioMatutino, RestriccionHoraria restriccionHoraria)
    {      
        super(cursoEtapaGrupo, profesor, tipoHorarioMatutino, cursoEtapaGrupo.getEsoBachillerato(), restriccionHoraria) ;

        this.asignatura = asignatura ;
    }
    
    @Override
    public String toString()
    {
    	return this.asignatura.toString() ;
    }
}

