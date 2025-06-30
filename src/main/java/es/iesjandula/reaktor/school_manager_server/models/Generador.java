package es.iesjandula.reaktor.school_manager_server.models;

import java.util.Date;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Column;

@Data
@Entity
@Table(name = "Generador")
public class Generador 
{
    /**
     * Identificador único del generador.
     * <p>Es la clave primaria de la entidad "Generador" y se genera automáticamente mediante la estrategia de identidad.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id ;

    /**
     * Puntuación del generador.
     * <p>Representa la puntuación del generador.</p>
     */
    @Column(name = "puntuacion")
    private int puntuacion ;

    /**
     * Fecha de inicio de la generación.
     * <p>Representa la fecha de inicio de la generación.</p>
     */
    @Column(name = "fechaInicio")
    private Date fechaInicio ;

    /**
     * Fecha de fin de la generación.
     * <p>Representa la fecha de fin de la generación.</p>
     */
    @Column(name = "fechaFin")
    private Date fechaFin ;

    /**
     * Estado del generador.
     * <p>Representa el estado del generador.</p>
     */
    @Column(name = "estado")
    private String estado ;

    /**
     * Mensaje de información.
     * <p>Representa el mensaje de información de la generación.</p>
     */
    @Column(name = "mensajeInformacion")
    private String mensajeInformacion ;

    /**
     * Constructor por defecto.
     * <p>Inicializa la fecha de inicio y el estado del generador a en curso.</p>
     */
    public Generador()
    {
        this.fechaInicio = new Date() ;
        this.estado      = Constants.ESTADO_EN_CURSO ;
    }

    /**
     * Método para establecer el mensaje de error.
     * @param estado - El estado de la generación.
     * @param mensajeInformacion - El mensaje de información de la generación.
     */
    public void pararGenerador(String estado, String mensajeInformacion)
    {
        this.estado             = estado ;
        this.mensajeInformacion = mensajeInformacion ;
        this.fechaFin           = new Date() ;
    }
}