package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlumnoDto3
{
    /**
     * El nombre del alumno.
     * <p>Representa el primer nombre o los nombres del alumno.</p>
     */
    private String nombre;

    /**
     * Los apellidos del alumno.
     * <p>Representa los apellidos del alumno, usualmente en formato completo.</p>
     */
    private String apellidos;

    /**
     * Variable para ver si el alumno esta asignado.
     */
    private Boolean asignado;
}
