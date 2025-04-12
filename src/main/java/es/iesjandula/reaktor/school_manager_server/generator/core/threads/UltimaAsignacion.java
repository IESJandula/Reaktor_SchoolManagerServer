package es.iesjandula.reaktor.school_manager_server.generator.core.threads;

import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UltimaAsignacion
{
    /** Indices de asignación de sesión */
    private IndicesAsignacionSesion indicesAsignacionSesion ;

    /** Asignación */
    private Asignacion asignacion ;
}
