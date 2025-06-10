package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import es.iesjandula.reaktor.school_manager_server.dtos.*;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/asignaturasYDepartamentos")
public class Paso5AsignaturasYDepartamentosController
{
    @Autowired
    private IDepartamentoRepository iDepartamentoRepository;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IImpartirRepository iImpartirRepository;

    /**
     * Recupera todos los departamentos registrados en la base de datos y los convierte en objetos DTO.
     * <p>
     * Si no se encuentran departamentos, se lanza una excepción personalizada. El método también maneja
     * excepciones generales, devolviendo códigos de estado HTTP apropiados y mensajes de error detallados.
     * <p>
     * Solo accesible para usuarios con rol de dirección.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de {@link DepartamentoDto} con los nombres de los departamentos si la operación es exitosa.
     * - 404 (NOT_FOUND) si no se encuentran departamentos.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante el proceso.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/departamentos")
    public ResponseEntity<?> obtenerDepartamentos()
    {
        try
        {
            List<Departamento> departamentos = iDepartamentoRepository.findAll();

            if (departamentos.isEmpty())
            {
                String mensajeError = "No se encontraron departamentos registrados en el sistema.";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(Constants.DEPARTAMENTO_NO_ENCONTRADO, mensajeError);
            }

            // Convertimos los departamentos a DTOs
            List<DepartamentoDto> departamentosDto = departamentos.stream()
                    .map(departamento -> new DepartamentoDto(departamento.getNombre()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok().body(departamentosDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se ha producido un error inesperado al intentar obtener la lista de departamentos.";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna un número máximo de plantilla de profesores a un departamento existente en el sistema.
     * <p>
     * El departamento es identificado por su nombre. Si no se encuentra el departamento, se lanza una
     * excepción personalizada. También se manejan errores generales de servidor.
     * <p>
     * Este método requiere permisos de dirección para ser ejecutado.
     *
     * @param nombre    el nombre del departamento al que se desea asignar la plantilla de profesores.
     * @param plantilla número máximo de plantilla de profesores a asignar al departamento.
     * @return un {@link ResponseEntity} con:
     * - Código HTTP 204 (No Content) si la asignación fue exitosa.
     * - Código HTTP 404 (Not Found) si el departamento no fue encontrado.
     * - Código HTTP 500 (Internal Server Error) en caso de error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/departamentos")
    public ResponseEntity<?> asignarProfesoresADepartamentos(@RequestParam("nombre") String nombre,
                                                             @RequestParam("plantilla") int plantilla)
    {
        try
        {
            Optional<Departamento> departamentoOpt = this.iDepartamentoRepository.findByNombre(nombre);

            if (departamentoOpt.isEmpty())
            {
                String mensajeError = "No se ha encontrado ningún departamento con el nombre: " + nombre;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.DEPARTAMENTO_NO_ENCONTRADO, mensajeError);
            }

            Departamento departamento = departamentoOpt.get();
            departamento.setPlantilla(plantilla);

            iDepartamentoRepository.saveAndFlush(departamento);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se ha producido un error inesperado al asignar plantilla al departamento: " + nombre;
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene una lista de asignaturas junto con los departamentos a los que pertenecen.
     * <p>
     * Este método consulta los datos desde el repositorio, los transforma en DTOs y los devuelve
     * en la respuesta. Es accesible únicamente para usuarios con rol de dirección.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@link AsignaturaConDepartamentoDto} si la operación es exitosa.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un fallo en el servidor.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas/infoDepartamentos")
    public ResponseEntity<?> obtenerDatosDepartamentosConAsignaturas()
    {
        try
        {
            List<AsignaturaConDepartamentoDto> asignaturaConDepartamentoDtos = this.iAsignaturaRepository.encontrarAsignaturasConDepartamento();

            return ResponseEntity.ok(asignaturaConDepartamentoDtos);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se ha producido un error inesperado al obtener la información de asignaturas con sus departamentos.";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera todos los registros de CursoEtapaGrupo, los transforma en objetos DTO y los devuelve como respuesta.
     * <p>
     * Los datos incluyen curso, etapa, grupo, si tiene horario matutino y si pertenece a ESO o Bachillerato.
     * <p>
     * Este método es seguro y requiere permisos de dirección.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de {@link CursoEtapaGrupoDto} si hay datos disponibles.
     * - 404 (NOT_FOUND) si no se encuentra ningún registro.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/curso")
    public ResponseEntity<?> obtenerCursosEtapasGrupos()
    {
        try
        {
            List<CursoEtapaGrupo> cursoEtapaGrupos = this.iCursoEtapaGrupoRepository.buscarTodosLosCursosEtapasGrupos();

            if (cursoEtapaGrupos.isEmpty())
            {
                String mensajeError = "No hay curso, etapa y grupo registradas en la base de datos";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(Constants.CURSO_ETAPA_GRUPO_NO_ENCONTRADO, mensajeError);
            }

            List<CursoEtapaGrupoDto> cursoEtapaGrupoDto = cursoEtapaGrupos.stream().map(cursoEtapaGrupo ->
                    new CursoEtapaGrupoDto(
                            cursoEtapaGrupo.getIdCursoEtapaGrupo().getCurso(),
                            cursoEtapaGrupo.getIdCursoEtapaGrupo().getEtapa(),
                            cursoEtapaGrupo.getIdCursoEtapaGrupo().getGrupo(),
                            cursoEtapaGrupo.getHorarioMatutino(),
                            cursoEtapaGrupo.getEsoBachillerato()
                    )).collect(Collectors.toList());

            return ResponseEntity.ok(cursoEtapaGrupoDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se ha producido un error inesperado al obtener los datos del desplegable de curso, etapa y grupo.";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera las asignaturas registradas para un curso, etapa y grupo específicos.
     * <p>
     * Las asignaturas se devuelven en forma de DTOs que contienen únicamente el nombre de cada una.
     * Si no se encuentra ninguna asignatura para la combinación especificada, se lanza una excepción.
     *
     * @param curso el identificador del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa la etapa educativa, proporcionada en la cabecera de la solicitud.
     * @param grupo el identificador del grupo al que pertenece la asignatura, proporcionado en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de {@link NombreAsignaturaDto} si se encuentran asignaturas.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturasPorCursoEtapaGrupo")
    public ResponseEntity<?> obtenerAsignaturasPorCursoEtapaGrupo(@RequestParam("curso") int curso,
                                                                  @RequestParam("etapa") String etapa,
                                                                  @RequestParam("grupo") String grupo)
    {
        try
        {
            List<Asignatura> asignaturas = iAsignaturaRepository.asignaturasPorCursoEtapaGrupo(curso, etapa, grupo);

            // Convertimos las asignaturas a DTOs
            List<NombreAsignaturaDto> NombreAsignaturaDto = asignaturas.stream().map(asignatura ->
                    new NombreAsignaturaDto(
                            asignatura.getIdAsignatura().getNombre()
                    )).collect(Collectors.toList());

            return ResponseEntity.ok(NombreAsignaturaDto);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se ha producido un error inesperado al obtener las asignaturas por curso, etapa y grupo.";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera todas las asignaturas registradas en el sistema.
     * <p>
     * Este método obtiene la lista de todas las asignaturas, las convierte al formato DTO y las devuelve.
     * Si no hay asignaturas registradas, lanza una excepción con un mensaje de error adecuado.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@link AsignaturaInfoDto} si existen asignaturas.
     * - 404 (NOT_FOUND) si no se encontraron asignaturas.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurrió un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
    public ResponseEntity<?> obtenerTodasLasAsignaturas()
    {
        try
        {
            List<Asignatura> asignaturas = iAsignaturaRepository.findAll();

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No se encontraron asignaturas registradas en la base de datos.";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            // Convertimos a DTO
            List<AsignaturaInfoDto> asignaturasDto = asignaturas.stream().map(asignatura ->
                    new AsignaturaInfoDto(
                            asignatura.getIdAsignatura().getNombre(),
                            asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getCurso(),
                            asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getEtapa(),
                            asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getGrupo(),
                            (asignatura.getDepartamentoPropietario() != null) ? asignatura.getDepartamentoPropietario().getNombre() : null,
                            (asignatura.getDepartamentoReceptor() != null) ? asignatura.getDepartamentoReceptor().getNombre() : null,
                            asignatura.getHoras()
                    )
            ).collect(Collectors.toList());

            return ResponseEntity.ok(asignaturasDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se ha producido un error inesperado al obtener la lista completa de asignaturas.";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina la asociación entre una asignatura y sus departamentos (departamentoPropietario y departamentoReceptor)
     * en función del curso, la etapa, el grupo y el nombre proporcionados.
     * <p>
     * Si se encuentra la asignatura correspondiente, se desvinculará de ambos departamentos.
     *
     * @param curso  el identificador del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa  la etapa educativa, proporcionada en la cabecera de la solicitud.
     * @param grupo  el identificador del grupo al que pertenece la asignatura, proporcionado en la cabecera de la solicitud.
     * @param nombre el nombre de la asignatura cuya asociación se desea eliminar, proporcionado en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la desvinculación se realizó correctamente.
     * - 404 (NOT_FOUND) si no se encontró la asignatura.
     * - 409 (CONFLICT) la asignatura está asignada a un profesor
     * - 500 (INTERNAL_SERVER_ERROR) si ocurrió un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PATCH, value = "/asignaturas/quitarDepartamentos")
    public ResponseEntity<?> quitarAsignaturasDeDepartamentos(@RequestParam("curso") int curso,
                                                              @RequestParam("etapa") String etapa,
                                                              @RequestParam("grupo") String grupo,
                                                              @RequestParam("nombre") String nombre)
    {
        try
        {
            // Buscar la asignatura con los datos proporcionados
            Optional<Asignatura> asignaturaOpt = iAsignaturaRepository
                    .findAsignaturasByCursoEtapaGrupoAndNombre(curso, etapa, grupo, nombre);

            if (asignaturaOpt.isEmpty())
            {
                String mensajeError = "No se encontró ninguna asignatura con los datos: " + curso + " " + etapa + " " + grupo + ", " + nombre;
                log.warn(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            // Obtener la asignatura y actualizar los departamentos a null
            Asignatura asignatura = asignaturaOpt.get();
            asignatura.setDepartamentoPropietario(null);
            asignatura.setDepartamentoReceptor(null);

//          Comprobamos si la asignatura ya está asignada a profesores
            if (!iImpartirRepository.encontrarAsignaturaImpartidaPorNombreAndCursoEtpa(asignatura.getIdAsignatura().getNombre(), asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getCurso(),
                    asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getEtapa()).isEmpty())
            {
                String mensajeError = "No se puede borrar la asignatura ya que está asignadas a profesores";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_ASIGNADA_A_PROFESOR, mensajeError);
            }

            // Guardar cambios en la base de datos
            iAsignaturaRepository.saveAndFlush(asignatura);

            log.info("INFO - Departamentos eliminados para la asignatura: {}", nombre);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_NO_ENCONTRADA)
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
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar eliminar la asignatura asociada al departamento.";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna una asignatura a uno o más departamentos en función del curso, la etapa, el grupo y el nombre proporcionados.
     * <p>
     * Se actualiza el departamento propietario de la asignatura y, opcionalmente, un departamento receptor.
     *
     * @param curso                   el identificador del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa                   la etapa educativa, proporcionada en la cabecera de la solicitud.
     * @param grupo                   el identificador del grupo al que pertenece la asignatura, proporcionado en la cabecera de la solicitud.
     * @param nombre                  el nombre de la asignatura que se va a asignar, proporcionado en la cabecera de la solicitud.
     * @param departamentoPropietario el nombre del departamento que será propietario de la asignatura.
     * @param departamentoReceptor    el nombre del departamento receptor.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la asignación se realizó correctamente.
     * - 404 (NOT_FOUND) si no se encontró la asignatura o alguno de los departamentos.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurrió un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PATCH, value = "/asignaturas/asignarDepartamentos")
    public ResponseEntity<?> asignarAsignaturasADepartamentos(@RequestParam("curso") int curso,
                                                              @RequestParam("etapa") String etapa,
                                                              @RequestParam("grupo") String grupo,
                                                              @RequestParam("nombre") String nombre,
                                                              @RequestParam("departamentoPropietario") String departamentoPropietario,
                                                              @RequestParam(value = "departamentoReceptor") String departamentoReceptor)
    {
        try
        {
            // Buscar la asignatura con los datos proporcionados
            Optional<Asignatura> asignaturaOpt = iAsignaturaRepository
                    .findAsignaturasByCursoEtapaGrupoAndNombre(curso, etapa, grupo, nombre);

            if (asignaturaOpt.isEmpty())
            {
                String mensajeError = "No se encontró la asignatura con los datos " + curso + " " + etapa + " " + grupo + ", " + nombre;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            Asignatura asignatura = asignaturaOpt.get();

            // Obtener la asignatura y actualizar los departamentos
            Optional<Departamento> departamentoPropietarioOpt = iDepartamentoRepository.findByNombre(departamentoPropietario);
            Optional<Departamento> departamentoReceptorOpt = iDepartamentoRepository.findByNombre(departamentoReceptor);

            if (departamentoPropietarioOpt.isEmpty())
            {
                String mensajeError = "No se encontró el departamento propietario con el nombre " + departamentoPropietario;
                log.warn(mensajeError);
                throw new SchoolManagerServerException(Constants.DEPARTAMENTO_NO_ENCONTRADO, mensajeError);
            }

            departamentoReceptorOpt.ifPresent(asignatura::setDepartamentoReceptor);
            asignatura.setDepartamentoPropietario(departamentoPropietarioOpt.get());

            // Guardar cambios en la base de datos
            iAsignaturaRepository.saveAndFlush(asignatura);

            log.info("INFO - Departamentos asignados para la asignatura: {}", nombre);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se ha producido un error inesperado al intentar asignar la asignatura a los departamentos";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}