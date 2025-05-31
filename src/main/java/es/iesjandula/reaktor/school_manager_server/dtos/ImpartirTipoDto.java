package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImpartirTipoDto
{
    private String tipo;

    private String nombreAsignatura;

    private Integer horasAsignatura;

    private Integer cupoHorasAsignatura;

    private Integer curso;

    private String etapa ;

    private String grupo;

    private Boolean asignadoDireccion;
}
