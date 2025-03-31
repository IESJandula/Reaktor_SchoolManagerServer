package es.iesjandula.school_manager_server.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.school_manager_server.dtos.AsignaturaDtoSinGrupo;
import es.iesjandula.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/resumenAsignaturas")
public class Paso4ResumenAsignaturas 
{
	
	@Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;
	
	@Autowired
    private IMatriculaRepository iMatriculaRepository;
	
	@Autowired
	private IAsignaturaRepository iAsignaturaRepository;
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/numeroAlumnos")
    public ResponseEntity<?> obtenerCantidadAlumnosEnGrupo(@RequestHeader(value = "curso", required = true) Integer curso,
                                                           @RequestHeader(value = "etapa", required = true) String etapa,
                                                           @RequestHeader(value = "grupo", required = true) Character grupo)
    {
        try
        {
            List<Character> listaGrupos = iCursoEtapaGrupoRepository.findGrupoByCursoAndEtapaChar(curso,etapa);

            // Si no esta ese grupo lanzar excepcion
            if (!listaGrupos.contains(grupo)) {
                log.error("ERROR - Grupo vacio");
                throw new SchoolManagerServerException(404, "ERROR - No se ha encontrado ningún grupo con esa letra");
            }

            // Alumnos en el grupo
            Long numAlumnos = iMatriculaRepository.numeroAlumnosPorGrupo(curso,etapa,grupo);

            // Devolver la lista
            log.info("INFO - Lista de los cursos etapas");
            return ResponseEntity.status(200).body(numAlumnos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Manejo de excepciones personalizadas
            log.error(schoolManagerServerException.getBodyExceptionMessage().toString());

            // Devolver la excepción personalizada con código 1 y el mensaje de error
            return ResponseEntity.status(404).body(schoolManagerServerException);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo cargar el numero de alumnos";
            log.error(msgError, exception);

            // Devolver la excepción personalizada con código 1, el mensaje de error y la
            // excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException);
        }
    }
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/numeroAlumnosEnAsignatura")
    public ResponseEntity<?> obtenerCantidadAlumnosEnGrupoPorAsignatura(@RequestHeader(value = "curso", required = true) Integer curso,
                                                           				@RequestHeader(value = "etapa", required = true) String etapa,
                                                   						@RequestHeader(value = "grupo", required = true) Character grupo,
                                                                        @RequestHeader(value = "asignatura", required = true) String asignatura)
    {
        try
        {
            List<Character> listaGrupos = iCursoEtapaGrupoRepository.findGrupoByCursoAndEtapaChar(curso,etapa);

            // Si no esta ese grupo lanzar excepcion
            if (!listaGrupos.contains(grupo)) {
                log.error("ERROR - Grupo vacio");
                throw new SchoolManagerServerException(404, "ERROR - No se ha encontrado ningún grupo con esa letra");
            }

            // Alumnos en el grupo
            Long numAlumnos = iMatriculaRepository.numeroAlumnosPorGrupoYAsignatura(curso,etapa,grupo,asignatura);

            // Devolver la lista
            log.info("INFO - Lista de los cursos etapas");
            return ResponseEntity.status(200).body(numAlumnos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Manejo de excepciones personalizadas
            log.error(schoolManagerServerException.getBodyExceptionMessage().toString());

            // Devolver la excepción personalizada con código 1 y el mensaje de error
            return ResponseEntity.status(404).body(schoolManagerServerException);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo cargar el numero de alumnos";
            log.error(msgError, exception);

            // Devolver la excepción personalizada con código 1, el mensaje de error y la
            // excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException);
        }
    }
	
	/**
	 * Endpoint para obtener las asignaturas de los cursos etapas.
	 *
	 * Este método recibe los parámetros del curso y la etapa y luego recupera una lista
	 * de asignaturas mostrando su nombre, el nº de horas, el nº de alumnos tanto en general
	 * como en los distintos grupos.
	 *
	 * @param curso 		     - El curso para el que se solicita la lista de alumnos.
	 * @param etapa 			 - La etapa para la cual se solicita la lista de alumnos.
	 * @return ResponseEntity<?> - Respuesta con la lista de asignaturas mapeando un dto para mostrar los datos de las asignaturas.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/asignaturasUnicas")
	public ResponseEntity<?> obtenerAsignaturasUnicas(@RequestHeader("curso") int curso,
										   			  @RequestHeader("etapa") String etapa)
	{
		try
		{
			List<AsignaturaDtoSinGrupo> asignaturas = iAsignaturaRepository.findByCursoAndEtapaDistinct(curso, etapa);

			if(asignaturas.isEmpty())
			{
				String mensajeError = "No existen asignaturas con ese curso y etapa";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}


			return ResponseEntity.status(200).body(asignaturas);
		}
		catch (SchoolManagerServerException schoolManagerServerException)
		{
			return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
		}
		catch (Exception exception)
		{

			String msgError = "Error al acceder a la base de datos";
			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, msgError, exception);

			log.error(msgError, exception);
			return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
		}

	}

}
