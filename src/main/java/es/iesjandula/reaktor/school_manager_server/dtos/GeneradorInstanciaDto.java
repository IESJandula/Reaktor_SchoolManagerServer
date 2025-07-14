package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GeneradorInstanciaDto
{
    /** Identificador de la instancia del generador */
    private int idGeneradorInstancia ;

    /** Puntuación de la instancia del generador */
    private double puntuacion ;

    /** Solución elegida */
    private Boolean solucionElegida ;

    /** Puntuaciones desglosadas de la instancia del generador (general y por profesor) */
    private List<GeneradorInstanciaSolucionInfoGeneralDto> puntuacionesDesglosadas ;

    /** Constructor de la clase */
    public GeneradorInstanciaDto()
    {
        this.puntuacionesDesglosadas = new ArrayList<>() ;
    }
}
