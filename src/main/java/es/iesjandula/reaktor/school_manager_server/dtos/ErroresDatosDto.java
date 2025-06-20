package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List ;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;

@Data
public class ErroresDatosDto
{

    /** Título del error de datos */
    private String titulo ;

    /** Tipo de error de datos */
    private String tipo ;

    /** Valores implicados en el error de datos */
    private List<String> valoresImplicados ;

    /**
     * Constructor
     * 
     * @param titulo - Título del error de datos
     */
    public ErroresDatosDto(String titulo)
    {
        this.titulo            = titulo ;
        
        this.tipo              = Constants.ERROR_DATOS_TIPO_WARNING ;
        this.valoresImplicados = new ArrayList<String>() ;
    }

    /**
     * Método que agrega un valor implicado al error de datos
     * @param valor - Valor implicado
     */
    public void agregarValorImplicado(String valor)
    {
        this.tipo = Constants.ERROR_DATOS_TIPO_ERROR ;
        this.valoresImplicados.add(valor) ;
    }
}
