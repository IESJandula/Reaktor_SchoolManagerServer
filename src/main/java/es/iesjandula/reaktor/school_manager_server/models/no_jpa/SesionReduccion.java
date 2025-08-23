package es.iesjandula.reaktor.school_manager_server.models.no_jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHoraria;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class SesionReduccion extends SesionBase
{
	/** Reduccion impartida en la sesion */
    private final Reduccion reduccion ;
    
    /**
     * Constructor que inicializa la lista de restricciones horarias
     * 
     * @param cursoEtapaGrupo curso etapa grupo
     * @param reduccion reduccion
     * @param profesor profesor
     * @param tipoHorario tipo de horario
     * @param restriccionHoraria restricción horaria
     */
    public SesionReduccion(CursoEtapaGrupo cursoEtapaGrupo,
        Reduccion reduccion, Profesor profesor, boolean tipoHorarioMatutino, RestriccionHoraria restriccionHoraria)
    {      
        super(cursoEtapaGrupo, profesor, tipoHorarioMatutino, cursoEtapaGrupo.getEsoBachillerato(), restriccionHoraria) ;

        this.reduccion = reduccion ;
    }
    
    @Override
    public String toString()
    {
    	return this.reduccion.toString() ;
    }
}

