package es.iesjandula.matriculas_horarios_server.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.matriculas_horarios_server.dtos.AsignaturaDto;
import es.iesjandula.matriculas_horarios_server.models.Asignatura;
import es.iesjandula.matriculas_horarios_server.models.Bloque;
import es.iesjandula.matriculas_horarios_server.models.CursoEtapa;
import es.iesjandula.matriculas_horarios_server.models.ids.IdAsignatura;
import es.iesjandula.matriculas_horarios_server.parsers.IParseoDatosBrutos;
import es.iesjandula.matriculas_horarios_server.repositories.IAsignaturaRepository;
import es.iesjandula.matriculas_horarios_server.repositories.IBloqueRepository;
import es.iesjandula.matriculas_horarios_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.matriculas_horarios_server.repositories.ICursoEtapaRepository;
import es.iesjandula.matriculas_horarios_server.repositories.IDatosBrutoAlumnoMatriculaGrupoRepository;
import es.iesjandula.matriculas_horarios_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.matriculas_horarios_server.utils.MatriculasHorariosServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(value = "/direccionVentana3")
public class DireccionControllerVentana3 
{	
	@Autowired
	ICursoEtapaRepository iCursoEtapaRepository;
	
	@Autowired
	ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;
	
	@Autowired
	IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;
	
	@Autowired
	IDatosBrutoAlumnoMatriculaGrupoRepository iDatosBrutoAlumnoMatriculaGrupoRepository;
	
	@Autowired
	IParseoDatosBrutos iParseoDatosBrutos;
	
	@Autowired
	IAsignaturaRepository iAsignaturaRepository ;
	
	@Autowired
	IBloqueRepository iBloqueRepository ;
	
