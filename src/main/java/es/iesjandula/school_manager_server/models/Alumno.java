package es.iesjandula.school_manager_server.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - Alumno
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad Alumno en la base de datos. 
 * <p>Utiliza las anotaciones de JPA para definir cómo se mapea a la tabla "Alumno" en la base de datos.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>La entidad tiene tres atributos: id, nombre y apellidos, que corresponden a las columnas de la tabla.</li>
 * <li>Se utiliza la estrategia de generación de valores automática para el campo "id" con {@link GenerationType.IDENTITY}.</li>
 * <li>Las columnas para el nombre y los apellidos tienen restricciones de longitud definidas.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Alumno")
public class Alumno 
{
    /**
     * Identificador único del alumno.
     * <p>Es la clave primaria de la entidad "Alumno" y se genera automáticamente mediante la estrategia de identidad.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    /**
     * Nombre del alumno.
     * <p>Representa el primer nombre o los nombres del alumno. Tiene una longitud máxima de 50 caracteres.</p>
     */
    @Column(length = 50)
    private String nombre;
    
    /**
     * Apellidos del alumno.
     * <p>Representa los apellidos del alumno. Tiene una longitud máxima de 100 caracteres.</p>
     */
    @Column(length = 100)
    private String apellidos;
}
