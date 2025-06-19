package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ValidadorDatosDto
{
    /** Lista de errores de datos */
    private List<ErroresDatosDto> erroresDatos ;

    /** Constructor por defecto */
    public ValidadorDatosDto()
    {
        this.erroresDatos = new ArrayList<ErroresDatosDto>() ;
    }
}
