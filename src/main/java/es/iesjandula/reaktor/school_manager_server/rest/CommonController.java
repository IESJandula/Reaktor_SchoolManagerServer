package es.iesjandula.reaktor.school_manager_server.rest;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.services.ReduccionProfesorService;
import es.iesjandula.reaktor.school_manager_server.services.ValidacionesGlobales;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/common")
public class CommonController
{
    @Autowired
    private ICursoEtapaRepository iCursoEtapaRepository;

    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IProfesorReduccionRepository iProfesorReduccionRepository;

    @Autowired
    private ReduccionProfesorService reduccionProfesorService;

    @Autowired
    private ValidacionesGlobales validacionesGlobales;

    /**
     * Endpoint para obtener los cursos etapas.
     *
     * Este método obtiene mediante un get todos los cursos etapas guardados en base
     * de datos para despues mostrarlos en el front en un select.
     *
     * @return ResponseEntity<?> - Respuesta con las lista de cursos y etapas.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/cursoEtapa")
    public ResponseEntity<?> obtenerCursoEtapa()
    {
        try
        {
            // Lista usada para guardar los registros de la Tabla CursoEtapa
            List<CursoEtapa> listaCursoEtapa = this.iCursoEtapaRepository.findAll();

            // Si la lista esta vacia, lanzar excepcion
            if (listaCursoEtapa.isEmpty())
            {
                String mensajeError = "ERROR - Sin cursos y etapas en la base de datos";

                // Lanzar excepcion y mostrar log con mensaje diferente
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_CURSOS_ETAPAS_ENCONTRADOS, mensajeError);
            }

            // Devolver la lista
            log.info("INFO - Lista de los cursos etapas");
            return ResponseEntity.status(200).body(listaCursoEtapa);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo cargar la lista";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para obtener los grupos asociados a un curso y etapa específicos.
     *
     * Este método consulta los grupos disponibles para un curso y etapa
     * determinados.
     * Si no se encuentran grupos para la combinación de curso y etapa
     * proporcionados,
     * se lanza una excepción personalizada. En caso de un error general, se maneja
     * adecuadamente
     * la excepción y se devuelve un mensaje de error.
     *
     * @param curso - El identificador del curso para el cual se desean obtener los
     *              grupos.
     * @param etapa - La etapa educativa asociada al curso para la cual se desean
     *              obtener los grupos.
     * @return ResponseEntity<?> - La lista de grupos encontrados o una excepción
     *         con el mensaje de error si no se encontraron grupos o si ocurre algún
     *         fallo durante el proceso.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/grupos")
    public ResponseEntity<?> obtenerGrupos(@RequestHeader(value = "curso", required = true) Integer curso,
                                           @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            // Obtener la lista de grupos según curso y etapa
            List<CursoEtapaGrupoDto> cursosEtapasGrupos = this.iCursoEtapaGrupoRepository.buscaCursoEtapaGruposCreados(curso, etapa);

            // Si la lista está vacía, lanzar una excepción
            if (cursosEtapasGrupos.isEmpty())
            {
                // Lanzar excepcion y mostrar log con mensaje de Error
                String mensajeError = "ERROR - No se encontraron grupos para el curso " + curso + " y etapa " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.CURSO_ETAPA_GRUPO_NO_ENCONTRADO, mensajeError);
            }

            // Log de información antes de la respuesta
            log.info("INFO - Se han encontrado los siguientes grupos para el curso: {} y etapa: {}", curso, etapa);

            // Devolver la lista de cursos, etapas y grupos encontrados
            return ResponseEntity.status(200).body(cursosEtapasGrupos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo encontrar el grupo";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/asignarReducciones")
    private ResponseEntity<?> asignarReduccion(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                               @RequestHeader(value = "email") String email,
                                               @RequestHeader(value = "reduccion") String nombreReduccion,
                                               @RequestHeader(value = "horas") Integer horasReduccion)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }

            Reduccion reduccion = this.reduccionProfesorService.validadarYObtenerReduccion(nombreReduccion, horasReduccion);

            Profesor profesor = this.reduccionProfesorService.validadarYObtenerProfesor(email);

            IdProfesorReduccion idProfesorReduccion = new IdProfesorReduccion(profesor, reduccion);

            Optional<ProfesorReduccion> optionalProfesorReduccion = this.iProfesorReduccionRepository.findById(idProfesorReduccion);

            if(optionalProfesorReduccion.isPresent())
            {
                String mensajeError = "Ya has asignado esa reducción a ese profesor";

                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            ProfesorReduccion profesorReduccion = new ProfesorReduccion();
            profesorReduccion.setIdProfesorReduccion(idProfesorReduccion);

            this.iProfesorReduccionRepository.saveAndFlush(profesorReduccion);

            return ResponseEntity.ok().build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {

            String mensajeError = "Error al acceder a la base de datos";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
