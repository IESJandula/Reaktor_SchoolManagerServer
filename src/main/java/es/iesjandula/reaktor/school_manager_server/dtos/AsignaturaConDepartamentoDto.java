package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaConDepartamentoDto
{

    private String nombre;

    private int plantilla;

    private int horasNecesarias;

    private long horasTotales;

    private long desfase;


}
