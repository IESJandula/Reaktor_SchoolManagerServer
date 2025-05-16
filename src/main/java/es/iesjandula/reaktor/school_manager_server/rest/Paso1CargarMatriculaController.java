package es.iesjandula.reaktor.school_manager_server.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto3;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.DatosMatriculaDto;
import es.iesjandula.reaktor.school_manager_server.interfaces.IParseoDatosBrutos;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.services.CursoEtapaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/cargarMatriculas")
public class Paso1CargarMatriculaController
{
    @Autowired
    private CursoEtapaService cursoEtapaService;

    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    @Autowired
    private IParseoDatosBrutos iParseoDatosBrutos;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private IMatriculaRepository iMatriculaRepository;

    @Autowired
    private IAlumnoRepository iAlumnoRepository;

    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IBloqueRepository iBloqueRepository;


    /**
     * Endpoint para cargar las matrículas a través de un archivo CSV.
     * <p>
     * Este método procesa un archivo CSV enviado como parte de una solicitud POST.
     * Si el archivo está vacío o hay un error durante el proceso, se lanzan
     * excepciones personalizadas.
     *
     * @param archivoCsv - El archivo CSV que contiene las matrículas a procesar.
     * @param curso      - El identificador del curso al que se asignan las
     *                   matrículas.
     * @param etapa      - La etapa educativa (por ejemplo, "Primaria",
     *                   "Secundaria") asociada al curso.
     * @return ResponseEntity<?> - Si la operación tiene éxito, devuelve un estado HTTP 200.
     *                             Para errores inesperados, devuelve un estado HTTP 500.
     * @throws IOException
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/matriculas", consumes = "multipart/form-data")
    public ResponseEntity<?> subirFicheros(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv,
                                           @RequestHeader(value = "curso", required = true) Integer curso,
                                           @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            // Si el archivo esta vacio
            if (archivoCsv.isEmpty())
            {
                // Lanzar excepcion
                String msgError = "ERROR - El archivo importado está vacío";
                log.error(msgError);
                throw new SchoolManagerServerException(Constants.ARCHIVO_VACIO, msgError);
            }

            // Convertir MultipartFile a String
            String archivoCsvReadable = new String(archivoCsv.getBytes(), StandardCharsets.UTF_8);

            // Declarar Scanner para realizar lectura del fichero
            Scanner scanner = new Scanner(archivoCsvReadable);

            // Obtenemos el cursoEtapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Llamar al Service IParseoDatosBrutos para realizar parseo
            this.iParseoDatosBrutos.parseoDatosBrutos(scanner, cursoEtapa);

            log.info("INFO - La matricula " + curso + " - " + etapa + " se ha cargado correctamente");

            List<DatosBrutoAlumnoMatricula> listAsignaturas = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAsignaturaByCursoEtapa(cursoEtapa);

            if (listAsignaturas.isEmpty())
            {
                String mensajeError = "ERROR - No se han encontrado asignaturas para ese curso y etapa";

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();

            cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(curso, etapa, Constants.SIN_GRUPO_ASIGNADO));

            // Borramos el cursoEtapa si ya existen diferentes grupos
            this.iCursoEtapaGrupoRepository.borrarPorCursoEtapa(curso, etapa);

            // Guardamos el cursoEtapaGrupo
            this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

            for (DatosBrutoAlumnoMatricula datosAsignatura : listAsignaturas)
            {
                IdAsignatura idAsignatura = new IdAsignatura();

                idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);
                idAsignatura.setNombre(datosAsignatura.getAsignatura());

                Asignatura asignatura = new Asignatura();
                asignatura.setIdAsignatura(idAsignatura);

                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }

            // Devolver OK informando que se ha insertado los registros
            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Manejo de excepciones personalizadas
            if (schoolManagerServerException.getCode() == Constants.ARCHIVO_VACIO)
            {
                // Mensaje de error personalizado
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                // Devolver la excepción personalizada con código y el mensaje de error
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }
        catch (IOException ioException)
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo realizar la lectura del fichero";
            log.error(msgError, ioException);

            // Devolver una excepción personalizada con código 1, el mensaje de error y la
            // excepcion general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    Constants.IO_EXCEPTION, msgError, ioException);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para recuperar los cursos que tienen matriculas
     *
     * Este método gestiona una solicitud GET para obtener la lista de etapas del curso
     * que tienen matriculas de estudiantes asociadas. Recupera los datos de la base de datos
     * utilizando el {@code iDatosBrutoAlumnoMatriculaRepository}.
     *
     * @return ResponseEntity<?> - Si la operación tiene éxito, devuelve un estado HTTP 200 contiene la lista
     *                             de objetos {@code CursoEtapaDto}. Para errores inesperados, devuelve un estado HTTP 500.
     *
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/matriculas")
    public ResponseEntity<?> cargarMatriculas()
    {
        try
        {
            List<CursoEtapaDto> listCursoEtapa = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosMatriculaPorEtapaYCurso();

            return ResponseEntity.ok(listCursoEtapa);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Fallo al cargar las matrículas por curso y etapa desde la base de datos";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina toda la información de registro asociada a un curso y etapa específicos.
     * Esto incluye registros en entidades relacionadas como estudiantes, asignaturas y bloques.
     *
     * @param curso el identificador del curso cuyas inscripciones se eliminarán
     * @param etapa el identificador de la etapa cuyas inscripciones se suprimirán
     * @return a ResponseEntity. Si la operación tiene éxito, devuelve un estado HTTP 200.
     *                           Para errores inesperados, devuelve un estado HTTP 500.
     *
     */
    /* Endpoint que borrar la información asignada a las matriculas relacionada a un curso*/
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/matriculas")
    public ResponseEntity<?> borrarMatriculas(@RequestHeader(value = "curso", required = true) Integer curso,
                                              @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {

            List<CursoEtapaDto> listAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosMatriculaPorEtapaYCurso(curso, etapa);

            if (listAlumnoMatriculas.isEmpty())
            {
                String mensajeError = "No se ha encontrado matriculas para el" + curso + " y " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_NO_ENCONTRADA, mensajeError);
            }

            // Obtenemos el cursoEtapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            List<AlumnoDto3> alumnoDto3 = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAlumnosByCursoEtapa(curso, etapa);

            this.iDatosBrutoAlumnoMatriculaRepository.deleteDistinctByCursoEtapa(cursoEtapa);
            this.iMatriculaRepository.borrarPorCursoYEtapa(curso, etapa);
            for (AlumnoDto3 a : alumnoDto3)
            {
                this.iAlumnoRepository.deleteByNombreAndApellidos(a.getNombre(), a.getApellidos());
            }

            List<Long> bloques = this.iAsignaturaRepository.encontrarBloquePorCursoEtapa(curso, etapa);

            this.iAsignaturaRepository.borrarPorCursoYEtapa(curso, etapa);

            Bloque bloque = new Bloque();
            for (Long cantidadBloques : bloques)
            {
                bloque.setId(cantidadBloques);
                this.iBloqueRepository.delete(bloque);
            }

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Fallo interno al intentar borrar las matrículas del" + curso + " y " + etapa;
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera datos de matriculación basados en un curso y etapa especificados.
     * Este método obtiene la lista de datos de inscripción correspondientes al curso y etapa proporcionados.
     * Si no se encuentra ningún dato, se lanza una excepción personalizada.
     *
     * @param curso el curso cuyos datos de matriculación deben recuperarse
     * @param etapa la etapa cuyos datos de inscripción deben recuperarse
     * @return a ResponseEntity Si la operación tiene éxito, devuelve un estado HTTP 200 contiene la lista
     *                          de objetos {@code DatosMatriculaDto} si se encuentran. Para errores inesperados,
     *                          devuelve un estado HTTP 500.
     */
    /*Endpoint para que nos muestre los datos de las matriculas según un curso y una etapa*/
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/datosMatriculas")
    public ResponseEntity<?> obtenerDatosMatriculas(@RequestHeader(value = "curso", required = true) Integer curso,
                                                    @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            List<DatosMatriculaDto> listDatosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarDatosMatriculaPorCursoYEtapa(curso, etapa);

            if (listDatosBrutoAlumnoMatriculas.isEmpty())
            {
                String mensajeError = "No se ha encontrado datos de matriculas para el " + curso + " y " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.DATOS_MATRICULA_NO_ENCONTRADA, mensajeError);
            }

            return ResponseEntity.ok(listDatosBrutoAlumnoMatriculas);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo encontrar en base de datos los datos de matriculas por el curso y etapa deseados";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Actualiza el estado de matriculación de un alumno en un curso y etapa en función de los parámetros proporcionados.
     *
     * @param nombre      El nombre del estudiante.
     * @param apellidos   Los apellidos del estudiante.
     * @param asignatura  La asignatura en la que está matriculado el alumno.
     * @param curso       El curso en el que está matriculado el alumno.
     * @param etapa       La etapa en la que está matriculado el alumno.
     * @param estado      El nuevo estado de inscripción que se actualizará para el alumno.
     * @return A ResponseEntity Si la operación tiene éxito, devuelve un estado HTTP 200.
     *                          Para errores inesperados, devuelve un estado HTTP 500.
     *
     */
    /*Endpoint para que nos muestre los datos de las matriculas según un curso y una etapa*/
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/datosMatriculas")
    public ResponseEntity<?> matricularAsignatura(@RequestHeader(value = "nombre") String nombre,
                                                  @RequestHeader(value = "apellidos") String apellidos,
                                                  @RequestHeader(value = "asignatura") String asignatura,
                                                  @RequestHeader(value = "curso") Integer curso,
                                                  @RequestHeader(value = "etapa") String etapa,
                                                  @RequestHeader(value = "estado") String estado)
    {
        try
        {
            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(nombre, apellidos, asignatura, curso, etapa);

            if (datosBrutoAlumnoMatriculas == null)
            {
                String mensajeError = "El alumno " + nombre + apellidos + " no está matriculado en el curso de " + curso + etapa;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_ALUMNO_NO_ENCONTRADA, mensajeError);
            }

            datosBrutoAlumnoMatriculas.setEstadoMatricula(estado);

            this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(datosBrutoAlumnoMatriculas);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo encontrar el alumno deseado en base de datos";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Gestiona el proceso de matriculación de un alumno basándose en las cabeceras proporcionadas.
     * Valida si el estudiante ya está registrado con la misma información y realiza las acciones apropiadas
     * para registrar al estudiante si no está ya presente.
     *
     * @param nombre      El nombre del estudiante que se va a matricular.
     * @param apellidos   Los apellidos del estudiante que se va a matricular.
     * @param asignatura  Las asignaturas en las que se va a matricular el alumno.
     * @param curso       El curso en el que se va a matricular el alumno.
     * @param etapa       La etapa en la que se va a matricular el alumno.
     * @param estado      El estado de la matricula del alumno
     * @return a ResponseEntity. Si la operación tiene éxito, devuelve un estado HTTP 200. Si no se encuentra al alumno,
     *          devuelve un estado HTTP 404. Si el alumno está asignado a un grupo o se produce otro conflicto,
     *          devuelve un estado HTTP 409. Para errores inesperados, devuelve un estado HTTP 500.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/datosMatriculas")
    public ResponseEntity<?> matricularAlumno(@RequestHeader(value = "nombre") String nombre,
                                              @RequestHeader(value = "apellidos") String apellidos,
                                              @RequestHeader(value = "asignatura") String asignatura,
                                              @RequestHeader(value = "curso") Integer curso,
                                              @RequestHeader(value = "etapa") String etapa,
                                              @RequestHeader(value = "estado") String estado)
    {
        try
        {

            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(nombre, apellidos, asignatura, curso, etapa);

            if (datosBrutoAlumnoMatriculas != null)
            {
                String mensajeError = "Ya existe un alumno matriculado con ese nombre y esas asignaturas";

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_ALUMNO_EXISTENTE, mensajeError);
            }

            // Obtenemos el cursoEtapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            DatosBrutoAlumnoMatricula nuevosDatosBrutoAlumnoMatricula = new DatosBrutoAlumnoMatricula();

            nuevosDatosBrutoAlumnoMatricula.setNombre(nombre);
            nuevosDatosBrutoAlumnoMatricula.setApellidos(apellidos);
            nuevosDatosBrutoAlumnoMatricula.setAsignatura(asignatura);
            nuevosDatosBrutoAlumnoMatricula.setCursoEtapa(cursoEtapa);
            nuevosDatosBrutoAlumnoMatricula.setEstadoMatricula(estado);

            this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(nuevosDatosBrutoAlumnoMatricula);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.MATRICULA_ALUMNO_EXISTENTE)
            {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo encontrar el alumno deseado en base de datos";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina la matrícula de un alumno en un curso y asignatura concretos, siempre que el alumno exista y
     * no esté asignado a un grupo.
     *
     * @param nombre      El nombre del estudiante que se va a desmatricular.
     * @param apellidos   Los apellidos del estudiante que se va a desmatricular.
     * @param asignatura  Las asignaturas en las que se va a desmatricular el alumno.
     * @param curso       El curso en el que se va a desmatricular el alumno.
     * @param etapa       La etapa en la que se va a desmatricular el alumno.
     * @param estado      El estado de matriculación actual del alumno.
     * @return A ResponseEntity. Si la operación tiene éxito, devuelve un estado HTTP 200. Si no se encuentra al alumno,
     *         devuelve un estado HTTP 404. Si el alumno está asignado a un grupo o se produce otro conflicto,
     *         devuelve un estado HTTP 409. Para errores inesperados, devuelve un estado HTTP 500.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/datosMatriculas")
    public ResponseEntity<?> desmatricularAlumno(@RequestHeader(value = "nombre") String nombre,
                                                 @RequestHeader(value = "apellidos") String apellidos,
                                                 @RequestHeader(value = "asignatura") String asignatura,
                                                 @RequestHeader(value = "curso") Integer curso,
                                                 @RequestHeader(value = "etapa") String etapa,
                                                 @RequestHeader(value = "estado") String estado)
    {
        try
        {

            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaABorrar = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnoPorNombreYApellidosYAsignaturaYCursoYEtapaYEstado(nombre, apellidos,
                    asignatura, curso, etapa, estado);

            if (datosBrutoAlumnoMatriculaABorrar == null)
            {
                String mensajeError = "El alumno " + nombre + apellidos + " no está matriculado en el curso de " + curso + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_ALUMNO_NO_ENCONTRADA, mensajeError);
            }

            if (datosBrutoAlumnoMatriculaABorrar.isAsignado())
            {
                String mensajeError = "El alumno " + nombre + apellidos + " no puede ser desmatriculado porque está asignado a una grupo";
                log.error(mensajeError);
                throw new SchoolManagerServerException(6, mensajeError);
            }

            this.iDatosBrutoAlumnoMatriculaRepository.delete(datosBrutoAlumnoMatriculaABorrar);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.MATRICULA_ALUMNO_NO_ENCONTRADA)
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
            String mensajeError = "ERROR - No se pudo encontrar el alumno deseado en base de datos";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
