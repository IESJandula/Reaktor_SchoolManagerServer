package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
