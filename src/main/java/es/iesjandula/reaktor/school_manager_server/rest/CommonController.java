package es.iesjandula.reaktor.school_manager_server.rest;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.services.DiaTramoTipoHorarioService;
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
    private IProfesorReduccionRepository iProfesorReduccionRepository;

    @Autowired
    private DiaTramoTipoHorarioService diaTramoTipoHorarioService;

    @Autowired
    private ReduccionProfesorService reduccionProfesorService;

    @Autowired
    private ValidacionesGlobales validacionesGlobales;

    /**
     * Recupera todos los registros de cursos y etapas disponibles en la base de datos.
     * <p>
     * Si no se encuentran registros, se lanza una excepción personalizada. También se maneja
     * cualquier excepción general que pueda surgir durante la operación, devolviendo respuestas
     * HTTP adecuadas con detalles del error.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y la lista de objetos {@code CursoEtapa} si se obtienen registros.
     * - 400 (BAD_REQUEST) si no existen cursos ni etapas en la base de datos.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la operación.
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
            return ResponseEntity.ok().body(listaCursoEtapa);
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
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/diasSemana")
    public ResponseEntity<?> obtenerDiasSemana()
    {
        try
        {
            // Obtenemos los días de la semana
            List<String> diasSemana = this.diaTramoTipoHorarioService.obtenerDiasSemana() ;

            return ResponseEntity.ok(diasSemana) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener los días de la semana";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/tramosHorarios")
    public ResponseEntity<?> obtenerTramosHorarios()
    {
        try
        {
            // Obtenemos los tramos horarios
            List<String> tramosHorarios = this.diaTramoTipoHorarioService.obtenerTramosHorarios() ;

            return ResponseEntity.ok(tramosHorarios) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener los tramos horarios";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna una reducción horaria a un profesor específico, validando previamente los datos proporcionados.
     * <p>
     * El método verifica la existencia de la reducción y del profesor, y evita duplicidades en las asignaciones.
     *
     * @param usuario         el usuario autenticado que realiza la solicitud, utilizado para validaciones de acceso.
     * @param email           el correo electrónico del profesor al que se desea asignar la reducción, indicado en la cabecera.
     * @param nombreReduccion el nombre de la reducción que se desea asignar, indicado en la cabecera.
     * @param horasReduccion  el número de horas de la reducción, indicado en la cabecera.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) si la reducción se asigna correctamente.
     * - 409 (CONFLICT) si la reducción ya estaba asignada al profesor.
     * - 500 (INTERNAL_SERVER_ERROR) si se produce un error inesperado durante el proceso.
     */
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

            if (optionalProfesorReduccion.isPresent())
            {
                String mensajeError = "La reducción " + nombreReduccion + " ya ha sido asignada a " + profesor.getNombre() + " " + profesor.getApellidos();

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.REDUCCION_ASIGNADA, mensajeError);
            }

            ProfesorReduccion profesorReduccion = new ProfesorReduccion();
            profesorReduccion.setIdProfesorReduccion(idProfesorReduccion);

            this.iProfesorReduccionRepository.saveAndFlush(profesorReduccion);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Error inesperado al asignar la reducción al profesor";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
