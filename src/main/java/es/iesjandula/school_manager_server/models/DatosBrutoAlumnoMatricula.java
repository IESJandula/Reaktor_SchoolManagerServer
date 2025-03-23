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
 * Entidad - DatosBrutoAlumnoMatricula
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad "DatosBrutoAlumnoMatricula", que mapea la tabla "Datos_Bruto_Alumno_Matricula".
 * <p>Utiliza la anotación {@link Id} para definir una clave primaria autoincremental para cada registro.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>La clase contiene información básica de los alumnos, como su nombre, apellidos y la asignatura que cursan.</li>
 * <li>Además, la clase tiene una relación de muchos a uno con la entidad {@link CursoEtapa}, lo que significa que múltiples registros
 * de alumnos pueden estar asociados con una única combinación de curso y etapa.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Datos_Bruto_Alumno_Matricula")
public class DatosBrutoAlumnoMatricula 
{
    /**
     * Identificador único para el registro de la matrícula del alumno.
     * <p>Este campo se mapea como la clave primaria de la entidad {@link DatosBrutoAlumnoMatricula}.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    /**
     * Nombre del alumno.
     * <p>Este campo almacena el nombre del alumno en la matrícula.</p>
     */
    @Column(length = 50)
    private String nombre;
    
    /**
     * Apellidos del alumno.
     * <p>Este campo almacena los apellidos del alumno en la matrícula.</p>
     */
    @Column(length = 100)
    private String apellidos;
    
    /**
     * Asignatura en la que el alumno está matriculado.
     * <p>Este campo almacena el nombre de la asignatura en la que el alumno está matriculado.</p>
     */
    @Column(length = 100)
    private String asignatura;

    /**
     * Variable para determinar si el alumno esta asignado
     */
    @Column(nullable = false,columnDefinition = "BOOLEAN DEFAULT false")
    private boolean asignado;
    
    @Column(length = 7)
    private String estadoMatricula;

    /**
     * Relación con la entidad {@link CursoEtapa}.
     * <p>Este campo establece la relación de muchos a uno con la entidad {@link CursoEtapa}, indicando el curso y la etapa
     * en los que el alumno está matriculado.</p>
     */
    @ManyToOne
    private CursoEtapa cursoEtapa;
}
