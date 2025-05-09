package es.iesjandula.reaktor.school_manager_server.rest;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.base.utils.HttpClientUtils;
import es.iesjandula.reaktor.school_manager_server.dtos.ProfesorDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ProfesorReduccionesDto;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.services.ReduccionProfesorService;
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

	@Autowired
	private ReduccionProfesorService reduccionProfesorService;

	@Value("${reaktor.http_connection_timeout}")
	private int httpConnectionTimeout;

	@Value("${reaktor.firebase_server_url}")
	private String firebaseServerUrl;
	
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
	@RequestMapping(method = RequestMethod.GET, value = "/profesores")
	private ResponseEntity<?> obtenerProfesores(@AuthenticationPrincipal DtoUsuarioExtended usuario)
	{
		try
		{
			List<Profesor> list = this.buscarProfesor(usuario);

			if(list.isEmpty())
			{
				String mensajeError = "No se han encontrado profesores";
				log.error(mensajeError);
				throw new SchoolManagerServerException(1, mensajeError);
			}

			List<ProfesorDto> listaProfesorDto = list.stream().map(profesor ->
					new ProfesorDto(
							profesor.getNombre(),
							profesor.getApellidos(),
							profesor.getEmail()
					)).collect(Collectors.toList());

			return ResponseEntity.ok(listaProfesorDto);

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

	/**
	 * @param jwtAdmin JWT del usuario admin
	 * @param email    email del profesor que va a realizar la reserva
	 * @return el profesor encontrado enfirebase
	 * @throws SchoolManagerServerException con un error
	 */
	private List<Profesor> buscarProfesorEnFirebase(String jwtAdmin) throws SchoolManagerServerException
	{
		List<Profesor> profesores = new ArrayList<>();

		// Creamos un HTTP Client con Timeout
		CloseableHttpClient closeableHttpClient = HttpClientUtils.crearHttpClientConTimeout(this.httpConnectionTimeout);

		CloseableHttpResponse closeableHttpResponse = null;

		try
		{
			HttpGet httpGet = new HttpGet(this.firebaseServerUrl + "/firebase/queries/users");

			// Añadimos el jwt y el email a la llamada
			httpGet.addHeader("Authorization", "Bearer " + jwtAdmin);
			httpGet.addHeader("Accept", "application/json");

			// Hacemos la peticion
			closeableHttpResponse = closeableHttpClient.execute(httpGet);

			if (closeableHttpResponse.getStatusLine().getStatusCode() == 403)
			{
				String mensajeError = "Acceso denegado (403) al obtener lista de profesores";

				log.error(mensajeError);
				throw new SchoolManagerServerException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError);
			}

			// Comprobamos si viene la cabecera. En caso afirmativo, es porque trae un
			// profesor
			if (closeableHttpResponse.getEntity() == null)
			{
				String mensajeError = "No se han encontrado profesores en la BBDD Global";

				log.error(mensajeError);
				throw new SchoolManagerServerException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError);
			}

			// Convertimos la respuesta en un objeto DtoInfoUsuario
			ObjectMapper objectMapper = new ObjectMapper();

			// Obtenemos la respuesta de Firebase
			List<DtoUsuarioBase> listDtoUsuarioBase = objectMapper
					.readValue(closeableHttpResponse.getEntity().getContent(),
							new TypeReference<List<DtoUsuarioBase>>() {});

			// Creamos una instancia de profesor con la respuesta de Firebase
			for(DtoUsuarioBase dtoUsuarioBase: listDtoUsuarioBase)
			{
				Departamento departamento = new Departamento();
				departamento.setNombre(dtoUsuarioBase.getDepartamento());

				Profesor profesor = new Profesor();
				profesor.setNombre(dtoUsuarioBase.getNombre());
				profesor.setApellidos(dtoUsuarioBase.getApellidos());
				profesor.setEmail(dtoUsuarioBase.getEmail());
				profesor.setDepartamento(departamento);
				profesores.add(profesor);
			}

			// Almacenamos los profesores en nuestra BBDD
			this.iProfesorRepository.saveAllAndFlush(profesores);
		}
		catch (SocketTimeoutException socketTimeoutException)
		{
			String errorString = "SocketTimeoutException de lectura o escritura al comunicarse con el servidor (búsqueda del profesor asociado a la reserva)";

			log.error(errorString, socketTimeoutException);
			throw new SchoolManagerServerException(Constants.ERROR_CONEXION_FIREBASE, errorString, socketTimeoutException);
		}
		catch (ConnectTimeoutException connectTimeoutException)
		{
			String errorString = "ConnectTimeoutException al intentar conectar con el servidor (búsqueda del profesor asociado a la reserva)";

			log.error(errorString, connectTimeoutException);
			throw new SchoolManagerServerException(Constants.TIMEOUT_CONEXION_FIREBASE, errorString, connectTimeoutException);
		}
		catch (IOException ioException)
		{
			String errorString = "IOException mientras se buscaba el profesor asociado a la reserva";

			log.error(errorString, ioException);
			throw new SchoolManagerServerException(Constants.IO_EXCEPTION_FIREBASE, errorString, ioException);
		}
		finally
		{
			// Cierre de flujos
			this.buscarProfesorEnFirebaseCierreFlujos(closeableHttpResponse);
		}

		return profesores;
	}

	/**
	 * @param usuario usuario
	 * @param email   email
	 * @return el profesor encontrado
	 * @throws SchoolManagerServerException con un error
	 */
	private List<Profesor> buscarProfesor(DtoUsuarioExtended usuario) throws SchoolManagerServerException
	{
		// Buscamos el profesor en Firebase
		return  this.buscarProfesorEnFirebase(usuario.getJwt());
	}

	/**
	 * @param closeableHttpResponse closeable HTTP response
	 * @throws PrinterClientException printer client exception
	 */
	private void buscarProfesorEnFirebaseCierreFlujos(CloseableHttpResponse closeableHttpResponse) throws SchoolManagerServerException
	{
		if (closeableHttpResponse != null)
		{
			try
			{
				closeableHttpResponse.close();
			}
			catch (IOException ioException)
			{
				String errorString = "IOException mientras se cerraba el closeableHttpResponse en el método que busca al profesor de la reserva";

				log.error(errorString, ioException);
				throw new SchoolManagerServerException(Constants.IO_EXCEPTION_FIREBASE, errorString, ioException);
			}
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
			Reduccion reduccion = this.reduccionProfesorService.validadarYObtenerReduccion(nombreReduccion, horasReduccion);

			Profesor profesor = this.reduccionProfesorService.validadarYObtenerProfesor(email);

			IdProfesorReduccion idProfesorReduccion = new IdProfesorReduccion(profesor, reduccion);

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
