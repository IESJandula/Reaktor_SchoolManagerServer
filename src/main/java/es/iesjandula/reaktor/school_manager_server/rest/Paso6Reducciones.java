package es.iesjandula.reaktor.school_manager_server.rest;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.base.utils.HttpClientUtils;
import es.iesjandula.reaktor.school_manager_server.dtos.ProfesorReduccionesDto;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

	@Autowired
	private IProfesorRepository iProfesorRepository;

	@Autowired
	private IProfesorReduccionRepository iProfesorReduccionRepository;



	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/reducciones")
	public ResponseEntity<?> crearReduccion(@RequestHeader(value = "nombre", required = true) String nombre,
											@RequestHeader(value = "horas", required = true) Integer horas,
											@RequestHeader(value = "decideDireccion", required = true) Boolean decideDireccion)
	{
		try 
		{

			IdReduccion idReduccion = new IdReduccion(nombre, horas);
			Optional<Reduccion> reduccion = this.iReduccionRepository.findById(idReduccion);

			if(reduccion.isPresent())
			{
				String mensajeError = "Ya existe una reducción con ese nombre";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			idReduccion.setNombre(nombre);
			idReduccion.setHoras(horas);

			Reduccion nuevaReduccion = new Reduccion();
			nuevaReduccion.setIdReduccion(idReduccion);
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
			IdReduccion idReduccioABorrar = new IdReduccion(nombre, horas);
			Optional<Reduccion> reduccion = this.iReduccionRepository.findById(idReduccioABorrar);

			if(reduccion.isEmpty())
			{
				String mensajeError = "No existe una reducción con ese nombre";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			ProfesorReduccion profesorReduccion = this.iProfesorReduccionRepository.encontrarProfesorReduccion(nombre, horas);

			if(profesorReduccion != null)
			{
				String mensajeError = "No se puede borrar la reducción porque está asignada";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			idReduccioABorrar.setNombre(nombre);
			idReduccioABorrar.setHoras(horas);
			
			Reduccion reduccionABorrar = new Reduccion();
			reduccionABorrar.setIdReduccion(idReduccioABorrar);
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



	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/asignarReducciones")
	private ResponseEntity<?> obtenerReduccionesProfesores ()
	{
		try
		{
			List<ProfesorReduccionesDto> list = this.iProfesorReduccionRepository.encontrarTodosProfesoresReducciones();

			if(list.isEmpty())
			{
				String mensajeError = "No se han encontrado profesores con reducciones asignadas";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			return ResponseEntity.ok(list);

		} catch (SchoolManagerServerException schoolManagerServerException)
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
	@RequestMapping(method = RequestMethod.DELETE, value = "/asignarReducciones")
	private ResponseEntity<?> borrarReduccionesProfesores (@RequestHeader(value = "email") String email,
														   @RequestHeader(value = "reduccion") String nombreReduccion,
														   @RequestHeader(value = "horas") Integer horasReduccion)
	{
		try
		{
			IdReduccion idReduccion = new IdReduccion(nombreReduccion, horasReduccion);
			Optional<Reduccion> reduccion = this.iReduccionRepository.findById(idReduccion);

			if(reduccion.isEmpty())
			{
				String mensajeError = "No existe una reducción con ese nombre y esas horas";

				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			Optional<Profesor> profesor = this.iProfesorRepository.findById(email);

			if(profesor.isEmpty())
			{
				String mensajeError = "No existe un profesor con ese nombre y esos apellidos";

				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			IdProfesorReduccion idProfesorReduccion = new IdProfesorReduccion(profesor.get(), reduccion.get());

			Optional<ProfesorReduccion> optionalProfesorReduccion = this.iProfesorReduccionRepository.findById(idProfesorReduccion);

			if(optionalProfesorReduccion.isEmpty())
			{
				String mensajeError = "No hay asignada una reducción con ese nombre a el profesor que indicas";

				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			ProfesorReduccion profesorReduccion = new ProfesorReduccion();
			profesorReduccion.setIdProfesorReduccion(idProfesorReduccion);

			this.iProfesorReduccionRepository.delete(profesorReduccion);

			return ResponseEntity.ok().build();

		} catch (SchoolManagerServerException schoolManagerServerException)
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
