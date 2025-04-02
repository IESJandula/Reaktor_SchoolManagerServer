package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/schoolManager/crearReducciones")
@Slf4j
public class Paso6Reducciones 
{
	
	@Autowired
	private IReduccionRepository iReduccionRepository;
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/reducciones")
	public ResponseEntity<?> crearReduccion(@RequestHeader(value = "nombre", required = true) String nombre,
											@RequestHeader(value = "horas", required = true) Integer horas,
											@RequestHeader(value = "decideDireccion", required = true) Boolean decideDireccion)
	{
		try 
		{
			
			
			
			Optional<Reduccion> reduccion = this.iReduccionRepository.findById(nombre);
			
			if(reduccion.isPresent()) 
			{
				String mensajeError = "Ya existe una reducción con ese nombre";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}
			
			Reduccion nuevaReduccion = new Reduccion();
			nuevaReduccion.setNombre(nombre);
			nuevaReduccion.setHoras(horas);
			nuevaReduccion.setDecideDireccion(decideDireccion);
			
			this.iReduccionRepository.saveAndFlush(nuevaReduccion);
			
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
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/reducciones")
	public ResponseEntity<?> mostrarReduccion()
	{
		try 
		{
			List<ReduccionDto> listReduccions = this.iReduccionRepository.encontrarTodasReducciones();
			
			if(listReduccions.isEmpty()) {
				
				String mensajeError = "No se han encontrado ninguna reducción";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}
			
			return ResponseEntity.ok(listReduccions);
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

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/reducciones")
	public ResponseEntity<?> borrarReduccion(@RequestHeader(value = "nombre", required = true) String nombre,
											 @RequestHeader(value = "horas", required = true) Integer horas,
											 @RequestHeader(value = "decideDireccion", required = true) Boolean decideDireccion)
	{
		try 
		{
			Optional<Reduccion> reduccion = this.iReduccionRepository.findById(nombre);
			
			if(!reduccion.isPresent()) 
			{
				String mensajeError = "No existe una reducción con ese nombre";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}
			
			Reduccion reduccionABorrar = new Reduccion();
			reduccionABorrar.setNombre(nombre);
			reduccionABorrar.setHoras(horas);
			reduccionABorrar.setDecideDireccion(decideDireccion);
			
			this.iReduccionRepository.delete(reduccionABorrar);
			
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
