package es.iesjandula.reaktor.school_manager_server.dtos.generador;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneradorRestriccionBaseDto
{
    /** Número de la restricción */
    private int numeroRestriccion;

    /** Día de la semana */
    private String diaDesc;

    /** Tramo horario */
    private String tramoDesc;
}
