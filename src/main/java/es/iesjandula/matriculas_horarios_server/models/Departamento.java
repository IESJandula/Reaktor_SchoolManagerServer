package es.iesjandula.matriculas_horarios_server.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Departamento")
public class Departamento
{
    /**
     * Nombre del Departamento.
     * Este atributo es la clave primaria del Departamento y se usa para identificarlo
     * de forma única dentro de la base de datos.
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
}
