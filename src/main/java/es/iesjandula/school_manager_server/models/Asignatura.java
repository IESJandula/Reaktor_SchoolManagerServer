package es.iesjandula.school_manager_server.models;

import es.iesjandula.school_manager_server.models.ids.IdAsignatura;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - Asignatura
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad "Asignatura" en la base de datos.
 * <p>Utiliza las anotaciones de JPA para definir cómo se mapea a la tabla "Asignatura" en la base de datos.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>La clase tiene un identificador compuesto (IdAsignatura) que se define mediante la anotación {@link EmbeddedId}.</li>
 * <li>Se establece una relación con la entidad "Departamento" tanto para el departamento propietario como receptor de la asignatura.</li>
 * <li>La clase también se asocia con un "Bloque" que representa el bloque de la asignatura.</li>
 * <li>Además, existe una relación de uno a muchos con la entidad "Matricula", representando las matrículas asociadas a la asignatura.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Asignatura")
public class Asignatura 
{
    /**
     * Identificador compuesto de la asignatura.
     * <p>Este campo es una clave primaria compuesta que se encuentra en la clase {@link IdAsignatura}.</p>
     */
    @EmbeddedId
    private IdAsignatura idAsignatura;

    /**
     * Numero de horas a impartir estipuladas de la asignatura.
     */
    @Column
    private int horas;
    
    /**
     * Departamento propietario de la asignatura.
     * <p>La asignatura tiene una relación de muchos a uno con el "Departamento" propietario. Se mapea con la columna
     * "departamento_propietario" en la tabla "Asignatura".</p>
     */
    @ManyToOne
    @JoinColumn(name="departamento_propietario", referencedColumnName = "nombre")
    private Departamento departamentoPropietario;
    
    /**
     * Departamento receptor de la asignatura.
     * <p>La asignatura también tiene una relación de muchos a uno con el "Departamento" receptor. Se mapea con la columna
     * "departamento_receptor" en la tabla "Asignatura".</p>
     */
    @ManyToOne
    @JoinColumn(name="departamento_receptor", referencedColumnName = "nombre")
    private Departamento departamentoReceptor;
    
    /**
     * Bloque al que pertenece la asignatura.
     * <p>Se establece una relación de muchos a uno con la entidad "Bloque" mediante la columna "bloque_id" en la tabla "Asignatura".
     * Esta relación indica a qué bloque de materias pertenece la asignatura.</p>
     */
    @ManyToOne
    @JoinColumn(name="bloque_id", referencedColumnName="id")
    private Bloque bloqueId;
    
    /**
     * Lista de matrículas asociadas a la asignatura.
     * <p>Existe una relación de uno a muchos con la entidad "Matricula", donde la asignatura puede estar asociada a muchas matrículas.
     * Esta relación se mapea mediante el atributo "asignatura" en la clase "Matricula".</p>
     */
//    @OneToMany(mappedBy = "asignatura")
//    private List<Matricula> matriculas;
}
