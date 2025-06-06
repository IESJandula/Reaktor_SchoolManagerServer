package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImpartirAsignaturaDto
{
    private String nombre;

    private Integer horas;

    private Integer curso;

    private String etapa;

}
