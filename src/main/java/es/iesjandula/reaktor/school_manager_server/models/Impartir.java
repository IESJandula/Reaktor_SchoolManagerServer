package es.iesjandula.reaktor.school_manager_server.models;

import java.util.HashSet;
import java.util.Set;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdImpartir;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - Impartir
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la relación entre un {@link Asignatura} y un {@link Profesor} en el contexto de la asignación
 * de profesores a asignaturas. Utiliza un identificador compuesto {@link IdImpartir} como clave primaria para esta relación.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Impartir")
public class Impartir 
{
    /**
     * Identificador compuesto para la relación entre {@link Asignatura} y {@link Profesor}.
     * El identificador se compone de la {@link Asignatura} y el {@link Profesor} que imparten la asignatura.
     */
    @EmbeddedId
    private IdImpartir idImpartir;
    
    /**
     * Asignatura que está siendo impartida por el profesor.
     * Relación de muchos a uno con la entidad {@link Asignatura}.
     */
    @MapsId(value = "asignatura")
    @ManyToOne
    private Asignatura asignatura;
    
    /**
     * Profesor que está asignado para impartir la asignatura.
     * Relación de muchos a uno con la entidad {@link Profesor}.
     */
    @MapsId(value = "profesor")
    @ManyToOne
    private Profesor profesor;

    @Column
    private Integer cupoHoras;

    @Column
    private Boolean asignadoDireccion;

    @OneToMany(mappedBy = "impartir", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GeneradorRestriccionesImpartir> generadorRestriccionesImpartir = new HashSet<>();

    /**
     * Método que devuelve si es horario matutino
     * @return true si es horario matutino, false en caso contrario
     */
    public boolean isHorarioMatutino()
    {
        return this.asignatura.getIdAsignatura().getCursoEtapaGrupo().getHorarioMatutino() ;
    }

    /**
     * Método que devuelve si la asignatura es optativa
     * @return true si la asignatura es optativa, false en caso contrario
     */
    public boolean isOptativa()
    {
        return this.asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getGrupo().equals(Constants.GRUPO_OPTATIVAS) ;
    }

    /**
     * Método que devuelve el curso etapa grupo
     * @return Curso etapa grupo
     */
    public CursoEtapaGrupo getCursoEtapaGrupo()
    {
        return this.asignatura.getIdAsignatura().getCursoEtapaGrupo() ;
    }

    /**
     * Método que devuelve el curso etapa grupo
     * @return Curso etapa grupo
     */
    public int getCurso()
    {
        return this.asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getCurso() ;
    }

    /**
     * Método que devuelve la etapa
     * @return Etapa
     */
    public String getEtapa()
    {
        return this.asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getEtapa() ;
    }

    /**
     * Método que devuelve el grupo
     * @return Grupo
     */
    public int getHorasTotalesAsignatura()
    {
        return this.asignatura.getHoras() ;
    }
}
