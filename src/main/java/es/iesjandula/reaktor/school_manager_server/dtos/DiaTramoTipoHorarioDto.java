package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiaTramoTipoHorarioDto
{
    private String diaDesc;

    private String tramoDesc;

    private Boolean horarioMatutino;
}
