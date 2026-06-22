package es.iesjandula.reaktor.school_manager_server.rest.manager;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDepartamento;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.school_manager_server.dtos.CargaCsvResultDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.services.manager.ParseoCsvConfiguracionBasicaService;
import es.iesjandula.reaktor.school_manager_server.services.manager.ReduccionProfesorService;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/schoolManager/crearReducciones")
@Slf4j
public class Paso6ReduccionesController
{
    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IReduccionRepository iReduccionRepository;

    @Autowired
    private IProfesorRepository iProfesorRepository;

    @Autowired
    private IProfesorReduccionRepository iProfesorReduccionRepository;

    @Autowired
    private IDepartamentoRepository iDepartamentoRepository;

    @Autowired
    private es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver cursoAcademicoResolver;

    @Autowired
    private ReduccionProfesorService reduccionProfesorService;

    @Autowired
    private ParseoCsvConfiguracionBasicaService parseoCsvConfiguracionBasicaService;

    @Value("${reaktor.http_connection_timeout}")
    private int httpConnectionTimeout;

    @Value("${reaktor.firebase_server_url}")
    private String firebaseServerUrl;

    /**
     * Crea un nuevo recurso de reducción con los parámetros especificados.
     * <p>
     * Si ya existe una reducción con los mismos parámetros, se lanza una excepción.
     *
     * @param nombre           el nombre de la reducción, que actúa como identificador.
     * @param horas            la cantidad de horas asociadas a la reducción.
     * @param decideDireccion  indica si la decisión sobre esta reducción la toma la dirección.
     * @return una {@link ResponseEntity} con:
     * - 201 (CREATED) si la reducción se creó correctamente.
     * - 409 (CONFLICT) si ya existe una reducción con los mismos parámetros.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/reducciones")
    public ResponseEntity<?> crearReduccion(@RequestHeader(value = "nombre", required = true) String nombre,
                                            @RequestHeader(value = "horas", required = true) Integer horas,
                                            @RequestHeader(value = "decideDireccion", required = true) Boolean decideDireccion,
                                            @RequestHeader(value = "curso", required = false) Integer curso,
                                            @RequestHeader(value = "etapa", required = false) String etapa,
                                            @RequestHeader(value = "grupo", required = false) String grupo)
    {
        try
        {
            // Las reducciones están scoped por el curso académico activo (seleccionado = true)
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            IdReduccion idReduccion = new IdReduccion(cursoAcademico, nombre, horas);
            Optional<Reduccion> reduccion = this.iReduccionRepository.findById(idReduccion);

            if (reduccion.isPresent())
            {
                String mensajeError = "Ya existe una reducción con esos parámetros";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.REDUCCION_EXISTENTE, mensajeError);
            }

            Reduccion nuevaReduccion = new Reduccion();
            nuevaReduccion.setIdReduccion(idReduccion);
            nuevaReduccion.setDecideDireccion(decideDireccion);
            
            // Si el curso, etapa y grupo no son nulos, se busca la relación con el cursoEtapaGrupo
            if (curso != null && etapa != null && grupo != null)
            {
                CursoEtapaGrupo cursoEtapaGrupo = this.iCursoEtapaGrupoRepository.buscarCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo);

                if (cursoEtapaGrupo == null)
                {
                    String mensajeError = "No existe un curso etapa grupo con el curso " + curso + " etapa " + etapa + " y grupo " + grupo;
                    
                    log.error(mensajeError);
                    throw new SchoolManagerServerException(Constants.CURSO_ETAPA_GRUPO_NO_ENCONTRADO, mensajeError);
                }

                nuevaReduccion.setCursoEtapaGrupo(cursoEtapaGrupo);
            }

            this.iReduccionRepository.saveAndFlush(nuevaReduccion);

            return ResponseEntity.status(HttpStatus.CREATED).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar crear la reducción.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera la lista de reducciones disponibles en el sistema.
     * <p>
     * Cada reducción representa un conjunto de horas que pueden ser asignadas o decididas
     * por la dirección según las reglas establecidas.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@link ReduccionDto} si la operación es exitosa.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/reducciones")
    public ResponseEntity<?> cargarReducciones()
    {
        try
        {
            // Solo las reducciones del curso académico activo (seleccionado = true)
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            List<ReduccionDto> listReduccions = this.iReduccionRepository.encontrarReduccionesPorCursoAcademico(cursoAcademico);

            return ResponseEntity.ok().body(listReduccions);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar al obtener la lista de reducciones.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Carga de forma masiva reducciones del tipo "NO tutorías" desde un fichero CSV de 2 columnas (la primera fila
     * es la cabecera y se ignora como dato; cada fila restante es {@code nombre,horas}). Reutiliza el parser robusto
     * y la persistencia idempotente del servicio de carga CSV. El curso académico activo se resuelve internamente
     * en el servicio (seleccionado = true); el cliente no lo envía.
     * <p>
     * Como la PK de la reducción es {@code (nombre, horas)}, las filas con el mismo nombre y horas distintas se
     * crean como reducciones independientes, y las filas exactamente repetidas se omiten (idempotencia).
     *
     * @param archivoCsv el fichero CSV con nombre y horas por fila.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y {@link CargaCsvResultDto} si el procesamiento finaliza correctamente.
     * - 400 (Bad Request) si el archivo está vacío, no es .csv o no contiene filas de datos.
     * - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/reducciones/noTutorias/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> cargarReduccionesNoTutoriasDesdeCsv(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv)
    {
        try
        {
            CargaCsvResultDto resultado = this.parseoCsvConfiguracionBasicaService.cargarReduccionesNoTutoriasDesdeCsv(archivoCsv);

            return ResponseEntity.ok().body(resultado);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron cargar las reducciones (no tutorías) desde el CSV";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Carga de forma masiva reducciones del tipo "TUTORÍAS" desde un fichero CSV de 3 columnas (la primera fila es
     * la cabecera y se ignora como dato; cada fila restante es {@code curso,etapa,horas}). El servicio sintetiza un
     * nombre estable {@code "Tutoría <curso>º <etapa>"} para encajar en la PK existente {@code (nombre, horas)} sin
     * migración de esquema. El curso académico activo se resuelve internamente en el servicio (seleccionado = true).
     *
     * @param archivoCsv el fichero CSV con curso, etapa y horas por fila.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y {@link CargaCsvResultDto} si el procesamiento finaliza correctamente.
     * - 400 (Bad Request) si el archivo está vacío, no es .csv o no contiene filas de datos.
     * - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/reducciones/tutorias/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> cargarReduccionesTutoriasDesdeCsv(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv)
    {
        try
        {
            CargaCsvResultDto resultado = this.parseoCsvConfiguracionBasicaService.cargarReduccionesTutoriasDesdeCsv(archivoCsv);

            return ResponseEntity.ok().body(resultado);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron cargar las reducciones (tutorías) desde el CSV";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina una entidad de tipo "Reducción" según los parámetros proporcionados.
     * <p>
     * El método verifica que la reducción exista y que no esté asignada a ningún profesor antes de proceder con la eliminación.
     *
     * @param nombre           el nombre de la reducción a eliminar.
     * @param horas            la cantidad de horas asociadas a la reducción.
     * @param decideDireccion  indica si la decisión sobre esta reducción la toma la dirección.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la reducción se eliminó correctamente.
     * - 404 (NOT_FOUND) si no existe la reducción especificada.
     * - 409 (CONFLICT) si la reducción está asignada a un profesor.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/reducciones")
    public ResponseEntity<?> borrarReduccion(@RequestHeader(value = "nombre", required = true) String nombre,
                                             @RequestHeader(value = "horas", required = true) Integer horas,
                                             @RequestHeader(value = "decideDireccion", required = true) Boolean decideDireccion)
    {
        try
        {
            // La reducción a borrar pertenece al curso académico activo (seleccionado = true)
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            IdReduccion idReduccioABorrar = new IdReduccion(cursoAcademico, nombre, horas);
            Optional<Reduccion> reduccionABorrar = this.iReduccionRepository.findById(idReduccioABorrar);

            if (reduccionABorrar.isEmpty())
            {
                String mensajeError = "No existe una reducción con el nombre " + nombre + " y horas " + horas;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.REDUCCION_NO_ENCONTRADA, mensajeError);
            }

            ProfesorReduccion profesorReduccion = this.iProfesorReduccionRepository.encontrarProfesorReduccion(nombre, horas);

            if (profesorReduccion != null)
            {
                Profesor profesor = this.iProfesorRepository.findByCursoAcademicoAndEmail(cursoAcademico, profesorReduccion.getIdProfesorReduccion().getProfesor().getEmail());
                String mensajeError = "No se puede borrar la reducción porque está asignada al profesor " + profesor;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.REDUCCION_ASIGNADA, mensajeError);
            }

            this.iReduccionRepository.delete(reduccionABorrar.get());

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.REDUCCION_NO_ENCONTRADA)
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
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar al intentar borrar la reducción.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Borra TODAS las reducciones del curso académico activo de un tipo (tutorías o no tutorías).
     * <p>
     * El tipo se distingue por el nombre: las tutorías son las que empiezan por {@link Constants#PREFIJO_REDUCCION_TUTORIA}
     * (incluye tanto las plantilla a nivel curso/etapa como las materializadas por grupo); el resto son no tutorías.
     *
     * @param tutoria {@code true} para borrar todas las tutorías; {@code false} para borrar todas las no tutorías.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si el borrado se realizó correctamente.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/reducciones/borrarTodos")
    public ResponseEntity<?> borrarTodasReducciones(@RequestParam(value = "tutoria", required = true) Boolean tutoria)
    {
        try
        {
            // Solo se borran las reducciones del curso académico activo (seleccionado = true)
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            String patronTutoria = Constants.PREFIJO_REDUCCION_TUTORIA + "%";

            if (Boolean.TRUE.equals(tutoria))
            {
                this.iReduccionRepository.borrarPorCursoAcademicoYNombreLike(cursoAcademico, patronTutoria);
            }
            else
            {
                this.iReduccionRepository.borrarPorCursoAcademicoYNombreNotLike(cursoAcademico, patronTutoria);
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - Se produjo un error inesperado al borrar todas las reducciones.";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera la lista de profesores disponibles en la base de datos en forma de objetos DTO.
     * <p>
     * Si no se encuentran profesores, se lanza una excepción con el mensaje adecuado.
     * Este método requiere que el usuario tenga el rol de Dirección para su acceso.
     *
     * @param usuario el objeto {@link DtoUsuarioExtended} que representa al usuario autenticado que realiza la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@link ProfesorDto} si la operación es exitosa.
     * - 404 (NOT_FOUND) si no se encuentran profesores.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/profesores")
    private ResponseEntity<?> obtenerProfesores(@AuthenticationPrincipal DtoUsuarioExtended usuario)
    {
        try
        {
            List<Profesor> list = this.buscarProfesoresEnFirebase(usuario.getJwt());

            if (list.isEmpty())
            {
                String mensajeError = "No se han encontrado profesores en base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_PROFESORES_ENCONTRADOS, mensajeError);
            }

            List<ProfesorDto> listaProfesorDto = list.stream().map(profesor ->
                    new ProfesorDto(
                            profesor.getNombre(),
                            profesor.getApellidos(),
                            profesor.getEmail()
                    )).collect(Collectors.toList());

            return ResponseEntity.ok().body(listaProfesorDto);

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Si el fallo es de CONECTIVIDAD con el servicio externo de profesores (Firebase no levantado, conexión
            // rechazada o timeout), NO rompemos la vista de Reducciones: devolvemos 200 con lista vacía para que el
            // resto de la pantalla (reducciones y asignaciones) siga funcionando. El frontend mostrará un aviso suave
            // (no alarmante) indicando que el listado de profesores no está disponible temporalmente.
            if (this.esErrorConexionFirebase(schoolManagerServerException.getCode()))
            {
                log.warn("AVISO - El servicio de profesores (Firebase) no está disponible; se devuelve una lista de profesores vacía para no romper la vista de Reducciones. Código: {}", schoolManagerServerException.getCode());

                return ResponseEntity.ok().body(new ArrayList<ProfesorDto>());
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar obtener la lista de profesores.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Indica si el código de error corresponde a un fallo de conexión con el servicio externo de profesores
     * (Firebase): conexión rechazada / IOException, timeout de conexión o timeout de lectura.
     *
     * @param codigoError el código de la {@link SchoolManagerServerException}.
     * @return {@code true} si es un fallo de conectividad con Firebase.
     */
    private boolean esErrorConexionFirebase(int codigoError)
    {
        return codigoError == Constants.IO_EXCEPTION_FIREBASE
                || codigoError == Constants.TIMEOUT_CONEXION_FIREBASE
                || codigoError == Constants.ERROR_CONEXION_FIREBASE;
    }

