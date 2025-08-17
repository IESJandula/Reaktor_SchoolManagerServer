package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeneradorAsignadaReduccionCursoEtapaGrupoDto extends GeneradorAsignadaBaseReduccionDto
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
     * @param reduccion - Nombre de la reducción.
     * @param nombreProfesor - Nombre del profesor.
     * @param apellidosProfesor - Apellidos del profesor.
     */
    public GeneradorAsignadaReduccionCursoEtapaGrupoDto(String diaDesc,
                                                        String tramoDesc,
                                                        Boolean horarioMatutino,
                                                        String reduccion,
                                                        String nombreProfesor,
                                                        String apellidosProfesor)
    {
        super(diaDesc, tramoDesc, horarioMatutino, reduccion) ;
        
        this.nombreProfesor    = nombreProfesor ;
        this.apellidosProfesor = apellidosProfesor ;
    }
}
