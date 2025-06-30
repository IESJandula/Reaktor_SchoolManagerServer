package es.iesjandula.reaktor.school_manager_server.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.repositories.IDiasTramosTipoHorarioRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiasTramosTipoHorarioService
{
    @Autowired
    private IDiasTramosTipoHorarioRepository diasTramosTipoHorarioRepository ;

    /**
     * Método que obtiene un día y tramo de tipo horario
     * @param dia - Día
     * @param tramo - Tramo
     * @param horarioMatutino - Horario matutino
     * @return - Día y tramo de tipo horario
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public DiasTramosTipoHorario obtenerDiasTramosHorario(int dia, int tramo, boolean horarioMatutino) throws SchoolManagerServerException
    {
        // Creamos una instancia de IdDiasTramosTipoHorario
        IdDiasTramosTipoHorario idDiasTramosTipoHorario = new IdDiasTramosTipoHorario(dia, tramo, horarioMatutino) ;

        // Buscamos DiasTramosHorarios por IdDiasTramosTipoHorario
        Optional<DiasTramosTipoHorario> diasTramosTipoHorarioOptional = this.diasTramosTipoHorarioRepository.findById(idDiasTramosTipoHorario) ;

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
