package es.iesjandula.reaktor.school_manager_server.models;

import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdDepartamento;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - Departamento
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa a un Departamento en la base de datos.
 * Un Departamento está asociado a varias asignaturas y profesores, los cuales se pueden
 * obtener a través de las relaciones mapeadas con otras entidades.
 * -----------------------------------------------------------------------------------------------------------------
 * La clave primaria es compuesta {@code (cursoAcademico, nombre)} mediante {@link IdClass}, de modo que el
 * catálogo de departamentos queda asociado/filtrado por curso académico. Los departamentos globales (profesores,
 * asignaturas, reducciones) usan {@link Constants#CURSO_ACADEMICO_GLOBAL} como {@code cursoAcademico}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Departamento")
@IdClass(IdDepartamento.class)
public class Departamento
{
    /**
     * Curso académico al que pertenece el departamento. Forma parte de la clave primaria.
     * {@link Constants#CURSO_ACADEMICO_GLOBAL} para los departamentos globales.
     */
    @Id
    @Column(length = 9)
    private String cursoAcademico;

    /**
     * Nombre del Departamento.
     * Junto con {@code cursoAcademico} forma la clave primaria del Departamento.
     */
    @Id
    @Column(length = 100)
    private String nombre;

    /**
     * Plantilla del Departamento.
     * Cantidad de profesores del departamento.
     */
    @Column
    private int plantilla;
    
    /**
     * Lista de asignaturas que son propiedad del Departamento.
     * Relación de uno a muchos con la entidad {@link Asignatura}.
     * Las asignaturas en esta lista están asociadas como "propietarias" de este Departamento.
     */
    @OneToMany(mappedBy = "departamentoPropietario")
    private List<Asignatura> asignaturasPropietarias;
    
    /**
     * Lista de asignaturas que son receptoras de este Departamento.
     * Relación de uno a muchos con la entidad {@link Asignatura}.
     * Las asignaturas en esta lista están asociadas como "receptoras" de este Departamento.
     */
    @OneToMany(mappedBy = "departamentoReceptor")
    private List<Asignatura> asignaturasReceptores;
    
    /**
     * Lista de profesores que están asignados a este Departamento.
     * Relación de uno a muchos con la entidad {@link Profesor}.
     * Los profesores en esta lista son miembros del Departamento.
     */
    @OneToMany(mappedBy = "departamento")
    private List<Profesor> profesores;

    /**
     * Constructor de compatibilidad para departamentos globales (profesores, asignaturas, reducciones).
     *
     * @param nombre - Nombre del departamento.
     */
    public Departamento(String nombre)
    {
        this.cursoAcademico = Constants.CURSO_ACADEMICO_GLOBAL;
        this.nombre = nombre;
    }
}
