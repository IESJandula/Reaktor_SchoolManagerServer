package es.iesjandula.reaktor.school_manager_server.generator.models.enums;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;

import lombok.extern.slf4j.Slf4j;

/**
 * Enumerado con los tipos de horario disponibles
 */
@Slf4j
public enum TipoHorario
{
    /** Horario de mañana */
    MATUTINO(Constants.HORARIO_MATUTINO),

    /** Horario de tarde */
    VESPERTINO(Constants.HORARIO_VESPERTINO) ;

    /** Tipo de horario */
    private String tipoHorario ;

    /** Constructor de la clase */
    private TipoHorario(String tipoHorario)
    {
        this.tipoHorario = tipoHorario ;
    }

    /**
     * Método para obtener el tipo de horario
     * @return Tipo de horario
     */
    public String getTipoHorario()
    {
        return this.tipoHorario ;
    }

    /** 
     * Método para obtener el tipo de horario 
     * @param tipoHorario Tipo de horario
     * @return Tipo de horario
     */
    public static TipoHorario getTipoHorario(String tipoHorario) throws SchoolManagerServerException
    {
        if (tipoHorario.equals(Constants.HORARIO_MATUTINO))
        {
            return TipoHorario.MATUTINO ;
        }
        else if (tipoHorario.equals(Constants.HORARIO_VESPERTINO))
        {
            return TipoHorario.VESPERTINO ;
        }
        else
        {
            String mensajeError = "El tipo de horario no es válido: " + tipoHorario ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.ERROR_TIPO_HORARIO_NO_VALIDO, mensajeError) ;
        }
    }


}
