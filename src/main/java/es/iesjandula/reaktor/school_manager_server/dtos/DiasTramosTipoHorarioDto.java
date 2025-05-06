package es.iesjandula.reaktor.school_manager_server.dtos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiasTramosTipoHorarioDto
{
    private String dia;

    private Integer tramo;

    private String tipoHorario;
}
