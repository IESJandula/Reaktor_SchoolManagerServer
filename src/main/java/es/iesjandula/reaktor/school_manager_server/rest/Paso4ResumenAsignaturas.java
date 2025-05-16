package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.List;

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
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDtoSinGrupo;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/resumenAsignaturas")
public class Paso4ResumenAsignaturas
{
    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IMatriculaRepository iMatriculaRepository;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    /**
     * Endpoint para obtener las asignaturas de los cursos etapas.
     * <p>
     * Este método recibe los parámetros del curso y la etapa y luego recupera una lista
     * de asignaturas mostrando su nombre, el nº de horas, el nº de alumnos tanto en general
     * como en los distintos grupos.
     *
     * @param curso - El curso para el que se solicita la lista de alumnos.
     * @param etapa - La etapa para la cual se solicita la lista de alumnos.
     * @return ResponseEntity<?> - Respuesta con la lista de asignaturas mapeando un dto para mostrar los datos de las asignaturas.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturasUnicas")
    public ResponseEntity<?> cargarAsignaturasUnicas(@RequestHeader("curso") int curso,
                                                     @RequestHeader("etapa") String etapa)
    {
        try
        {
            List<AsignaturaDtoSinGrupo> asignaturas = iAsignaturaRepository.findByCursoAndEtapaDistinct(curso, etapa);

            if (asignaturas.isEmpty())
            {
                String mensajeError = "ERROR - No existen asignaturas con ese curso y etapa";
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
     * Obtiene la cantidad de alumnos en un grupo específico para una asignatura determinada.
     *
     * @param curso      El curso académico del cual se solicita la información.
     * @param etapa      La etapa educativa en la que se encuentra el curso (por ejemplo, Primaria, Secundaria).
     * @param grupo      La letra del grupo al que pertenece la información solicitada.
     * @param asignatura El nombre de la asignatura para la cual se solicita la cantidad de alumnos.
     * @return ResponseEntity<?> Respuesta que contiene el número de alumnos del grupo para la asignatura solicitada.
     * Puede devolver errores en caso de que el grupo no exista o de que ocurra algún fallo.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/numeroAlumnosEnAsignatura")
    public ResponseEntity<?> obtenerCantidadAlumnosEnGrupoPorAsignatura(@RequestHeader(value = "curso", required = true) Integer curso,
                                                                        @RequestHeader(value = "etapa", required = true) String etapa,
                                                                        @RequestHeader(value = "grupo", required = true) Character grupo,
                                                                        @RequestHeader(value = "asignatura", required = true) String asignatura)
    {
        try
        {
            List<Character> listaGrupos = iCursoEtapaGrupoRepository.buscaLetrasGruposDeCursoEtapas(curso, etapa);

            // Si no esta ese grupo lanzar excepcion
            if (!listaGrupos.contains(grupo))
            {
                String mensajeError = "ERROR - No se ha encontrado ningún grupo para el " + curso + " y " + etapa + " con letra " + grupo;
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
