package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad - CursoEtapa
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa la entidad "CursoEtapa" en la base de datos, que mapea la tabla "Curso_Etapa".
 * <p>Utiliza la anotación {@link EmbeddedId} para definir una clave primaria compuesta a través de la clase {@link IdCursoEtapa}.</p>
 * -----------------------------------------------------------------------------------------------------------------
 * <ul>
 * <li>La clase tiene un identificador compuesto, representado por el objeto {@link IdCursoEtapa}, que es la clave primaria.</li>
 * <li>La clase tiene una relación de uno a muchos con la entidad {@link DatosBrutoAlumnoMatricula}, representando los registros
 * de alumnos matriculados en el curso y etapa correspondientes.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Curso_Etapa")
public class CursoEtapa 
{
    /**
     * Identificador compuesto que representa el curso y la etapa.
     * <p>Este campo se mapea como la clave primaria compuesta de la entidad {@link CursoEtapa} mediante la clase {@link IdCursoEtapa}.</p>
     */
    @EmbeddedId
    private IdCursoEtapa idCursoEtapa;

    /**
     * Indica si el curso y etapa pertenece a ESO o Bachillerato.
     * <p>Este campo se mapea como la columna "esoBachillerato" de la tabla "Curso_Etapa".</p>
     */
    @Column(name = "esoBachillerato")
    private boolean esoBachillerato ;

    /**
     * Método que devuelve el curso y etapa como una cadena de texto.
     * @return - Cadena de texto con el curso y etapa.
     */
    public String getCursoEtapaString()
    {
        return this.idCursoEtapa.getCurso() + " " + this.idCursoEtapa.getEtapa() ;
    }
}
