package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdObservacionesAdicionales;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ObservacionesAdicionales")
@IdClass(IdObservacionesAdicionales.class)
public class ObservacionesAdicionales
{
    /** Curso académico del profesor. Parte de la clave primaria compuesta y de la FK hacia Profesor. */
    @Id
    @Column(name = "profesor_curso_academico", length = 9)
    private String profesorCursoAcademico;

    @Id
    @Column(name = "profesor_email")
    private String profesorEmail;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "profesor_curso_academico", referencedColumnName = "cursoAcademico", insertable = false, updatable = false),
        @JoinColumn(name = "profesor_email", referencedColumnName = "email", insertable = false, updatable = false)
    })
    private Profesor profesor;

    @Column
    private Boolean conciliacion;

    @Column
    private Boolean sinClasePrimeraHora;

    @Column(length = 1000)
    private String otrasObservaciones;
}
