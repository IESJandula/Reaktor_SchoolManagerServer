package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.Date;

import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.Data;

@Data
public class GeneradorDto
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
     * Puntuación de la generación.
     * <p>Representa la puntuación de la generación.</p>
     */
    private int puntuacion ;

    /**
     * Mensaje de información.
     * <p>Representa el mensaje de información de la generación.</p>
     */
    private String mensajeInformacion ;

    /**
     * Constructor que recibe un mensaje de información. Cuando se lanza es que el generador no está en curso
     * <p>Inicializa el DTO con el mensaje de información.</p>
     * @param mensajeInformacion - El mensaje de información.
     */
    public GeneradorDto(String mensajeInformacion)
    {
        this.estado             = Constants.ESTADO_DETENIDO ;
        this.mensajeInformacion = mensajeInformacion ;
    }

    /**
     * Constructor que recibe un generador.
     * <p>Inicializa el DTO con los datos del generador.</p>
     * @param generador - El generador.
     */
    public GeneradorDto(Generador generador)
    {
        this.estado             = generador.getEstado() ;
        this.fechaInicio        = generador.getFechaInicio() ;
        this.fechaFin           = generador.getFechaFin() ;
        this.puntuacion         = generador.getPuntuacion() ;
        this.mensajeInformacion = generador.getMensajeInformacion() ;
    }
}
