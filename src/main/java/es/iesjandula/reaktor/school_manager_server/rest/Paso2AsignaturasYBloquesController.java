package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaHorasDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Bloque;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IBloqueRepository;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/asignaturasYBloques")
public class Paso2AsignaturasYBloquesController
{
    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private IBloqueRepository iBloqueRepository;

    /**
     * Obtiene la lista de asignaturas de un curso y etapa determinados.
     * <p>
     * Devuelve el nombre de la asignatura, el número de horas y el número de alumnos, tanto en general como por grupos.
     *
     * @param curso el curso para el que se solicitan las asignaturas.
     * @param etapa la etapa educativa correspondiente.
     * @return una {@link ResponseEntity} con:
     *         - 200 (OK) y la lista de asignaturas si se encuentra información.
     *         - 404 (Not Found) si no existen asignaturas para ese curso y etapa.
     *         - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
    public ResponseEntity<?> cargarAsignaturas(@RequestHeader("curso") int curso,
                                               @RequestHeader("etapa") String etapa)
    {
        try
        {
            List<AsignaturaDto> asignaturas = iAsignaturaRepository.findByCursoAndEtapa(curso, etapa);

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No existen asignaturas para " + curso + etapa;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            return ResponseEntity.ok().body(asignaturas);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudieron cargar las asignaturas";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Crea un nuevo bloque de asignaturas para un curso y etapa determinados.
     * <p>
     * El método valida que se hayan seleccionado al menos dos asignaturas, verifica que existan
     * y comprueba que no estén ya asignadas a otro bloque antes de crear uno nuevo.
     *
     * @param curso el identificador del curso para el que se crea el bloque.
     * @param etapa la etapa educativa asociada al bloque.
     * @param asignaturas una lista de nombres de asignaturas que se incluirán en el bloque;
     *                    debe contener al menos dos asignaturas.
     * @return una {@link ResponseEntity} con:
     *         - 201 (Created) y el ID del bloque si se crea correctamente.
     *         - 400 (Bad Request) si se seleccionan menos de dos asignaturas.
     *         - 404 (Not Found) si alguna asignatura no se encuentra.
     *         - 409 (Conflict) si alguna asignatura ya está asignada a otro bloque.
     *         - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/bloques")
    public ResponseEntity<?> crearBloques(@RequestParam("curso") int curso,
                                          @RequestParam("etapa") String etapa,
                                          @RequestParam("asignaturas") List<String> asignaturas)
    {
        try
        {

            if (asignaturas == null || asignaturas.size() < 2)
            {
                String mensajeError = "Tienes que seleccionar al menos 2 asignaturas";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURAS_MINIMAS_NO_SELECCIONADAS, mensajeError);
            }

            Bloque bloque = new Bloque();

            for (String asignaturaString : asignaturas)
            {
                List<Optional<Asignatura>> optionalAsignatura = this.iAsignaturaRepository.findAsignaturasByCursoEtapaAndNombre(curso, etapa, asignaturaString);

                for (Optional<Asignatura> asignatura : optionalAsignatura)
                {

                    if (asignatura.isEmpty())
                    {
                        String mensajeError = "La asignatura no fue encontrada";
                        log.error(mensajeError);
                        throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
                    }

                    if (asignatura.get().getBloqueId() != null)
                    {
                        String mensajeError = "Una de las asignaturas ya tiene un bloque asignado";
                        log.error(mensajeError);
                        throw new SchoolManagerServerException(Constants.ASIGNATURA_CON_BLOQUE, mensajeError);
                    }

                    this.iBloqueRepository.save(bloque);

                    asignatura.get().setBloqueId(bloque);

                    iAsignaturaRepository.saveAndFlush(asignatura.get());
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(bloque.getId());

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.ASIGNATURAS_MINIMAS_NO_SELECCIONADAS)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_NO_ENCONTRADA)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo crear el bloque";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina un bloque de asignaturas en función del curso, la etapa y el nombre de la asignatura proporcionados.
     * <p>
     * Si al desvincular la asignatura, el bloque ya no contiene más asignaturas asociadas, el bloque será eliminado.
     *
     * @param curso  el identificador del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa  la etapa educativa, proporcionada en la cabecera de la solicitud.
     * @param nombre el nombre de la asignatura a desvincular del bloque, proporcionado en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si el bloque se eliminó correctamente.
     * - 404 (NOT_FOUND) si no se encontró la asignatura.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurrió un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/bloques")
    public ResponseEntity<?> eliminarBloque(@RequestHeader(value = "curso", required = true) Integer curso,
                                            @RequestHeader(value = "etapa", required = true) String etapa,
                                            @RequestHeader(value = "nombre", required = true) String nombreAsignatura)
    {
        try
        {

            // Buscamos la asignatura
            Asignatura asignatura = iAsignaturaRepository.encontrarPorCursoYEtapaYNombre(curso, etapa, nombreAsignatura);

            if (asignatura == null)
            {
                String mensajeError = "No se han encontrado " + nombreAsignatura + " en " + curso + etapa;
                log.error(mensajeError);

                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            // Desasociar la asignatura del bloque
            Bloque bloque = asignatura.getBloqueId();
            asignatura.setBloqueId(null);

            this.iAsignaturaRepository.saveAndFlush(asignatura);

            if (bloque != null && bloque.getAsignaturas().isEmpty())
            {
                iBloqueRepository.delete(bloque);
            }
            else
            {
                log.info("Queda bloques por eliminar");
            }

            Long bloqueId = -1l;
            if (bloque != null)
            {
                bloqueId = bloque.getId();
            }

            log.info("INFO - Bloque " + bloqueId + " eliminado con éxito");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Error al eliminar el bloque";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Actualiza el estado de docencia de una asignatura por su nombre.
     * <p>
     * Permite marcar una asignatura como sin docencia o restaurarla a su estado original.
     *
     * @param nombreAsignatura el nombre de la asignatura a actualizar.
     * @param sinDocencia true si se desea marcar como sin docencia; false para restaurar.
     * @return una {@link ResponseEntity} con:
     *         - 204 (No Content) si la operación se realiza correctamente.
     *         - 404 (Not Found) si no se encuentra la asignatura.
     *         - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/sinDocencia")
    public ResponseEntity<?> asignaturasSinDocencia(@RequestHeader("nombreAsignatura") String nombreAsignatura,
                                                    @RequestHeader("sinDocencia") Boolean sinDocencia)
    {
        try
        {
            Optional<Asignatura> asignaturaOpt = this.iAsignaturaRepository.encontrarAsignaturaPorNombre(nombreAsignatura);

            if (asignaturaOpt.isEmpty())
            {
                String mensajeError = "No se ha encontrado " + nombreAsignatura + " en base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            Asignatura asignatura = asignaturaOpt.get();
            asignatura.setSinDocencia(sinDocencia);

            this.iAsignaturaRepository.saveAndFlush(asignatura);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo actualizar el estado de docencia de la asignatura";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene el número de horas de las asignaturas para un curso y etapa determinados.
     * <p>
     * Recupera desde el repositorio una lista de asignaturas con sus nombres y horas correspondientes.
     *
     * @param curso el identificador del curso enviado en la cabecera de la solicitud.
     * @param etapa la etapa educativa enviada en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     *         - 200 (OK) y la lista de asignaturas si se encuentran datos.
     *         - 404 (Not Found) si no se encuentran asignaturas.
     *         - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/horas")
    public ResponseEntity<?> mostrarHoras(@RequestHeader("curso") Integer curso,
                                          @RequestHeader("etapa") String etapa)
    {
        try
        {
            List<AsignaturaHorasDto> listAsignatuasHoras = this.iAsignaturaRepository.findNombreAndHorasByCursoEtapaAndNombres(curso, etapa);

            if (listAsignatuasHoras.isEmpty())
            {
                String mensajeError = "No se ha encontrado asignaturas con horas para '" + curso + etapa;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            return ResponseEntity.ok().body(listAsignatuasHoras);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudieron obtener las horas de las asignaturas";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna un número de horas a una asignatura, identificada por su curso, etapa y nombre.
     *
     * @param curso el curso en el que se encuentra la asignatura.
     * @param etapa la etapa educativa correspondiente.
     * @param nombreAsignatura el nombre de la asignatura a actualizar.
     * @param horas el número de horas a asignar.
     * @return una {@link ResponseEntity} con:
     *         - 204 (No Content) si la operación se realiza correctamente.
     *         - 404 (Not Found) si no se encuentra la asignatura.
     *         - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/horas")
    public ResponseEntity<?> asignarHoras(@RequestHeader("curso") Integer curso,
                                          @RequestHeader("etapa") String etapa,
                                          @RequestHeader("nombreAsignatura") String nombreAsignatura,
                                          @RequestHeader("horas") Integer horas)
    {
        try
        {
            List<Asignatura> listAsignatura = this.iAsignaturaRepository.findNombreByCursoEtapaAndNombres(curso, etapa, nombreAsignatura);

            if (listAsignatura == null)
            {
                String mensajeError = "No se ha encontrado la asignatura '" + nombreAsignatura + "' para '" + curso + etapa + "' paara asignar las horas";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            for (Asignatura asignatura : listAsignatura)
            {

                asignatura.setHoras(horas);
                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudieron asignar las horas a la asignatura";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}


