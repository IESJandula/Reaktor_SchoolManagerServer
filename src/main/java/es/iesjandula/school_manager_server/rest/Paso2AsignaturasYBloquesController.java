package es.iesjandula.school_manager_server.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.school_manager_server.dtos.AsignaturaDto;
import es.iesjandula.school_manager_server.dtos.AsignaturaHorasDto;
import es.iesjandula.school_manager_server.models.Asignatura;
import es.iesjandula.school_manager_server.models.Bloque;
import es.iesjandula.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.school_manager_server.repositories.IBloqueRepository;
import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/asignaturasYBloques")
public class Paso2AsignaturasYBloquesController 
{
	@Autowired
	private IAsignaturaRepository iAsignaturaRepository;

	@Autowired
	private IBloqueRepository iBloqueRepository;

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
	   public ResponseEntity<?> obtenerAsignatura(@RequestHeader("curso") int curso, 
			   									  @RequestHeader("etapa") String etapa)
	   {
		   try 
		   {
				List<AsignaturaDto> asignaturas = iAsignaturaRepository.findByCursoAndEtapa(curso, etapa);
				
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
	   public ResponseEntity<?> crearBloques(@RequestParam("curso") int curso,
											 @RequestParam("etapa") String etapa,
											 @RequestParam("asignaturas") List<String> asignaturas)
	   {	
	   	try
	   	{	
	   		
	   		if (asignaturas == null || asignaturas.size() < 2)
	   		{
	               String msgError = "ERROR - Hay que seleccionar al menos 2 asignaturas";
	               log.error(msgError);
	               throw new SchoolManagerServerException(100, msgError);
	   		}
	   		
	   		Bloque bloque = new Bloque();
	   		
	   		for (String asignaturaString : asignaturas)
	   		{
	   			List<Optional<Asignatura>> optionalAsignatura = this.iAsignaturaRepository.findAsignaturasByCursoEtapaAndNombre(curso, etapa, asignaturaString);
	   			
	   			for(Optional<Asignatura> asignatura : optionalAsignatura) {
	   				
	   				if(!asignatura.isPresent())
	   				{
	   					String msgError = "ERROR - La asignatura no fue encontrada";
	   					log.error(msgError);
	   					throw new SchoolManagerServerException(101, msgError);
	   				}
	   				
	   				if(asignatura.get().getBloqueId() != null)
	   				{
	   					String msgError = "ERROR - Una de las asignaturas ya tiene un bloque asignado";
	   					log.error(msgError);
	   					throw new SchoolManagerServerException(102, msgError);
	   				}
	   				
	   				this.iBloqueRepository.save(bloque);
	   				
	   				asignatura.get().setBloqueId(bloque);
	   				
	   				iAsignaturaRepository.saveAndFlush(asignatura.get());
	   			}
	   			
	   		}
	 		
	   		return ResponseEntity.status(201).body(bloque.getId());
	   		
	   	}
	   	catch (SchoolManagerServerException schoolManagerServerException) 
	   	{
	           return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
	   	}
	   	catch (Exception exception)
	   	{
			String msgError = "ERROR - No se pudo crear el bloque";
			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, msgError, exception);
			
			log.error(msgError, exception);
			return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
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
	   @RequestMapping(method = RequestMethod.DELETE, value = "/bloques")
	   public ResponseEntity<?> eliminarBloque(@RequestBody IdAsignatura idAsignatura)
	   {
	   	try 
	   	{
	   		
	   		// Buscamos el id de la asignatura
			Optional<Asignatura> asignaturaOpt = iAsignaturaRepository.findById(idAsignatura);
			
			if (!asignaturaOpt.isPresent())
			{
				String mensajeError = "No se han encontrado asignaturas con esos parametros";
				log.error(mensajeError);
				
				throw new SchoolManagerServerException(1, mensajeError);
			}
			Asignatura asignatura = asignaturaOpt.get();
			
			// Desasociar la asignatura del bloque
			Bloque bloque = asignatura.getBloqueId();
			asignatura.setBloqueId(null) ;
			
			this.iAsignaturaRepository.saveAndFlush(asignatura);
			
			if (bloque != null && bloque.getAsignaturas().isEmpty())
			{
				iBloqueRepository.delete(bloque);
			}
			
			log.info("INFO - Bloque eliminado con éxito");
			return ResponseEntity.status(200).build();
				
		}
	   	catch (SchoolManagerServerException schoolManagerServerException) 
	    {
		    return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
	    }
	   	catch (Exception exception) 
	   	{
			String msgError = "ERROR - Error en el servidor";
			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, msgError, exception);

			log.error(msgError, exception);
			return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
		} 
	   	
	   }
	   
	   @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	   @RequestMapping(method = RequestMethod.GET, value = "/horas")
	   public ResponseEntity<?> mostrarHoras(@RequestHeader("curso") Integer curso, 
			   @RequestHeader("etapa") String etapa)
	   {
		   
		   try
		   {
			   List<AsignaturaHorasDto> listAsignatuasHoras = this.iAsignaturaRepository.findNombreAndHorasByCursoEtapaAndNombres(curso, etapa);
			   
			   if(listAsignatuasHoras.isEmpty())
			   {
				   String mensajeError = "No se ha encontrado una asignatura con ese nombre y para ese curso y etapa";
				   log.error(mensajeError);
				   throw new SchoolManagerServerException(1, mensajeError);
			   }
			   
			   return ResponseEntity.ok(listAsignatuasHoras);
		   }
		   catch (SchoolManagerServerException schoolManagerServerException) 
		   {
			   return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
		   }
		   catch (Exception exception) 
		   {
			   String msgError = "ERROR - Error en el servidor";
			   SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, msgError, exception);
			   
			   log.error(msgError, exception);
			   return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
		   } 
	   }

	   
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
			   
			   if(listAsignatura == null)
			   {
				   String mensajeError = "No se ha encontrado una asignatura con ese nombre y para ese curso y etapa";
				   log.error(mensajeError);
				   throw new SchoolManagerServerException(1, mensajeError);
			   }

			   for(Asignatura asignatura : listAsignatura) {
				   
				   asignatura.setHoras(horas);
				   this.iAsignaturaRepository.saveAndFlush(asignatura);
			   }
			   
			   
			   return ResponseEntity.ok().build();
		   }
		   catch (SchoolManagerServerException schoolManagerServerException) 
		   {
			   return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
		   }
		   catch (Exception exception) 
	   	   {
			   String msgError = "ERROR - Error en el servidor";
			   SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, msgError, exception);

			   log.error(msgError, exception);
			   return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
		   } 
	   }
	   
	   
	}