    /**
     * Resuelve el {@link Departamento} EXISTENTE del curso académico seleccionado a partir del nombre que llega de
     * Firebase, asociándolo por su id correcto {@code (cursoAcademico, nombre)}.
     * <p>
     * Decisión ante un departamento desconocido: si el nombre es nulo/vacío o no existe un departamento con ese
     * nombre en el curso seleccionado, se devuelve {@code null} (el profesor queda sin departamento) y se registra
     * un aviso. Así un departamento que no esté dado de alta en el curso seleccionado NO provoca un
     * EntityNotFoundException ni tumba la carga del resto de profesores. El alta de departamentos se gestiona en la
     * configuración básica; este método no crea departamentos nuevos para no contaminar el catálogo del curso.
     *
     * @param cursoAcademico         curso académico seleccionado.
     * @param nombreDepartamento     nombre del departamento recibido de Firebase.
     * @param departamentosPorNombre caché de departamentos ya resueltos (puede contener {@code null} para los no encontrados).
     * @return el departamento gestionado del curso seleccionado, o {@code null} si no existe.
     */
    private Departamento resolverDepartamentoDelCurso(String cursoAcademico, String nombreDepartamento, Map<String, Departamento> departamentosPorNombre)
    {
        if (nombreDepartamento == null || nombreDepartamento.trim().isEmpty())
        {
            return null;
        }

        String nombre = nombreDepartamento.trim();

        // Evitamos consultas repetidas: si ya lo intentamos resolver (aunque fuera null), reutilizamos el resultado.
        if (departamentosPorNombre.containsKey(nombre))
        {
            return departamentosPorNombre.get(nombre);
        }

        Optional<Departamento> departamentoOptional = this.iDepartamentoRepository.findById(new IdDepartamento(cursoAcademico, nombre));

        Departamento departamento = departamentoOptional.orElse(null);

        if (departamento == null)
        {
            log.warn("AVISO - El departamento '{}' recibido de Firebase no existe en el curso académico seleccionado '{}'. El profesor se asociará sin departamento.", nombre, cursoAcademico);
        }

        departamentosPorNombre.put(nombre, departamento);

        return departamento;
    }

