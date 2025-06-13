package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaConDepartamentoYResultadoDto
{
    private String nombre;

    private int plantilla;

    private int horasNecesarias;

    private long horasTotales;

    private long desfase;

    private String resultado;

    public AsignaturaConDepartamentoYResultadoDto(String nombre, int plantilla, int horasNecesarias, long horasTotales, long desfase)
    {
        this.nombre = nombre;
        this.plantilla = plantilla;
        this.horasNecesarias = horasNecesarias;
        this.horasTotales = horasTotales;
        this.desfase = desfase;
    }
}
