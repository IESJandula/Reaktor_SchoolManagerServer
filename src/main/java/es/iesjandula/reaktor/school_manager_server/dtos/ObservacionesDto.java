package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ObservacionesDto
{
    private Boolean tieneObservaciones;

    private Boolean conciliacion;

    private Boolean trabajarPrimeraHora;

    private String otrasObservaciones;

    public ObservacionesDto()
    {
        this.tieneObservaciones = false;
    }
}
