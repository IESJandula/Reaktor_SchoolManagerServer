package es.iesjandula.reaktor.school_manager_server.services ;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CursoEtapaService
{
    @Autowired
    private ICursoEtapaRepository iCursoEtapaRepository ;

    /**
     * Método para validar si existe el curso y etapa en la BD y obtener el Curso Etapa
     * 
     * @param curso - El identificador del curso para el cual se desea obtener el Curso Etapa.
     * @param etapa - La etapa educativa asociada al curso para el cual se desea obtener el Curso Etapa.
     * @return CursoEtapa - El Curso Etapa encontrado en la base de datos.
     * @throws SchoolManagerServerException con un error
     */
    public CursoEtapa validarYObtenerCursoEtapa(Integer curso, String etapa) throws SchoolManagerServerException
    {
        // Validamos si existe el curso y etapa en la BD
        Optional<CursoEtapa> cursoEtapaOptional = this.iCursoEtapaRepository.findById(new IdCursoEtapa(curso, etapa));

        if(!cursoEtapaOptional.isPresent())
        {
            String mensajeError = "ERROR - No se ha encontrado el curso y etapa: " + curso + " " + etapa;
            
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.CURSO_ETAPA_NO_ENCONTRADO, mensajeError);
        }

        // Obtener el Curso Etapa
        return cursoEtapaOptional.get() ;
    }
}
