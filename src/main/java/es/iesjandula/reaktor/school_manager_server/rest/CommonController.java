package es.iesjandula.reaktor.school_manager_server.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioBase;
import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.base.utils.HttpClientUtils;
import es.iesjandula.reaktor.school_manager_server.dtos.ProfesorDto;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.services.ReduccionProfesorService;
import es.iesjandula.reaktor.school_manager_server.services.ValidacionesGlobales;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/common")
public class CommonController
{
    @Autowired
    private ICursoEtapaRepository iCursoEtapaRepository;

    @Autowired
    private IProfesorRepository iProfesorRepository;

    @Autowired
    private IProfesorReduccionRepository iProfesorReduccionRepository;

    @Autowired
    private ReduccionProfesorService reduccionProfesorService;

    @Autowired
    private ValidacionesGlobales validacionesGlobales;

    @Value("${reaktor.http_connection_timeout}")
    private int httpConnectionTimeout;

    @Value("${reaktor.firebase_server_url}")
    private String firebaseServerUrl;

    /**
     * Endpoint para obtener los cursos etapas.
     *
     * Este método obtiene mediante un get todos los cursos etapas guardados en base
     * de datos para despues mostrarlos en el front en un select.
     *
     * @return ResponseEntity<?> - Respuesta con las lista de cursos y etapas.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/cursoEtapa")
    public ResponseEntity<?> obtenerCursoEtapa()
    {
        try
        {
            // Lista usada para guardar los registros de la Tabla CursoEtapa
            List<CursoEtapa> listaCursoEtapa = new ArrayList<>();

            // Asignar los registros de la Tabla CursoEtapa
            listaCursoEtapa = this.iCursoEtapaRepository.findAll();

            // Si la lista esta vacia, lanzar excepcion
            if (listaCursoEtapa.isEmpty())
            {
                String mensajeError = "ERROR - Sin cursos y etapas en la base de datos";

                // Lanzar excepcion y mostrar log con mensaje diferente
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_CURSOS_ETAPAS_ENCONTRADOS, mensajeError);
            }

            // Devolver la lista
            log.info("INFO - Lista de los cursos etapas");
            return ResponseEntity.status(200).body(listaCursoEtapa);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo cargar la lista";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/profesores")
    private ResponseEntity<?> obtenerProfesores(@AuthenticationPrincipal DtoUsuarioExtended usuario)
    {
        try
        {
            List<Profesor> list = this.buscarProfesorEnFirebase(usuario.getJwt());

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
    @RequestMapping(method = RequestMethod.POST, value = "/asignarReducciones")
    private ResponseEntity<?> asignarReduccion(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                               @RequestHeader(value = "email") String email,
                                               @RequestHeader(value = "reduccion") String nombreReduccion,
                                               @RequestHeader(value = "horas") Integer horasReduccion)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }

            Reduccion reduccion = this.reduccionProfesorService.validadarYObtenerReduccion(nombreReduccion, horasReduccion);

            Profesor profesor = this.reduccionProfesorService.validadarYObtenerProfesor(email);

            IdProfesorReduccion idProfesorReduccion = new IdProfesorReduccion(profesor, reduccion);

            Optional<ProfesorReduccion> optionalProfesorReduccion = this.iProfesorReduccionRepository.findById(idProfesorReduccion);

            if(optionalProfesorReduccion.isPresent())
            {
                String mensajeError = "Ya has asignado esa reducción a ese profesor";

                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            ProfesorReduccion profesorReduccion = new ProfesorReduccion();
            profesorReduccion.setIdProfesorReduccion(idProfesorReduccion);

            this.iProfesorReduccionRepository.saveAndFlush(profesorReduccion);

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
