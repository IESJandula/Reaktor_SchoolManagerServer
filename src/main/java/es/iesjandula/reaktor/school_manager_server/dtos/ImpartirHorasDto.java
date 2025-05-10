package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImpartirHorasDto
{
    private String nombre;

    private Integer horas;

    private Integer cupoHoras;

    private Integer curso;

    private String etapa ;

    private Character grupo;

    private Boolean asignadoDireccion;
}
