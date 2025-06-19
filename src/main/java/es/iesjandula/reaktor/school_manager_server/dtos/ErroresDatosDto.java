package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List ;

@Data
public class ErroresDatosDto
{
    /** Tipo de error de datos */
    private String tipo ;

    /** Valores implicados en el error de datos */
    private List<String> valoresImplicados ;

    /**
     * Constructor
     * 
     * @param tipo - Tipo de error de datos
     */
    public ErroresDatosDto(String tipo)
    {
        this.tipo              = tipo ;
        this.valoresImplicados = new ArrayList<String>() ;
    }
}
