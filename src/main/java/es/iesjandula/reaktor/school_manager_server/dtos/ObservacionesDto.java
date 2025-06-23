package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ObservacionesDto
{
    private Boolean tieneObservaciones;

    private Boolean conciliacion;

    private Boolean sinClasePrimeraHora;

    private String otrasObservaciones;

    public ObservacionesDto()
    {
        this.tieneObservaciones = false;
    }
}
