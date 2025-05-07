package es.iesjandula.reaktor.school_manager_server.services;

import es.iesjandula.reaktor.school_manager_server.models.Constantes;
import es.iesjandula.reaktor.school_manager_server.repositories.IConstantesRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ValidacionesGlobales
{
    @Autowired
    private IConstantesRepository iConstantesRepository;

    /**
     * @throws SchoolManagerServerException con un error
     */
    public void validacionesGlobalesPreviasEleccionHorarios() throws SchoolManagerServerException
    {

        // Vemos si la elección de horarios está deshabilitada
        Optional<Constantes> optionalAppDeshabilitada = this.iConstantesRepository
                .findByClave(Constants.TABLA_CONST_SELECCION_HORARIOS_POR_CLAUSTRO);

        if (optionalAppDeshabilitada.isEmpty())
        {
            String errorString = "Error obteniendo parametros";

            log.error(errorString + ". " + Constants.TABLA_CONST_SELECCION_HORARIOS_POR_CLAUSTRO);
            throw new SchoolManagerServerException(Constants.ERROR_OBTENIENDO_PARAMETROS, errorString);
        }

        if (!optionalAppDeshabilitada.get().getValor().isEmpty())
        {
            String infoAppDeshabilitada = optionalAppDeshabilitada.get().getValor();
            if (infoAppDeshabilitada != null)
            {
                log.error(infoAppDeshabilitada);
                throw new SchoolManagerServerException(Constants.ERROR_APP_DESHABILITADA, infoAppDeshabilitada);
            }
        }
    }
}
