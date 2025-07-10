package es.iesjandula.reaktor.school_manager_server.rest.manager;

import java.util.List;

import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturasUnicasDto;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/resumenAsignaturas")
public class Paso4ResumenAsignaturasController
{
    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IMatriculaRepository iMatriculaRepository;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    /**
     * Método que obtiene todos los grupos asociados a un curso y etapa específicos.
     *
     * @param curso el identificador del curso proporcionado en el encabezado de la solicitud
     * @param etapa la etapa específica proporcionada en el encabezado de la solicitud
     * @return ResponseEntity que contiene una lista de objetos CursoEtapaGrupoDto si la operación es exitosa
     *         o un cuerpo de error si ocurre una excepción
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/grupos")
    public ResponseEntity<?> obtenerTodosGrupos(@RequestHeader(value = "curso", required = true) Integer curso,
                                                @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            // Obtener la lista de grupos según curso y etapa
            List<CursoEtapaGrupoDto> cursosEtapasGrupos = this.iCursoEtapaGrupoRepository.buscaTodosCursoEtapaGruposCreados(curso, etapa);

            // Devolver la lista de cursos, etapas y grupos encontrados
            return ResponseEntity.ok().body(cursosEtapasGrupos);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo encontrar el grupo";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Carga las asignaturas únicas disponibles según el curso y la etapa educativa proporcionados.
     * <p>
     * Este endpoint recupera una lista de asignaturas sin duplicados asociadas a los parámetros dados.
     *
     * @param curso el identificador numérico del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa la etapa educativa (por ejemplo, ESO, Bachillerato), proporcionada en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y la lista de asignaturas si se encuentran resultados.
     * - 404 (NOT_FOUND) si no hay asignaturas que coincidan con los filtros.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturasUnicas")
    public ResponseEntity<?> cargarAsignaturasUnicas(@RequestHeader("curso") int curso,
                                                     @RequestHeader("etapa") String etapa)
    {
        try
        {
            List<AsignaturasUnicasDto> asignaturas = iAsignaturaRepository.findByCursoAndEtapaDistinct(curso, etapa);

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No existen asignaturas para " + curso + " " + etapa;
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
            String msgError = "ERROR - Error al acceder a la base de datos";
            log.error(msgError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, msgError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene el número de alumnos matriculados en una asignatura dentro de un grupo específico,
     * según el curso y la etapa proporcionados.
     * <p>
     * Este endpoint busca la cantidad de alumnos asociados a una asignatura concreta
     * en un curso, etapa y grupo determinados.
     *
     * @param curso      el identificador numérico del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa      la etapa educativa (por ejemplo, ESO, Bachillerato), proporcionada en la cabecera de la solicitud.
     * @param grupo      la letra del grupo (por ejemplo, A, B, C), proporcionada en la cabecera de la solicitud.
     * @param asignatura el nombre de la asignatura, proporcionado en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y el número de alumnos si la consulta es exitosa.
     * - 404 (NOT_FOUND) si no se encuentra el grupo o la asignatura especificada.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la operación.
     */

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/numeroAlumnosEnAsignatura")
    public ResponseEntity<?> obtenerCantidadAlumnosEnGrupoPorAsignatura(@RequestHeader(value = "curso", required = true) Integer curso,
                                                                        @RequestHeader(value = "etapa", required = true) String etapa,
                                                                        @RequestHeader(value = "grupo", required = true) String grupo,
                                                                        @RequestHeader(value = "asignatura", required = true) String asignatura)
    {
        try
        {
            List<String> listaGrupos = iCursoEtapaGrupoRepository.buscaLetrasGruposDeCursoEtapas(curso, etapa);

            // Si no esta ese grupo lanzar excepcion
            if (!listaGrupos.contains(grupo))
            {
                String mensajeError = "No se ha encontrado ningún grupo para " + curso + " " + etapa + " con letra " + grupo;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.GRUPO_NO_ENCONTRADO, mensajeError);
            }

            // Alumnos en el grupo
            Long numAlumnos = iMatriculaRepository.numeroAlumnosPorGrupoYAsignatura(curso, etapa, grupo, asignatura);

            // Devolver la lista
            log.info("INFO - Lista de los cursos etapas");
            return ResponseEntity.ok().body(numAlumnos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada con código y el mensaje de error
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo cargar el numero de alumnos";
            log.error(msgError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, msgError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException);
        }
    }
}
