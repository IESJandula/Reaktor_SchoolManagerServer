package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorReduccionesDto
{
    private String nombre;

    private String apellidos;

    private String nombreReduccion;

    private Integer horas;

    private String email;
}
