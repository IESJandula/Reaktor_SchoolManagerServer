package es.iesjandula.school_manager_server.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - DatosBrutoAlumnoMatriculaGrupo
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad "DatosBrutoAlumnoMatriculaGrupo", que mapea la tabla "Datos_Bruto_Alumno_Matricula_Grupo".
 * <p>Utiliza la anotación {@link Id} para definir una clave primaria autoincremental para cada registro.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>La clase contiene información básica de los alumnos, como su nombre, apellidos y la asignatura que cursan.</li>
 * <li>Además, la clase tiene una relación de muchos a uno con la entidad {@link CursoEtapaGrupo}, lo que significa que múltiples registros
 * de alumnos pueden estar asociados con una única combinación de curso, etapa y grupo.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Datos_Bruto_Alumno_Matricula_Grupo")
public class DatosBrutoAlumnoMatriculaGrupo 
{
    /**
     * Identificador único para el registro de la matrícula del alumno en un grupo.
     * <p>Este campo se mapea como la clave primaria de la entidad {@link DatosBrutoAlumnoMatriculaGrupo}.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    /**
     * Nombre del alumno.
     * <p>Este campo almacena el nombre del alumno en la matrícula del grupo.</p>
     */
    @Column(length = 50, nullable = false)
    private String nombre;
    
    /**
     * Apellidos del alumno.
     * <p>Este campo almacena los apellidos del alumno en la matrícula del grupo.</p>
     */
    @Column(length = 100, nullable = false)
    private String apellidos;
    
    /**
     * Asignatura en la que el alumno está matriculado en el grupo.
     * <p>Este campo almacena el nombre de la asignatura que el alumno cursa en el grupo.</p>
     */
    @Column(length = 100, nullable = false)
    private String asignatura;

    /**
     * Relación con la entidad {@link CursoEtapaGrupo}.
     * <p>Este campo establece la relación de muchos a uno con la entidad {@link CursoEtapaGrupo}, indicando el curso, etapa y grupo
     * en los que el alumno está matriculado.</p>
     */
    @ManyToOne
    private CursoEtapaGrupo cursoEtapaGrupo;
}
