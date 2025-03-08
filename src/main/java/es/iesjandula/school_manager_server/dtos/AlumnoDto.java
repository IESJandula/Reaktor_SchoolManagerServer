package es.iesjandula.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase DTO - AlumnoDto
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase actúa como un Objeto de Transferencia de Datos (DTO) para representar información básica de un alumno.
 * <p>Se utiliza para simplificar el transporte de datos entre capas o servicios en el sistema.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>Incluye los atributos básicos de un alumno: nombre y apellidos.</li>
 * <li>Se apoya en las anotaciones de Lombok para generar automáticamente métodos como getters, setters, 
 * constructores, y otros.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlumnoDto 
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
}
