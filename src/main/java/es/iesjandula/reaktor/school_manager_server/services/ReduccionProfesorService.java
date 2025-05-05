package es.iesjandula.reaktor.school_manager_server.services;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ReduccionProfesorService
{
    @Autowired
    private IReduccionRepository iReduccionRepository;

    @Autowired
    private IProfesorRepository iProfesorRepository;

    public Reduccion validadarYObtenerReduccion(String nombreReduccion, Integer horasReduccion) throws SchoolManagerServerException
    {
        IdReduccion idReduccion = new IdReduccion(nombreReduccion, horasReduccion);
        Optional<Reduccion> reduccion = this.iReduccionRepository.findById(idReduccion);

        if(reduccion.isEmpty())
        {
            String mensajeError = "No existe una reducción con ese nombre y esas horas";

            log.error(mensajeError);
            throw new SchoolManagerServerException(1, mensajeError);
        }
        return reduccion.get();
    }

    public Profesor validadarYObtenerProfesor(String email) throws SchoolManagerServerException
    {
        Optional<Profesor> profesor = this.iProfesorRepository.findById(email);

        if(profesor.isEmpty())
        {
            String mensajeError = "No existe un profesor con ese nombre y esos apellidos";

            log.error(mensajeError);
            throw new SchoolManagerServerException(1, mensajeError);
        }
        return profesor.get();
    }
}
