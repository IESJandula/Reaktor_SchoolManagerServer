package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SesionBaseDto
    {
    /** Número de sesión */
    private int numeroSesion;

    /** Día de la semana */
    private String diaDesc;

    /** Tramo horario */
    private String tramoDesc;
}
