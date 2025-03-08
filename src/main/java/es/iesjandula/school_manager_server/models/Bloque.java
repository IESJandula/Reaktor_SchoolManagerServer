package es.iesjandula.school_manager_server.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - Bloque
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad "Bloque" en la base de datos.
 * <p>Utiliza las anotaciones de JPA para definir cómo se mapea a la tabla "Bloque" en la base de datos.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>La clase tiene un identificador único "id" que se mapea como la clave primaria.</li>
 * <li>La clase tiene una relación de uno a muchos con la entidad "Asignatura", representando el bloque de materias.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Bloque")
public class Bloque 
{
    /**
     * Identificador único del bloque.
     * <p>Este campo representa la clave primaria del bloque, mapeada a la columna "id" de la tabla "Bloque".
     * El identificador es de tipo {@link String} y tiene un máximo de 100 caracteres.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 100)
    private Long id;
    
    /**
     * Lista de asignaturas asociadas al bloque.
     * <p>La clase tiene una relación de uno a muchos con la entidad "Asignatura", indicando que un bloque puede
     * tener múltiples asignaturas asociadas. Esta relación se mapea mediante el atributo "bloqueId" en la clase {@link Asignatura}.</p>
     */
    @OneToMany(mappedBy = "bloqueId")
    private List<Asignatura> asignaturas;
}
