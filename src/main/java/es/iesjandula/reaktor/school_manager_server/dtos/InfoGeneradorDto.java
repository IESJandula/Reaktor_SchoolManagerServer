package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.Date;
import java.util.List;
import java.util.Map;

import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.Data;

@Data
public class InfoGeneradorDto
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
     * Mapa de puntuaciones e información de las soluciones.
     * <p>Representa el mapa de puntuaciones e información de las soluciones.</p>
     */
    private Map<Integer, List<String>> infoPuntuaciones ;
}
