package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class GeneradorInfoDto
{
    /**
     * Estado del generador.
     * <p>Representa el estado del generador.</p>
     */
    private String estado ;

    /**
     * Fecha de inicio de la generación.
     * <p>Representa la fecha de inicio de la generación.</p>
     */
    private Date fechaInicio ;

    /**
     * Fecha de fin de la generación.
     * <p>Representa la fecha de fin de la generación.</p>
     */
    private Date fechaFin ;

    /**
     * Mensaje de información.
     * <p>Representa el mensaje de información de la generación.</p>
     */
    private String mensajeInformacion ;

    /**
     * Lista de soluciones.
     * <p>Representa la lista de soluciones.</p>
     */
    private List<GeneradorInstanciaDto> soluciones ;
}
