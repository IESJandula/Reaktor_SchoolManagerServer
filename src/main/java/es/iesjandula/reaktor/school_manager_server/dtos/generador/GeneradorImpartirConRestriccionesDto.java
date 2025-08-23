package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.Data;

@Data
public class GeneradorImpartirConRestriccionesDto
{
    /** Asignatura */
    private Asignatura asignatura ;
    
    /** Profesor */
    private Profesor profesor ;
    
    /** Curso etapa y grupo */
    private CursoEtapaGrupo cursoEtapaGrupo ;

    /** Dia y tramo de tipo horario */
    private DiaTramoTipoHorario diaTramoTipoHorario ;

    /**
     * Constructor de la clase GeneradorImpartirConRestriccionesDto
     * @param asignatura Asignatura
     * @param profesor Profesor
     * @param cursoEtapaGrupo Curso etapa y grupo
     * @param diaTramoTipoHorario Dia y tramo de tipo horario
     */
    public GeneradorImpartirConRestriccionesDto(Asignatura asignatura, Profesor profesor, CursoEtapaGrupo cursoEtapaGrupo, DiaTramoTipoHorario diaTramoTipoHorario)
    {
        this.asignatura          = asignatura ;
        this.profesor            = profesor ;
        this.cursoEtapaGrupo     = cursoEtapaGrupo ;
        this.diaTramoTipoHorario = diaTramoTipoHorario ;
    }
    
    /**
     * Método que devuelve si es horario matutino
     * @return true si es horario matutino, false en caso contrario
     */
    public boolean isHorarioMatutino()
    {
        return this.cursoEtapaGrupo.getHorarioMatutino() ;
    }

    /**
     * Método que devuelve si la asignatura es optativa
     * @return true si la asignatura es optativa, false en caso contrario
     */
    public boolean isOptativa()
    {
        return this.cursoEtapaGrupo.getIdCursoEtapaGrupo().getGrupo().equals(Constants.GRUPO_OPTATIVAS) ;
    }

    /**
     * Método que devuelve el curso etapa grupo
     * @return Curso etapa grupo
     */
    public int getCurso()
    {
        return this.cursoEtapaGrupo.getIdCursoEtapaGrupo().getCurso() ;
    }

    /**
     * Método que devuelve la etapa
     * @return Etapa
     */
    public String getEtapa()
    {
        return this.cursoEtapaGrupo.getIdCursoEtapaGrupo().getEtapa() ;
    }
}
