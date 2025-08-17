package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneradorAsignadaImpartirCursoEtapaGrupoDto extends GeneradorAsignadaBaseImpartirDto
{
    /**
     * Nombre del profesor.
     */
    private String nombreProfesor ;

    /**
     * Apellidos del profesor.
     */
    private String apellidosProfesor ;

    /**
     * Constructor de la clase GeneradorAsignadaCursoEtapaGrupoDto.
     * @param diaDesc - Descripción del día.
     * @param tramoDesc - Descripción del tramo horario.
     * @param horarioMatutino - Indica si el grupo es matutino.
     * @param asignatura - Nombre de la asignatura.
     * @param nombreProfesor - Nombre del profesor.
     * @param apellidosProfesor - Apellidos del profesor.
     */
    public GeneradorAsignadaImpartirCursoEtapaGrupoDto(String diaDesc,
                                                       String tramoDesc,
                                                       Boolean horarioMatutino,
                                                       String asignatura,
                                                       String nombreProfesor,
                                                       String apellidosProfesor)
    {
        super(diaDesc, tramoDesc, horarioMatutino, asignatura) ;
        
        this.nombreProfesor    = nombreProfesor ;
        this.apellidosProfesor = apellidosProfesor ;
    }
}
