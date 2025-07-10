package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneradorSesionAsignadaProfesorDto
{
    /**
     * Nombre de la asignatura.
     */
    private String asignatura ;

    /**
     * Descripción del día.
     */
    private String diaDesc ;

    /**
     * Descripción del tramo horario.
     */
    private String tramoDesc ;

    /**
     * Indica si el grupo es matutino.
     */
    private Boolean horarioMatutino ;

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
}
