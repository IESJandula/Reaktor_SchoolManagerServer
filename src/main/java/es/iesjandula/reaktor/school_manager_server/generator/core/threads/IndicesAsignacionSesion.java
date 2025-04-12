package es.iesjandula.reaktor.school_manager_server.generator.core.threads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndicesAsignacionSesion
{
    /** Indice para el curso dia */
    private int indiceCursoDia ;

    /** Indice para el tramo horario */
    private int indiceTramoHorario ;
}
