package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HorasYBloquesDto
{
    private Integer horas;

    private Long bloques;

    private boolean sinDocencia;

    private boolean desdoble;
}