    /**
     * Recupera y devuelve la lista de reducciones asignadas a los profesores.
     * <p>
     * Este método es accesible únicamente para usuarios con el rol definido en {@code BaseConstants.ROLE_DIRECCION}.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@link ProfesorReduccionesDto} si la operación es exitosa.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignarReducciones")
    private ResponseEntity<?> obtenerReduccionesProfesores()
    {
        try
        {
            List<ProfesorReduccionesDto> list = this.iProfesorReduccionRepository.encontrarTodosProfesoresReducciones();

            return ResponseEntity.ok().body(list);

        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar obtener la lista de reducciones de profesores.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina una reducción asignada a un profesor según los parámetros proporcionados.
     * <p>
     * Este método elimina la asociación entre una reducción específica y un profesor, si existe.
     * Si la reducción no está asignada al profesor, se devuelve una respuesta de error.
     *
     * @param email             el correo electrónico del profesor al que se le eliminará la reducción.
     * @param nombreReduccion   el nombre de la reducción a eliminar.
     * @param horasReduccion    las horas asociadas a la reducción.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la reducción se eliminó correctamente.
     * - 404 (NOT_FOUND) si la reducción no está asignada al profesor.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/asignarReducciones")
    private ResponseEntity<?> borrarReduccionesProfesores(@RequestHeader(value = "email") String email,
                                                          @RequestHeader(value = "reduccion") String nombreReduccion,
                                                          @RequestHeader(value = "horas") Integer horasReduccion)
    {
        try
        {
            Reduccion reduccion = this.reduccionProfesorService.validadarYObtenerReduccion(nombreReduccion, horasReduccion);

            Profesor profesor = this.reduccionProfesorService.validadarYObtenerProfesor(email);

            IdProfesorReduccion idProfesorReduccion = new IdProfesorReduccion(profesor, reduccion);

            Optional<ProfesorReduccion> optionalProfesorReduccion = this.iProfesorReduccionRepository.findById(idProfesorReduccion);

            if (optionalProfesorReduccion.isEmpty())
            {
                String mensajeError =
                        "No hay asignada ninguna reducción con el nombre " + nombreReduccion + " al profesor " + profesor.getNombre() + " " + profesor.getApellidos() + " con horas " + horasReduccion;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.REDUCCION_NO_ASIGNADA_A_PROFESOR, mensajeError);
            }

            ProfesorReduccion profesorReduccion = new ProfesorReduccion();
            profesorReduccion.setIdProfesorReduccion(idProfesorReduccion);

            this.iProfesorReduccionRepository.delete(profesorReduccion);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar borrar la reducción asignada al profesor.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * @param jwtAdmin JWT del usuario admin
     * @param email    email del profesor que va a realizar la reserva
     * @return el profesor encontrado enfirebase
     * @throws SchoolManagerServerException con un error
     */
    private List<Profesor> buscarProfesoresEnFirebase(String jwtAdmin) throws SchoolManagerServerException
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
                            new TypeReference<List<DtoUsuarioBase>>()
                            {
                            });

            // Resolvemos el curso académico activo (seleccionado = true). Tras la migración a curso académico por
            // año, los departamentos viven bajo el curso seleccionado (p. ej. 2025/2026), NO bajo "". Por eso ya no
            // se puede crear el Departamento con new Departamento(nombre) (id cursoAcademico=""), porque ese registro
            // no existe y el merge en saveAllAndFlush lanzaba EntityNotFoundException tumbando toda la carga.
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            // Cacheamos los departamentos ya resueltos por nombre para no consultar la BBDD repetidamente.
            Map<String, Departamento> departamentosPorNombre = new HashMap<>();

            // Creamos una instancia de profesor con la respuesta de Firebase
            for (DtoUsuarioBase dtoUsuarioBase : listDtoUsuarioBase)
            {
                // Asociamos el departamento EXISTENTE del curso seleccionado por nombre. Si el departamento que llega
                // de Firebase no existe en el curso seleccionado, dejamos el profesor SIN departamento (departamento
                // = null) y lo registramos, en lugar de provocar un EntityNotFound que rompería toda la carga.
                Departamento departamento = this.resolverDepartamentoDelCurso(cursoAcademico, dtoUsuarioBase.getDepartamento(), departamentosPorNombre);

                Profesor profesor = new Profesor();
                profesor.setCursoAcademico(cursoAcademico);
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
}
