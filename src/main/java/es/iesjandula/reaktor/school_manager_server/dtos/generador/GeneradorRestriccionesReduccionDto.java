package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneradorRestriccionesReduccionDto
{
    private Reduccion reduccion ;
    
    private Profesor profesor ;

    private CursoEtapaGrupo cursoEtapaGrupo ;

    private DiaTramoTipoHorario diaTramoTipoHorario ;
}
