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

    /**
     * Horas necesarias por las asignaturas asignadas (como receptor) al departamento.
     */
    private long horasNecesarias;

    /**
     * Horas disponibles que aporta la plantilla actual del departamento.
     */
    private long horasTotales;

    /**
     * Desfase = horasTotales - horasNecesarias.
     * Positivo = sobran horas, 0 = cerrado, negativo = faltan horas.
     */
    private long desfase;

    private String resultado;

    /**
     * Propuesta automática de profesores en plantilla para cubrir las horas necesarias:
     * techo(horasNecesarias / horas lectivas por profesor). Es orientativa y editable por el usuario.
     */
    private int plantillaPropuesta;

    public AsignaturaConDepartamentoYResultadoDto(String nombre, int plantilla, long horasNecesarias, long horasTotales, long desfase)
    {
        this.nombre = nombre;
        this.plantilla = plantilla;
        this.horasNecesarias = horasNecesarias;
        this.horasTotales = horasTotales;
        this.desfase = desfase;
    }
}
