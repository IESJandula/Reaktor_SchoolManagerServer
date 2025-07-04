package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TramoDto
{
    /**
     * Tramo de la jornada.
     * <p>Este campo se mapea como la columna "tramo" de la tabla "DiaTramoTipoHorario".</p>
     */
    private Integer tramo;

    /**
     * Descripción del tramo.
     * <p>Este campo se mapea como la columna "tramoDesc" de la tabla "DiaTramoTipoHorario".</p>
     */
    private String tramoDesc;
} 