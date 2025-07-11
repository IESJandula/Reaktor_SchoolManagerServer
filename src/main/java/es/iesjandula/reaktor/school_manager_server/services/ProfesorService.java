package es.iesjandula.reaktor.school_manager_server.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.dtos.ProfesorDto;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProfesorService
{
    @Autowired
    private IProfesorRepository iProfesorRepository;

    /**
     * Obtiene la lista de profesores registrados en la base de datos.
     *
     * @return una lista de objetos {@link ProfesorDto} que contiene los datos de los profesores.
     * @throws SchoolManagerServerException si no se encuentran profesores registrados en la base de datos.
     */
    public List<ProfesorDto> obtenerProfesores() throws SchoolManagerServerException
    {
        List<Profesor> profesores = this.iProfesorRepository.findAll();

        if (profesores.isEmpty())
        {
            String mensajeError = "No se encontraron profesores registrados en la base de datos.";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.SIN_PROFESORES_ENCONTRADOS, mensajeError);
        }

        return profesores.stream().map(profesor -> new ProfesorDto(profesor.getNombre(),
                                                                   profesor.getApellidos(),
                                                                   profesor.getEmail())
                                      ).collect(Collectors.toList()) ;
    }

    /**
     * Busca un profesor en la base de datos por su email.
     *
     * @param email el email del profesor a buscar.
     * @return el profesor encontrado.
     * @throws SchoolManagerServerException si no se encuentra ningún profesor con el email proporcionado.
     */
    public Profesor buscarProfesor(String email) throws SchoolManagerServerException
    {
        Profesor profesor = this.iProfesorRepository.findByEmail(email);
        if (profesor == null)
        {
            String mensajeError = "No se encontró ningún profesor con el email: " + email;
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError);
        }

        return profesor;
    }
}
