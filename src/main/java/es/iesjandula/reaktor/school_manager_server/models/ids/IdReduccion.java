package es.iesjandula.reaktor.school_manager_server.models.ids;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IdReduccion
{

    /**
     * Curso académico al que pertenece la reducción. Forma parte de la clave primaria para que cada curso académico
     * tenga su propio catálogo de reducciones aislado (tanto las sin docencia —tutorías plantilla y no tutorías—
     * como las con docencia por grupo). Se usa {@link Constants#CURSO_ACADEMICO_GLOBAL} como valor de compatibilidad
     * para registros globales heredados. Longitud acotada (9, p. ej. "2025/2026") para no exceder el límite de
     * clave de MySQL en las tablas que referencian la reducción.
     */
    @Column(length = 9)
    private String cursoAcademico;

    /**
     * Nombre de la reducción. Este es un identificador único de la reducción.
     */
    @Column(length = 100)
    private String nombre;

    /**
     * Número de horas que se reducen para el profesor. Este valor es obligatorio.
     */
    private Integer horas;

    /**
     * Constructor de compatibilidad: crea el id con {@link Constants#CURSO_ACADEMICO_GLOBAL}. Las rutas que crean o
     * buscan reducciones scoped por curso académico deben usar el constructor de 3 argumentos.
     *
     * @param nombre nombre de la reducción.
     * @param horas  horas de la reducción.
     */
    public IdReduccion(String nombre, Integer horas)
    {
        this(Constants.CURSO_ACADEMICO_GLOBAL, nombre, horas);
    }
}
