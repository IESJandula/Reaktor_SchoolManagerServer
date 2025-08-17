package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeneradorInstanciaSolucionInfoProfesorDto extends GeneradorInstanciaSolucionInfoGeneralDto
{
    /** Email del profesor */
    private String emailProfesor ;

    /** Constructor de la clase */
    public GeneradorInstanciaSolucionInfoProfesorDto()
    {
        super(Constants.CATEGORIA_SOLUCION_PROFESOR) ;
    }
}

