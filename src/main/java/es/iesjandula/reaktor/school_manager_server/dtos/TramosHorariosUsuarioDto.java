package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TramosHorariosUsuarioDto
{
    private Boolean tieneObservaciones;

    private List<DiaTramoTipoHorarioDto> tramosHorarios;

    public TramosHorariosUsuarioDto()
    {
        this.tieneObservaciones = false;
    }
}
