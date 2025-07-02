package es.iesjandula.reaktor.school_manager_server.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.repositories.IDiaTramoTipoHorarioRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiaTramoTipoHorarioService
{
    @Autowired
    private IDiaTramoTipoHorarioRepository diaTramoTipoHorarioRepository ;

    /**
     * Método que obtiene un día y tramo de tipo horario
     * @param dia - Día
     * @param tramo - Tramo
     * @param horarioMatutino - Horario matutino
     * @return - Día y tramo de tipo horario
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public DiaTramoTipoHorario obtenerDiaTramoTipoHorario(int dia, int tramo, Boolean horarioMatutino) throws SchoolManagerServerException
    {
        // Buscamos DiasTramosHorarios por IdDiasTramosTipoHorario
        Optional<DiaTramoTipoHorario> diasTramosTipoHorarioOptional = this.diaTramoTipoHorarioRepository.findByDiaAndTramoAndHorarioMatutino(dia, tramo, horarioMatutino) ;

        // Si no existe, lanzamos una excepción
        if (!diasTramosTipoHorarioOptional.isPresent())
        {
            String mensajeError = "El día " + dia + " y el tramo " + tramo + " no existe" ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.DIA_TRAMO_NO_ENCONTRADO, mensajeError) ;
        }   

        return diasTramosTipoHorarioOptional.get() ;
    }
}
