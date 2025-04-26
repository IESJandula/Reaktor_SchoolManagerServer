package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - CursoEtapaGrupo
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad "CursoEtapaGrupo" en la base de datos, que mapea la tabla "Curso_Etapa_Grupo".
 * <p>Utiliza la anotación {@link EmbeddedId} para definir una clave primaria compuesta a través de la clase {@link IdCursoEtapaGrupo}.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>La clase tiene un identificador compuesto, representado por el objeto {@link IdCursoEtapaGrupo}, que es la clave primaria.</li>
 * <li>La clase tiene una relación de uno a muchos con la entidad {@link DatosBrutoAlumnoMatriculaGrupo}, representando los registros
 * de alumnos matriculados en el curso, etapa y grupo correspondientes.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Curso_Etapa_Grupo")
public class CursoEtapaGrupo 
{
    /**
     * Identificador compuesto que representa el curso, etapa y grupo.
     * <p>Este campo se mapea como la clave primaria compuesta de la entidad {@link CursoEtapaGrupo} mediante la clase {@link IdCursoEtapaGrupo}.</p>
     */
    @EmbeddedId
    private IdCursoEtapaGrupo idCursoEtapaGrupo;

	/**
	 * Indica si el grupo es matutino.
	 */
	@Column(length = 1)
	private Boolean horarioMatutino;

    /**
     * Indica si el grupo es ESO o Bachillerato.
     */
    @Column(length = 1)
    private Boolean esoBachillerato;
    
    
    /**
     * @return El curso, etapa y grupo como una cadena de caracteres
     */
    public String getCursoEtapaGrupoString()
    {
        return this.idCursoEtapaGrupo.getCurso() + this.idCursoEtapaGrupo.getEtapa() + this.idCursoEtapaGrupo.getGrupo() ;
    }
}
