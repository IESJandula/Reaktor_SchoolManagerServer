package es.iesjandula.reaktor.school_manager_server.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "GeneradorInstancia")
public class GeneradorInstancia
{
    /** 
     * Id de la instancia del generador.
     * <p>Representa el id de la instancia del generador.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id ;

    /**
     * Puntuación de la instancia del generador.
     * <p>Representa la puntuación de la instancia del generador.</p>
     */
    @Column(name = "puntuacion")
    private double puntuacion ;

    /**
     * Mensaje de información de la instancia del generador.
     * <p>Representa el mensaje de información de la instancia del generador.</p>
     */
    @Column(name = "mensajeInformacion")
    private String mensajeInformacion ;

    /**
     * Estado de la instancia del generador.
     * <p>Representa el estado de la instancia del generador.</p>
     */
    @Column(name = "estado")
    private String estado ;


    /**
     * Solución elegida por el usuario.
     * <p>Representa la solución elegida por el usuario.</p>
     */
    @Column(name = "solucionElegida")
    private Boolean solucionElegida ;

    /**
     * Generador al que pertenece la instancia.
     * Relación de muchos a uno con la entidad {@link Generador}.
     */
    @ManyToOne
    @JoinColumn(name = "idGenerador")
    @JsonBackReference
    private Generador generador ;

    public GeneradorInstancia()
    {
        this.puntuacion         = 0.0 ;
        this.mensajeInformacion = "" ;
        this.estado             = Constants.ESTADO_GENERADOR_EN_CURSO ;
        this.solucionElegida    = false ;
    }
    
    /**
     * Método para establecer el mensaje de error.
     * @param estado - El estado de la generación.
     * @param mensajeInformacion - El mensaje de información de la generación.
     */
    public void pararGeneradorInstancia(String estado, double puntuacion, String mensajeInformacion)
    {
        this.estado             = estado ;
        this.puntuacion         = puntuacion ;
        this.mensajeInformacion = mensajeInformacion ;
    }
}
