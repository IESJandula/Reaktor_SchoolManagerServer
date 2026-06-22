package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CargaHorasAsignaturasResultDto
{
    private int asignaturasActualizadas;

    private List<AsignaturaHorasDto> horasAsignadas = new ArrayList<>();

    private List<String> asignaturasNoEncontradas = new ArrayList<>();
}
