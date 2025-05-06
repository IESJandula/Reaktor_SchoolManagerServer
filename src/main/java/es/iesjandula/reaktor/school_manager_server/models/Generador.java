package es.iesjandula.reaktor.school_manager_server.models;

import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
}