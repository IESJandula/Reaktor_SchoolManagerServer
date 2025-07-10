package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneradorSesionAsignadaCursoEtapaGrupoDto
{
    /**
     * Nombre de la asignatura.
     */
    private String asignatura ;

    /**
     * Nombre del profesor.
     */
    private String nombreProfesor ;

    /**
     * Apellidos del profesor.
     */
    private String apellidosProfesor ;

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
}
