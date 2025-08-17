package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneradorAsignadaReduccionProfesorDto extends GeneradorAsignadaBaseReduccionDto
{
    /**
     * Curso de la asignatura.
     */
    private Integer curso ;

    /**
     * Etapa educativa de la asignatura.
     */
    private String etapa ;

    /**
     * Grupo de la asignatura.
     */
    private String grupo ;

    /**
     * Constructor de la clase GeneradorAsignadaReduccionProfesorDto.
     * @param diaDesc - Descripción del día.
     * @param tramoDesc - Descripción del tramo horario.
     * @param horarioMatutino - Indica si el grupo es matutino.
     * @param reduccion - Nombre de la reducción.
     * @param curso - Curso de la asignatura.
     * @param etapa - Etapa educativa de la asignatura.
     * @param grupo - Grupo de la asignatura.
     */
    public GeneradorAsignadaReduccionProfesorDto(String diaDesc,
                                                 String tramoDesc,
                                                 Boolean horarioMatutino,
                                                 String reduccion,
                                                 Integer curso,
                                                 String etapa,
                                                 String grupo)
    {
        super(diaDesc, tramoDesc, horarioMatutino, reduccion);
        
        this.curso = curso;
        this.etapa = etapa;
        this.grupo = grupo;
    }
}
