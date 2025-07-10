package es.iesjandula.reaktor.school_manager_server.rest.timetable;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorSesionAsignadaRepository;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaCursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaProfesorDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/schoolManager/horarios")
public class Paso4HorariosController
{
    @Autowired
    private IProfesorRepository iProfesorRepository;

    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IGeneradorSesionAsignadaRepository iGeneradorSesionAsignadaRepository;

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/individual")
    public ResponseEntity<?> obtenerHorarioIndividual(@RequestHeader(value = "email") String email)
    {
        try
        {
            // Buscamos el profesor
            Profesor profesor = this.iProfesorRepository.findByEmail(email) ;

            if (profesor == null)
            {
                String mensajeError = "ERROR - No se pudo obtener los horarios del profesor";

                // Logueamos y lanzamos la excepción
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError) ;
            }

            // Buscamos en la tabla GeneradorSesionAsignada todas las sesiones del profesor en la solución elegida
            Optional<List<GeneradorSesionAsignadaProfesorDto>> listaSesionesAsignadas = this.iGeneradorSesionAsignadaRepository.buscarHorarioProfesorSolucionElegida(profesor);

            if (!listaSesionesAsignadas.isPresent())
            {
                String mensajeError = "ERROR - No se pudo obtener los horarios del profesor";

                // Logueamos y lanzamos la excepción
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERROR_CODE_SESIONES_ASIGNADAS_NO_ENCONTRADAS, mensajeError) ;
            }

            return ResponseEntity.ok(listaSesionesAsignadas.get()) ;
        }      
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo obtener los horarios del profesor";   

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/cursoEtapaGrupo")
    public ResponseEntity<?> obtenerHorarioCursoEtapaGrupo(@RequestHeader("curso") int curso,
                                                           @RequestHeader("etapa") String etapa,
                                                           @RequestHeader("grupo") String grupo)
    {
        try
        {
            // Buscamos el curso etapa grupo
            CursoEtapaGrupo cursoEtapaGrupo = this.iCursoEtapaGrupoRepository.buscarCursoEtapaGrupo(curso, etapa, grupo) ;

            if (cursoEtapaGrupo == null)
            {
                String mensajeError = "ERROR - No se pudo obtener los horarios del curso etapa grupo";

                // Logueamos y lanzamos la excepción
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.CURSO_ETAPA_GRUPO_NO_ENCONTRADO, mensajeError) ;
            }

            // Buscamos en la tabla GeneradorSesionAsignada todas las sesiones del curso etapa grupo en la solución elegida
            Optional<List<GeneradorSesionAsignadaCursoEtapaGrupoDto>> listaSesionesAsignadas = this.iGeneradorSesionAsignadaRepository.buscarHorarioCursoEtapaGrupoSolucionElegida(cursoEtapaGrupo);

            if (!listaSesionesAsignadas.isPresent())
            {
                String mensajeError = "ERROR - No se pudo obtener los horarios del curso etapa grupo";

                // Logueamos y lanzamos la excepción
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERROR_CODE_SESIONES_ASIGNADAS_NO_ENCONTRADAS, mensajeError) ;
            }

            return ResponseEntity.ok(listaSesionesAsignadas.get()) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo obtener los horarios del curso etapa grupo";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
