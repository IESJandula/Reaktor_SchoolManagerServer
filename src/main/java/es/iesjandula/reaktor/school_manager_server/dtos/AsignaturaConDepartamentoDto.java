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

    /**
     * Horas necesarias por las asignaturas asignadas (como receptor) al departamento (SUM de horas).
     */
    private long horasNecesarias;

    /**
     * Horas disponibles que aporta la plantilla del departamento (plantilla * horas lectivas por profesor).
     */
    private long horasTotales;

    /**
     * Desfase = horasTotales - horasNecesarias.
     * Positivo = sobran horas, 0 = cuadra (cerrado), negativo = faltan horas.
     */
    private long desfase;


}
