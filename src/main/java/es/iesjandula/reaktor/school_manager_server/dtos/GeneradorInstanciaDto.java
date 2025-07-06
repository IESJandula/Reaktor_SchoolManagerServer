package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.Data;

@Data
public class GeneradorInstanciaDto
{
    /** Identificador de la instancia del generador */
    private int idGeneradorInstancia ;

    /** Puntuación de la instancia del generador */
    private int puntuacion ;

    /** Mensaje de la instancia del generador */
    private String mensaje ;
}
