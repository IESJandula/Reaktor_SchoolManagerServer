package es.iesjandula.reaktor.school_manager_server.dtos;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.Data;

@Data
public class GeneradorInstanciaSolucionInfoGeneralDto
{
    /** Categoría de la solución */
    private String categoria ;

    /** Tipo de información de la solución */
    private String tipo ;

    /** Puntuación de la información de la solución */
    private int puntuacion ;

    /** Si el tipo de horario es matutino */
    private Boolean horarioMatutino ;

    /** Constructor de la clase */
    public GeneradorInstanciaSolucionInfoGeneralDto()
    {
        this(Constants.CATEGORIA_SOLUCION_GENERAL) ;
    }

    /** Constructor de la clase */
    public GeneradorInstanciaSolucionInfoGeneralDto(String categoria)
    {
        this.categoria = categoria ;
    }
}