    /**
     * Endpoint para obtener los cursos etapas.
     * 
     * Este método obtiene mediante un get todos los cursos etapas
     * guardados en base de datos para despues mostrarlos en el
     * front en un select.
     * 
     * @return ResponseEntity<?> - Respuesta con las lista de cursos y etapas.
     */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
   @RequestMapping(method = RequestMethod.GET, value = "/etapaCursos")
   public ResponseEntity<?> obtenerCursosEtapas()
   {
   	try 
   	{
   		List<CursoEtapa> cursos = this.iCursoEtapaRepository.findAll() ;
       	
       	log.info("INFO - Lista de los cursos etapas") ;
       	return ResponseEntity.status(200).body(cursos) ;
	} 
   	catch (Exception exception) 
   	{
		String msgError = "ERROR - No se pudo obtener la lista de cursos etapas" ;
		log.error(msgError, exception) ;
		
		MatriculasHorariosServerException matriculasHorariosServerException = new MatriculasHorariosServerException(1, msgError, exception) ;
		return ResponseEntity.status(500).body(matriculasHorariosServerException.getBodyExceptionMessage()) ;
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
   @RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
   public ResponseEntity<?> obtenerAsignatura(@RequestParam("curso") int curso, @RequestParam("etapa") String etapa)
   {
	   try 
	   {
			List<Asignatura> asignaturas = iAsignaturaRepository.findByCursoAndEtapa(curso, etapa) ;
			
			// Mapear a DTO y calcular el número de alumnos matriculados
			List<AsignaturaDto> asignaturasDto = asignaturas.stream().map(asignatura -> 
			{
				AsignaturaDto dto = new AsignaturaDto() ;
				dto.setNombre(asignatura.getId().getNombre()) ;
				dto.setGrupo(asignatura.getId().getGrupo()) ;
				dto.setEtapa(asignatura.getId().getEtapa()) ;
				dto.setCurso(asignatura.getId().getCurso()) ;
				// Numero total de alumnos en la asignatura
				dto.setNumeroDeAlumnos(asignatura.getMatriculas().size()) ;
				
				// Calcular el número de alumnos en el grupo específico
				Map<String, Integer> numeroAlumnosEnGrupo = asignatura.getMatriculas().stream()
						.collect(Collectors.groupingBy(
								matricula -> matricula.getAsignatura().getId().getGrupo(),
								Collectors.summingInt(m -> 1)
						 )) ;
				dto.setNumeroAlumnosEnGrupo(numeroAlumnosEnGrupo) ;
				
				dto.setBloqueId(asignatura.getBloqueId() != null ? asignatura.getBloqueId().getId() : null) ;
				return dto ;
			}).collect(Collectors.toList()) ;
			
			return ResponseEntity.status(200).body(asignaturasDto) ;
		} 
   	catch (Exception exception) 
   	{
			String msgError = "ERROR - No se pudo obtener la lista de asignaturas" ;
			log.error(msgError, exception) ;
			MatriculasHorariosServerException matriculasHorariosServerException = new MatriculasHorariosServerException(1, msgError, exception) ;
			return ResponseEntity.status(500).body(matriculasHorariosServerException.getBodyExceptionMessage()) ;
		}
   	
   }
   
   /**
    * Endpoint para crear un bloque y asignarlo a un conjunto de asignaturas
    * 
    * Este método recibirá un JSON con el curso, la etapa y una lista de nombres de asignaturas
    * para luego crear un bloque con un id autogenerado y asignar ese mismo id a las asignaturas
    * que se le pasen al endpoint
    * 
    * @param  idAsignatura 	 - JSON que contiene el curso, la etapa y el nombre de la asignatura.
    * @return ResponseEntity<?> - Respuesta del endpoint que no devolverá nada
    */
   @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
   @RequestMapping(method = RequestMethod.POST, value = "/bloques")
   public ResponseEntity<?> crearBloques
   (
   		@RequestParam("curso") int curso,
   		@RequestParam("etapa") String etapa,
   		@RequestParam("asignaturas") List<String> asignaturas
   )
   {	
   	try
   	{	
   		
   		if (asignaturas == null || asignaturas.size() < 2)
   		{
               String msgError = "ERROR - Hay que seleccionar al menos 2 asignaturas";
               log.error(msgError);
               throw new MatriculasHorariosServerException(100, msgError);
   		}
   		
   		Bloque bloque = new Bloque();
   		
   		for (String asignaturaString : asignaturas)
   		{
   			Optional<Asignatura> optionalAsignatura = this.iAsignaturaRepository.findAsignaturasByCursoEtapaAndNombre(curso, etapa, asignaturaString) ;
   			
   			if(!optionalAsignatura.isPresent())
   			{
   				String msgError = "ERROR - La asignatura no fue encontrada";
   				log.error(msgError);
   				throw new MatriculasHorariosServerException(101, msgError);
   			}
   			
   			if(optionalAsignatura.get().getBloqueId() != null)
   			{
   				String msgError = "ERROR - Una de las asignaturas ya tiene un bloque asignado";
   				log.error(msgError);
   				throw new MatriculasHorariosServerException(102, msgError);
   			}
   			
   			this.iBloqueRepository.save(bloque);
   			
   			optionalAsignatura.get().setBloqueId(bloque);
   			
   	   		iAsignaturaRepository.saveAndFlush(optionalAsignatura.get());
   		}
 		
   		return ResponseEntity.status(201).body(bloque.getId());
   		
   	}
   	catch (MatriculasHorariosServerException matriculasHorariosServerException) {
           return ResponseEntity.status(400).body(matriculasHorariosServerException.getBodyExceptionMessage());
   	}
   	catch (Exception exception)
   	{
			String msgError = "ERROR - No se pudo crear el bloque" ;
			log.error(msgError, exception) ;
			MatriculasHorariosServerException matriculasHorariosServerException = new MatriculasHorariosServerException(1, msgError, exception) ;
			return ResponseEntity.status(500).body(matriculasHorariosServerException.getBodyExceptionMessage()) ;
   	}
   	
   }
   
   /**
    * Endpoint para borrar un bloque y sus respectiva asignatura.
    * 
    * Este método recibirá un JSON con el curso, la etapa y el nombre de la asignatura
    * para luego settear a null el campo bloque_id y eliminar la asignatura del bloque de 
    * modo que dicho bloque quedará eliminado
    * 
    * @param  idAsignatura 	 - JSON que contiene el curso, la etapa y el nombre de la asignatura.
    * @return ResponseEntity<?> - Respuesta del endpoint que no devolverá nada
    */
   @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
   @RequestMapping(method = RequestMethod.DELETE, value = "/eliminarBloque")
   public ResponseEntity<?> eliminarBloque(@RequestBody IdAsignatura idAsignatura)
   {
   	try 
   	{
   		
   		// Buscamos el id de la asignatura
			Optional<Asignatura> asignaturaOpt = iAsignaturaRepository.findById(idAsignatura) ;
			
			if (asignaturaOpt.isPresent())
			{
				Asignatura asignatura = asignaturaOpt.get() ;
				
				// Desasociar la asignatura del bloque
				Bloque bloque = asignatura.getBloqueId() ;
				asignatura.setBloqueId(null) ;
				this.iAsignaturaRepository.saveAndFlush(asignatura) ;
				
				if (bloque != null && bloque.getAsignaturas().isEmpty())
				{
					iBloqueRepository.delete(bloque) ;
				}
			}
			
			log.info("INFO - Bloque eliminado con éxito") ;
			return ResponseEntity.status(200).build() ;
			
		} catch (Exception exception) 
   	{
			String msgError = "ERROR - Error en el servidor" ;
			log.error(msgError, exception) ;
			MatriculasHorariosServerException matriculasHorariosServerException = new MatriculasHorariosServerException(1, msgError, exception) ;
			return ResponseEntity.status(500).body(matriculasHorariosServerException.getBodyExceptionMessage()) ;
		} 
   	
   }
}