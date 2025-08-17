package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneradorAsignadaImpartirProfesorDto extends GeneradorAsignadaBaseImpartirDto
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
     * Constructor de la clase GeneradorAsignadaImpartirProfesorDto.
     * @param diaDesc - Descripción del día.
     * @param tramoDesc - Descripción del tramo horario.
     * @param horarioMatutino - Indica si el grupo es matutino.
     * @param asignatura - Nombre de la asignatura.
     * @param curso - Curso de la asignatura.
     * @param etapa - Etapa educativa de la asignatura.
     * @param grupo - Grupo de la asignatura.
     */
    public GeneradorAsignadaImpartirProfesorDto(String diaDesc,
                                                String tramoDesc,
                                                Boolean horarioMatutino,
                                                String asignatura,
                                                Integer curso,
                                                String etapa,
                                                String grupo)
    {
        super(diaDesc, tramoDesc, horarioMatutino, asignatura);
        
        this.curso = curso;
        this.etapa = etapa;
        this.grupo = grupo;
    }
}
