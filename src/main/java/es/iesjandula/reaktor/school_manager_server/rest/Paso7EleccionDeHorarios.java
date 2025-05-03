package es.iesjandula.reaktor.school_manager_server.rest;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDtoSinGrupo;
import es.iesjandula.reaktor.school_manager_server.dtos.ImpartirAsignaturaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdImpartir;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/eleccionDeHorarios")
public class Paso7EleccionDeHorarios
{

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private IImpartirRepository  iImpartirRepository;

    @Autowired
    private IReduccionRepository iReduccionRepository;

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
    public ResponseEntity<?> obtenerAsignaturas()
    {
        try
        {
            List<ImpartirAsignaturaDto> asignaturaDtoSinGrupos = this.iAsignaturaRepository.encontrarAsignaturasSinGrupo();

            if(asignaturaDtoSinGrupos.isEmpty())
            {
                String mensajeError = "Error - No se han encontrado asignaturas";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            return ResponseEntity.ok().body(asignaturaDtoSinGrupos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/asignaturas")
    public ResponseEntity<?> asignarAsignatura(@RequestHeader(value = "nombre") String nombreAsignatura,
                                               @RequestHeader(value = "horas") Integer horas,
                                               @RequestHeader(value = "curso") Integer curso,
                                               @RequestHeader(value = "etapa") String etapa,
                                               @RequestHeader(value = "grupo") Character grupo,
                                               @RequestHeader(value = "email") String email)
    {
        try
        {
            Impartir asignaturaImpartir = this.iImpartirRepository.encontrarAsignaturaAsignada(nombreAsignatura, horas, curso, etapa, grupo, email);

            if(asignaturaImpartir != null)
            {
                String mensajeError = "Error - Ya se ha asignado esa asignatura a otro profesor";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
            idCursoEtapaGrupo.setCurso(curso);
            idCursoEtapaGrupo.setEtapa(etapa);
            idCursoEtapaGrupo.setGrupo(grupo);

            CursoEtapaGrupo cursoEtapaGrupo =  new CursoEtapaGrupo();
            cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

            IdAsignatura idAsignatura = new IdAsignatura();
            idAsignatura.setNombre(nombreAsignatura);
            idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);

            Asignatura asignatura = new Asignatura();
            asignatura.setIdAsignatura(idAsignatura);

            Profesor profesor = new Profesor();
            profesor.setEmail(email);

            IdImpartir idImpartir = new IdImpartir(asignatura, profesor);

            Impartir asignarAsignatura = new Impartir();
            asignarAsignatura.setCupoHoras(horas);
            asignarAsignatura.setIdImpartir(idImpartir);

            this.iImpartirRepository.saveAndFlush(asignarAsignatura);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/reduccion")
    public ResponseEntity<?> obtenerReducciones(@AuthenticationPrincipal DtoUsuarioExtended usuario)
    {
        try
        {
            if(usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                List<ReduccionDto> listReduccion = this.iReduccionRepository.encontrarTodasReducciones();

                if(listReduccion.isEmpty())
                {
                    String mensajeError = "Error - No se han encontro reducciones en la base de datos";
                    log.error(mensajeError);
                    throw new SchoolManagerServerException(1, mensajeError);
                }

                return ResponseEntity.ok().body(listReduccion);
            }

            List<ReduccionProfesoresDto> listReduccionesProfesores = this.iReduccionRepository.encontrarReduccionesParaProfesores();

            if(listReduccionesProfesores.isEmpty())
            {
                String mensajeError = "Error - No se han encontro reducciones en la base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }
            return ResponseEntity.ok().body(listReduccionesProfesores);

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
