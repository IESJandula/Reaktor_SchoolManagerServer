package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiaDto
{
    /** 
     * Día de la semana.
     * <p>Este campo se mapea como la columna "dia" de la tabla "DiaTramoTipoHorario".</p>
     */
    private Integer dia ;

    /**
     * Descripción del día.
     * <p>Este campo se mapea como la columna "diaDesc" de la tabla "DiaTramoTipoHorario".</p>
     */
    private String diaDesc ;
} 