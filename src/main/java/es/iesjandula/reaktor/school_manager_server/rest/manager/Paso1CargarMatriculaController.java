package es.iesjandula.reaktor.school_manager_server.rest.manager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import java.nio.charset.Charset;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaAdHocDto;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaSinGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.DatosMatriculaDto;
import es.iesjandula.reaktor.school_manager_server.models.Alumno;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Bloque;
import es.iesjandula.reaktor.school_manager_server.models.CursoAcademico;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;
import es.iesjandula.reaktor.school_manager_server.models.Matricula;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IBloqueRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoAcademicoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver;
import es.iesjandula.reaktor.school_manager_server.services.manager.CursoEtapaService;
import es.iesjandula.reaktor.school_manager_server.services.manager.ParseoCsvService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/cargarMatriculas")
public class Paso1CargarMatriculaController
{
    @Autowired
    private ParseoCsvService parseoCsvService;

    @Autowired
    private CursoEtapaService cursoEtapaService;

    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;
    
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

    @Autowired
    private ICursoAcademicoRepository iCursoAcademicoRepository;

    @Autowired
    private CursoAcademicoResolver cursoAcademicoResolver;


    /**
     * Carga las matrículas de estudiantes a través de un archivo CSV enviado mediante una solicitud POST.
     * <p>
     * Este método procesa el archivo CSV recibido, valida la existencia del curso y etapa,
     * realiza el parseo del contenido y registra los datos de matrícula en la base de datos.
     * Si ya existen asignaciones de grupo previas para el curso y etapa indicados, se eliminan
     * antes de registrar las nuevas. También se crean asignaturas sin grupo asignado.
     *
     * @param archivoCsv el archivo CSV que contiene los datos de matrícula a cargar; no debe estar vacío.
     * @param curso      el identificador del curso al que pertenecen las matrículas, proporcionado en la cabecera.
     * @param etapa      la etapa educativa asociada al curso (por ejemplo, "Primaria", "Secundaria"), proporcionada en la cabecera.
     * @return una {@link ResponseEntity} con:
     * - 201 (CREATED) si la carga del archivo y el procesamiento se realizan correctamente.
     * - 400 (BAD_REQUEST) si el archivo está vacío.
     * - 404 (NOT_FOUND) si no se encuentran asignaturas asociadas al curso y etapa.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error de lectura del archivo.
     * @throws IOException si se produce un error al leer el contenido del archivo CSV.
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
                String msgError = "El archivo importado está vacío";
                log.error(msgError);
                throw new SchoolManagerServerException(Constants.ARCHIVO_VACIO, msgError);
            }

            // Obtener la codificación del archivo CSV
            Charset encoding = this.obtenerCodificacionArchivoCSV(archivoCsv);

            // Convertir MultipartFile a String
            String csvString = new String(archivoCsv.getBytes(), encoding);

            // Obtenemos el cursoEtapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Llamar al Service IParseoDatosBrutos para realizar parseo
            this.parseoCsvService.parseoDatosBrutos(csvString, cursoEtapa);

            log.info("INFO - La matricula " + curso + " - " + etapa + " se ha cargado correctamente");

            List<DatosBrutoAlumnoMatricula> listAsignaturas = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAsignaturaByCursoEtapa(cursoEtapa);

            if (listAsignaturas.isEmpty())
            {
                String mensajeError = "No se han encontrado asignaturas para " + curso + " " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();

            cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.SIN_GRUPO_ASIGNADO));

            // Borramos el cursoEtapa si ya existen diferentes grupos
            this.iCursoEtapaGrupoRepository.borrarPorCursoEtapa(cursoAcademico, curso, etapa);

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
            return ResponseEntity.status(HttpStatus.CREATED).build();
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

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.IO_EXCEPTION, msgError, ioException);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene la codificación del archivo CSV
     * 
     * @param archivoCsv el archivo CSV que contiene los datos de matrícula a cargar; no debe estar vacío.
     * @return la codificación del archivo CSV
     */
    private Charset obtenerCodificacionArchivoCSV(MultipartFile archivoCsv) throws IOException
    {
        // Creamos una instancia de CharsetDetector
        CharsetDetector detector = new CharsetDetector();

        // Establecemos el texto del archivo CSV
        detector.setText(archivoCsv.getBytes());

        // Detectamos la codificación del archivo CSV
        CharsetMatch match = detector.detect();

        // Devolvemos la codificación del archivo CSV
        return Charset.forName(match.getName());
    }

    /**
     * Recupera la lista de cursos y etapas que tienen matrículas de estudiantes asociadas.
     * <p>
     * Este endpoint realiza una consulta a la base de datos para obtener todas las combinaciones
     * de curso y etapa en las que existen registros de matrícula.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@code CursoEtapaDto} si la operación es exitosa.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la consulta.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/matriculas")
    public ResponseEntity<?> listarCursosEtapasConMatriculas()
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<CursoEtapaDto> listCursoEtapa = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosMatriculaPorEtapaYCurso(cursoAcademico);

            return ResponseEntity.ok().body(listCursoEtapa);
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
     * Elimina toda la información de matrícula asociada a un curso y etapa específicos.
     * <p>
     * Este proceso incluye la eliminación de registros vinculados a estudiantes, asignaturas y bloques.
     *
     * @param curso el identificador del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa la etapa educativa asociada al curso, proporcionada en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 200 (NO_CONTENT) si la operación se realiza correctamente.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error durante el proceso de eliminación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @Transactional
    @RequestMapping(method = RequestMethod.DELETE, value = "/matriculas")
    public ResponseEntity<?> borrarMatriculasPorCursoYEtapa(@RequestHeader(value = "curso", required = true) Integer curso,
                                                            @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<CursoEtapaDto> listAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosMatriculaPorEtapaYCurso(cursoAcademico, curso, etapa);

            if (listAlumnoMatriculas.isEmpty())
            {
                String mensajeError = "No se ha encontrado matriculas para " + curso + " " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_NO_ENCONTRADA, mensajeError);
            }

            // Borrado en cascada apoyado en las FKs ON DELETE CASCADE configuradas en BBDD:
            //   CursoEtapaGrupo  ->  Asignatura  ->  Matricula
            //                                    ->  (Alumno también cae cuando su Matricula desaparece, pero
            //                                         como un Alumno puede tener matrículas en otros curso/etapa,
            //                                         la limpieza de huérfanos se hace al final)
            //   CursoEtapa -> DatosBrutoAlumnoMatricula

            // 1. Borramos los grupos del curso/etapa: la BBDD propaga el borrado a Asignatura y Matricula
            this.iCursoEtapaGrupoRepository.borrarPorCursoEtapa(cursoAcademico, curso, etapa);

            // 2. Limpiamos los Bloques que se hayan quedado huérfanos (sin asignaturas asociadas)
            this.iBloqueRepository.deleteBloquesSinAsignaturas();

            // 3. Limpiamos los Alumnos que se hayan quedado sin ninguna matrícula
            this.iAlumnoRepository.deleteAlumnosSinMatriculas();

            // 4. Borramos los datos en bruto del curso/etapa
            this.iDatosBrutoAlumnoMatriculaRepository.borrarPorCursoYEtapa(cursoAcademico, curso, etapa);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Fallo interno al intentar borrar las matrículas del curso y etapa deseados";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera los datos de matrícula correspondientes a un curso y etapa específicos.
     * <p>
     * Este endpoint obtiene la lista de inscripciones registradas. Si no se encuentran datos, se lanza una excepción personalizada.
     *
     * @param curso el curso académico, proporcionado en la cabecera de la solicitud.
     * @param etapa la etapa educativa, proporcionada en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@code DatosMatriculaDto} si se encuentran datos.
     * - 404 (NOT_FOUND) si no hay inscripciones para los parámetros indicados.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/datosMatriculas")
    public ResponseEntity<?> obtenerDatosMatriculas(@RequestHeader(value = "curso", required = true) Integer curso,
                                                    @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<DatosMatriculaDto> listDatosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarDatosMatriculaPorCursoYEtapa(cursoAcademico, curso, etapa);

            if (listDatosBrutoAlumnoMatriculas.isEmpty())
            {
                String mensajeError = "No se ha encontrado datos de matriculas para " + curso + " " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.DATOS_MATRICULA_NO_ENCONTRADA, mensajeError);
            }

            return ResponseEntity.ok().body(listDatosBrutoAlumnoMatriculas);
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
     * Matrícula o desmatricula a un alumno en una asignatura determinada, actualizando el estado de matrícula
     * <p>
     * Este método se encarga de registrar la matrícula de un alumno en una asignatura existente o nueva,
     * o eliminarla en función del estado proporcionado. También sincroniza la tabla de datos brutos con los
     * cambios realizados.
     *
     * @param nombre     el nombre del alumno.
     * @param apellidos  los apellidos del alumno.
     * @param asignatura la asignatura relacionada con la matrícula.
     * @param curso      el curso académico correspondiente.
     * @param etapa      la etapa educativa correspondiente.
     * @param estado     el nuevo estado de la matrícula (por ejemplo, "MATR", "PEND", "NO_MATR")
     * @return una {@link ResponseEntity} con:
     * - 200 (NO_CONTENT) si la actualización es exitosa.
     * - 404 (NOT_FOUND) si no encuentra la matrícula del alumno, si no encuentra el alumno, si no encuentra
     * el grupo o si no encuentra la asignatura.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @Transactional
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
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(cursoAcademico, nombre, apellidos, asignatura, curso, etapa);

            if (datosBrutoAlumnoMatriculas == null)
            {
                String mensajeError = "El alumno " + nombre + " " + apellidos + " no está matriculado en " + curso + " " + etapa;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_ALUMNO_NO_ENCONTRADA, mensajeError);
            }

//          Si el alumno ya está asignado a un grupo al cambiar el estado de la matrícula se añade o se borra
            if (this.iMatriculaRepository.encontrarAlumnoPorNombreAndApellidosAndCursoAndEtapa(cursoAcademico, nombre, apellidos, curso, etapa) != null)
            {
//              Si el estado de la matricula es MATR o PEND
                if (estado.equals(Constants.ESTADO_MATRICULADO) || estado.equals(Constants.ESTADO_PENDIENTE))
                {
                    if (this.iMatriculaRepository.encontrarMatriculaPorNombreAndApellidosAndCursoAndEtapa(cursoAcademico, nombre, apellidos, asignatura, curso, etapa) == null)
                    {
//                      Buscamos el alumno en base de datos
                        Optional<Alumno> alumno = this.iAlumnoRepository.findByNombreAndApellidos(nombre, apellidos);
                        if (alumno.isEmpty())
                        {
                            String mensajeError = "El alumno " + nombre + " " + apellidos + " no existe en base de datos";
                            log.error(mensajeError);
                            throw new SchoolManagerServerException(Constants.ALUMNO_NO_ENCONTRADO, mensajeError);
                        }

//                      Buscamos el grupo al que pertenece el alumno
                        List<String> listaGrupos = this.iMatriculaRepository.encontrarGrupoPorNombreAndApellidosAndCursoAndEtapa(cursoAcademico, alumno.get().getNombre(), alumno.get().getApellidos(), curso, etapa);

                        if (listaGrupos.isEmpty())
                        {
                            String mensajeError = "El alumno no tiene grupo asignado";
                            log.error(mensajeError);
                            throw new SchoolManagerServerException(Constants.GRUPO_NO_ENCONTRADO, mensajeError);
                        }

                        String grupo = listaGrupos.stream().findFirst().get();

                        //Si la lista de grupos contiene entradas de optativas
                        if (listaGrupos.contains(Constants.GRUPO_OPTATIVAS))
                        {

                            // Buscamos la asignatura en base de datos
                            Optional<Asignatura> asignaturaExistente = this.iAsignaturaRepository
                                    .encontrarAsignaturaPorNombreYCursoYEtapaYGrupoOptativas(cursoAcademico, curso, etapa, asignatura);
                            Bloque bloqueDeOptativas = new Bloque();
                            if (asignaturaExistente.isPresent())
                            {
                                bloqueDeOptativas = asignaturaExistente.get().getBloqueId();
                            }

//                      Usamos la asignatura existente o crear una nueva si no existe
                            Bloque finalBloqueDeOptativas = bloqueDeOptativas;
                            Asignatura asignaturaParaMatricula = asignaturaExistente.orElseGet(() -> {
                                AsignaturaSinGrupoDto asignaturaABuscar = this.iAsignaturaRepository
                                        .encontrarPorCursoYEtapaYNombre(cursoAcademico, curso, etapa, asignatura);

                                if (asignaturaABuscar == null)
                                {
                                    String mensajeError = "No se encontró la asignatura " + asignatura + " para " + curso + " " + etapa;
                                    log.error(mensajeError);
                                    try
                                    {
                                        throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
                                    }
                                    catch (SchoolManagerServerException schoolManagerServerException)
                                    {
                                        throw new RuntimeException(schoolManagerServerException);
                                    }
                                }

                                IdAsignatura idAsignatura = new IdAsignatura();
                                IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
                                idCursoEtapaGrupo.setCursoAcademico(cursoAcademico);
                                idCursoEtapaGrupo.setCurso(curso);
                                idCursoEtapaGrupo.setEtapa(etapa);
                                idCursoEtapaGrupo.setGrupo(Constants.GRUPO_OPTATIVAS);


                                CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                                cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

                                idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);
                                idAsignatura.setNombre(asignatura);

                                Asignatura nuevaAsignatura = new Asignatura();
                                nuevaAsignatura.setIdAsignatura(idAsignatura);
                                nuevaAsignatura.setHoras(asignaturaABuscar.getHoras());
                                nuevaAsignatura.setEsoBachillerato(asignaturaABuscar.isEsoBachillerato());
                                nuevaAsignatura.setSinDocencia(asignaturaABuscar.isSinDocencia());
                                nuevaAsignatura.setDesdoble(asignaturaABuscar.isDesdoble());
                                //Aqui seteo el idBloque de la asignatura encontrada
                                nuevaAsignatura.setBloqueId(finalBloqueDeOptativas);

                                return this.iAsignaturaRepository.saveAndFlush(nuevaAsignatura);
                            });

//                        Primero buscamos si existe la asignatura con grupo "sin grupo" y la borramos
                            Optional<Asignatura> asignaturaGrupoZ = iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(
                                    cursoAcademico,
                                    curso,
                                    etapa,
                                    asignatura,
                                    Constants.SIN_GRUPO_ASIGNADO);  // Buscamos específicamente el grupo "sin grupo"

                            if (asignaturaGrupoZ.isPresent())
                            {
                                Asignatura asignaturaABorrar = asignaturaGrupoZ.get();
                                iAsignaturaRepository.delete(asignaturaABorrar);
                                iAsignaturaRepository.flush();
                            }

                            IdMatricula idMatricula = new IdMatricula(asignaturaParaMatricula, alumno.get());

                            Matricula nuevaMatricula = new Matricula(idMatricula);
                            this.iMatriculaRepository.saveAndFlush(nuevaMatricula);

                            datosBrutoAlumnoMatriculas.setAsignado(true);
                            datosBrutoAlumnoMatriculas.setEstadoMatricula(estado);
                        }
                        else
                        {
//                          Buscamos la asignatura en base de datos
                            Optional<Asignatura> asignaturaExistente = this.iAsignaturaRepository
                                    .encontrarAsignaturaPorNombreYCursoYEtapaYGrupoOSinEl(cursoAcademico, curso, etapa, asignatura, grupo);
                            Bloque bloqueDeOptativas = new Bloque();
                            if (asignaturaExistente.isPresent())
                            {
                                bloqueDeOptativas = asignaturaExistente.get().getBloqueId();
                            }
//                          Usamos la asignatura existente o crear una nueva si no existe
                            Bloque finalBloqueDeOptativas = bloqueDeOptativas;
                            Asignatura asignaturaParaMatricula = asignaturaExistente.orElseGet(() -> {
                                AsignaturaSinGrupoDto asignaturaABuscar = this.iAsignaturaRepository
                                        .encontrarPorCursoYEtapaYNombre(cursoAcademico, curso, etapa, asignatura);

                                if (asignaturaABuscar == null)
                                {
                                    String mensajeError = "No se encontró la asignatura " + asignatura + " para " + curso + " " + etapa;
                                    log.error(mensajeError);
                                    try
                                    {
                                        throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
                                    }
                                    catch (SchoolManagerServerException schoolManagerServerException)
                                    {
                                        throw new RuntimeException(schoolManagerServerException);
                                    }
                                }

                                IdAsignatura idAsignatura = new IdAsignatura();
                                IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
                                idCursoEtapaGrupo.setCursoAcademico(cursoAcademico);
                                idCursoEtapaGrupo.setCurso(curso);
                                idCursoEtapaGrupo.setEtapa(etapa);

                                if (asignaturaABuscar.getIdBloque() != null)
                                {
                                    idCursoEtapaGrupo.setGrupo(Constants.GRUPO_OPTATIVAS);
                                }
                                else
                                {
                                    idCursoEtapaGrupo.setGrupo(grupo);
                                }

                                CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                                cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

                                idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);
                                idAsignatura.setNombre(asignatura);

                                Asignatura nuevaAsignatura = new Asignatura();
                                nuevaAsignatura.setIdAsignatura(idAsignatura);
                                nuevaAsignatura.setHoras(asignaturaABuscar.getHoras());
                                nuevaAsignatura.setEsoBachillerato(asignaturaABuscar.isEsoBachillerato());
                                nuevaAsignatura.setSinDocencia(asignaturaABuscar.isSinDocencia());
                                nuevaAsignatura.setDesdoble(asignaturaABuscar.isDesdoble());
                                //Aqui seteo el idBloque de la asignatura encontrada
                                nuevaAsignatura.setBloqueId(finalBloqueDeOptativas);

                                return this.iAsignaturaRepository.saveAndFlush(nuevaAsignatura);
                            });

//                          1) Recupero la fila antigua:
                            Asignatura vieja = asignaturaExistente.get();
//                          2) Recupero el CursoEtapaGrupo “(curso, etapa, "A")” de BD:
                            CursoEtapaGrupo cegA;
                            if (vieja.isOptativa())
                            {
                                cegA = iCursoEtapaGrupoRepository
                                        .findById(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.GRUPO_OPTATIVAS))
                                        .orElseThrow();
                            }
                            else
                            {
                                cegA = iCursoEtapaGrupoRepository
                                        .findById(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo))
                                        .orElseThrow();
                            }
//                          3) Creo la nueva Asignatura con grupo="A":
                            IdAsignatura nuevoId = new IdAsignatura();
                            nuevoId.setCursoEtapaGrupo(cegA);
                            nuevoId.setNombre(vieja.getIdAsignatura().getNombre());

                            Asignatura copia = new Asignatura();
                            copia.setIdAsignatura(nuevoId);
                            copia.setHoras(vieja.getHoras());
                            copia.setEsoBachillerato(vieja.isEsoBachillerato());
                            copia.setSinDocencia(vieja.isSinDocencia());
                            copia.setDesdoble(vieja.isDesdoble());
                            copia.setBloqueId(vieja.getBloqueId());
                            iAsignaturaRepository.saveAndFlush(copia);

//                          4) Después, creo la Matricula sobre “copia”:
                            IdMatricula idMat = new IdMatricula(copia, alumno.get());
                            iMatriculaRepository.saveAndFlush(new Matricula(idMat));

//                          5) Por último, si quiero eliminar la fila vieja ("sin grupo"), hago:
                            iAsignaturaRepository.delete(vieja);
                            iAsignaturaRepository.flush();

                            datosBrutoAlumnoMatriculas.setAsignado(true);
                            datosBrutoAlumnoMatriculas.setEstadoMatricula(estado);

                            //Primero buscamos si existe la asignatura con grupo "sin grupo" y la borramos
                            Optional<Asignatura> asignaturaGrupoZ = iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(
                                    cursoAcademico,
                                    curso,
                                    etapa,
                                    asignatura,
                                    Constants.SIN_GRUPO_ASIGNADO);  // Buscamos específicamente el grupo "sin grupo"

                            if (asignaturaGrupoZ.isPresent())
                            {
                                Asignatura asignaturaABorrar = asignaturaGrupoZ.get();
                                iAsignaturaRepository.delete(asignaturaABorrar);
                                iAsignaturaRepository.flush();
                            }
                        }
                    }
                }
//              Si es cualquiera de los otros estados
                else
                {
//                  Buscamos la matricula y la eliminamos
                    Matricula matricula = this.iMatriculaRepository.encontrarMatriculaPorNombreAndApellidosAndCursoAndEtapa(cursoAcademico, nombre, apellidos, asignatura, curso, etapa);
                    if (matricula != null)
                    {
                        datosBrutoAlumnoMatriculas.setAsignado(false);
                        this.iMatriculaRepository.delete(matricula);
//                      Después de borrar confirmamos la operación
                        this.iMatriculaRepository.flush();

                        // Si la matricula actual de asignatura del alumno es la única de su grupo
                        if (this.iMatriculaRepository.numeroAsignaturasPorNombreYGrupo(
                                cursoAcademico, asignatura, curso, etapa,
                                matricula.getIdMatricula().getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getGrupo()) == 0)
                        {

                            Optional<Asignatura> asignaturaEncontrada = iAsignaturaRepository
                                    .encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(
                                            cursoAcademico, curso, etapa, asignatura,
                                            matricula.getIdMatricula().getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getGrupo());

                            // Primero borramos la asignatura actual
                            if (asignaturaEncontrada.isPresent())
                            {
                                iAsignaturaRepository.delete(asignaturaEncontrada.get());

                                // Verificar si quedan más grupos con esta asignatura
                                Long gruposRestantes = iAsignaturaRepository.contarGruposPorAsignatura(
                                        cursoAcademico,
                                        asignatura,
                                        curso,
                                        etapa);

                                // Solo si no quedan más grupos, creamos la asignatura sin grupo
                                if (gruposRestantes == 0)
                                {
                                    // Creo la asignatura sin el grupo
                                    Asignatura asignaturaSinGrupo = getAsignatura(
                                            cursoAcademico,
                                            curso,
                                            etapa,
                                            asignatura,
                                            asignaturaEncontrada);
                                    this.iAsignaturaRepository.saveAndFlush(asignaturaSinGrupo);
                                }
                            }
                        }

                    }

                }
            }

//          Si no solo se modifica el estado en la tabla de datosBrutos
            datosBrutoAlumnoMatriculas.setEstadoMatricula(estado);
            this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(datosBrutoAlumnoMatriculas);


            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.MATRICULA_ALUMNO_NO_ENCONTRADA)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else if (schoolManagerServerException.getCode() == Constants.ALUMNO_NO_ENCONTRADO)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else if (schoolManagerServerException.getCode() == Constants.GRUPO_NO_ENCONTRADO)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo encontrar el alumno deseado en base de datos para matricular en la asignatura";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Gestiona el proceso de matriculación de un alumno según los datos proporcionados en la cabecera de la solicitud.
     * <p>
     * Valida si el alumno ya está registrado y realiza la operación de matriculación solo si no existe duplicidad.
     *
     * @param nombre     el nombre del alumno a matricular.
     * @param apellidos  los apellidos del alumno a matricular.
     * @param asignatura la asignatura en la que se va a matricular.
     * @param curso      el curso académico correspondiente.
     * @param etapa      la etapa educativa correspondiente.
     * @param estado     el estado de la matrícula (por ejemplo, ACTIVO).
     * @return una {@link ResponseEntity} con:
     * - 200 (CREATED) si la matrícula es realizada con éxito.
     * - 404 (NOT_FOUND) si el alumno no se encuentra registrado.
     * - 409 (CONFLICT) si el alumno ya está asignado a un grupo o se produce un conflicto.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
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

            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(cursoAcademico, nombre, apellidos, asignatura, curso, etapa);

            if (datosBrutoAlumnoMatriculas != null)
            {
                String mensajeError = "El alumnos " + nombre + " " + apellidos + " ya está matriculado en " + curso + " " + etapa;

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

            return ResponseEntity.status(HttpStatus.CREATED).build();
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
            String mensajeError = "ERROR - No se pudo matricular el alumno";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina la matrícula de un alumno para una asignatura concreta, siempre que no esté asignado a un grupo.
     *
     * @param nombre     el nombre del alumno que se desea desmatricular.
     * @param apellidos  los apellidos del alumno.
     * @param asignatura la asignatura de la que se desea eliminar la matrícula.
     * @param curso      el curso académico correspondiente.
     * @param etapa      la etapa educativa correspondiente.
     * @param estado     el estado actual de la matrícula.
     * @return una {@link ResponseEntity} con:
     * - 200 (NO_CONTENT) si la matrícula es eliminada con éxito.
     * - 404 (NOT_FOUND) si el alumno no está registrado.
     * - 409 (CONFLICT) si el alumno está asignado a un grupo o existe otro conflicto.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
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

            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaABorrar = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnoPorNombreYApellidosYAsignaturaYCursoYEtapaYEstado(cursoAcademico, nombre, apellidos,
                    asignatura, curso, etapa, estado);

            if (datosBrutoAlumnoMatriculaABorrar == null)
            {
                String mensajeError = "El alumno " + nombre + " " + apellidos + " no está matriculado en el curso de " + curso + " " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_ALUMNO_NO_ENCONTRADA, mensajeError);
            }

            if (datosBrutoAlumnoMatriculaABorrar.isAsignado())
            {
                String mensajeError = "El alumno " + nombre + " " + apellidos + " no puede ser desmatriculado porque está asignado a una grupo";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ALUMNO_ASIGNADO_A_GRUPO, mensajeError);
            }

            this.iDatosBrutoAlumnoMatriculaRepository.delete(datosBrutoAlumnoMatriculaABorrar);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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

    /**
     * Crea una asignatura ad-hoc (a medida) para un curso, etapa y curso académico concretos.
     * <p>
     * La asignatura se persiste con el grupo {@link Constants#SIN_GRUPO_ASIGNADO} (catálogo) y el curso académico
     * global (igual que el resto de asignaturas de este flujo de matrículas), pero marcada con {@code esAdHoc = true}.
     * Al crearla, TODOS los alumnos del curso/etapa quedan como NO_MATR: esto se materializa creando una fila
     * {@link DatosBrutoAlumnoMatricula} por alumno con estado {@link Constants#ESTADO_NO_MATRICULADO} y
     * {@code asignado = false} (NO se crean filas {@link Matricula}, ya que en este modelo NO_MATR es un estado de
     * datos brutos sin matrícula real, que es lo que pinta la tabla del frontend).
     * </p>
     *
     * @param cursoAcademico el curso académico, proporcionado en la cabecera de la solicitud.
     * @param asignaturaAdHocDto cuerpo con {@code nombre}, {@code curso} y {@code etapa}.
     * @return una {@link ResponseEntity} con:
     * - 201 (CREATED) si la asignatura ad-hoc se crea correctamente.
     * - 400 (BAD_REQUEST) si los datos son inválidos o la asignatura ya existe.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @Transactional
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/asignaturaAdHoc", consumes = "application/json")
    public ResponseEntity<?> crearAsignaturaAdHoc(@RequestBody AsignaturaAdHocDto asignaturaAdHocDto)
    {
        try
        {
            // El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            this.validarAsignaturaAdHocDto(asignaturaAdHocDto);

            Integer curso = asignaturaAdHocDto.getCurso();
            String etapa = asignaturaAdHocDto.getEtapa();
            String nombre = asignaturaAdHocDto.getNombre();

            // Validamos que el curso/etapa exista (estructura global usada por matrículas)
            this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Comprobamos que no exista ya una asignatura ad-hoc con ese nombre en el curso/etapa
            List<Asignatura> asignaturasExistentes = this.iAsignaturaRepository.encontrarAsignaturaPorNombre(cursoAcademico, curso, etapa, nombre);
            boolean yaExisteAdHoc = asignaturasExistentes.stream().anyMatch(Asignatura::isEsAdHoc);
            if (yaExisteAdHoc)
            {
                log.error(Constants.ERR_ASIGNATURA_AD_HOC_YA_EXISTE_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ASIGNATURA_AD_HOC_YA_EXISTE_CODE, Constants.ERR_ASIGNATURA_AD_HOC_YA_EXISTE_MESSAGE);
            }

            // Aseguramos que exista el CursoEtapaGrupo (global, "Sin grupo") al que colgar la asignatura
            CursoEtapaGrupo cursoEtapaGrupo = this.obtenerOCrearCursoEtapaGrupoSinGrupo(cursoAcademico, curso, etapa);

            // Creamos la asignatura ad-hoc
            IdAsignatura idAsignatura = new IdAsignatura(cursoEtapaGrupo, nombre);
            Asignatura asignatura = new Asignatura();
            asignatura.setIdAsignatura(idAsignatura);
            asignatura.setEsAdHoc(true);
            this.iAsignaturaRepository.saveAndFlush(asignatura);

            // Materializamos a NO_MATR a todos los alumnos del curso/etapa
            this.materializarNoMatriculados(cursoAcademico, curso, etapa, nombre);

            log.info("INFO - Asignatura ad-hoc '" + nombre + "' creada para " + curso + " " + etapa + " (cursoAcademico " + cursoAcademico + ")");

            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo crear la asignatura ad-hoc";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Borra una asignatura ad-hoc (y sus filas NO_MATR materializadas) de un curso, etapa y curso académico.
     * <p>
     * Solo borra asignaturas marcadas como ad-hoc; si la asignatura no existe o no es ad-hoc, devuelve error
     * para impedir el borrado de asignaturas normales provenientes del CSV.
     * </p>
     *
     * @param cursoAcademico el curso académico, proporcionado en la cabecera de la solicitud.
     * @param asignaturaAdHocDto cuerpo con {@code nombre}, {@code curso} y {@code etapa}.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la asignatura ad-hoc se borra correctamente.
     * - 400 (BAD_REQUEST) si los datos son inválidos o la asignatura no es ad-hoc.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @Transactional
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/asignaturaAdHoc", consumes = "application/json")
    public ResponseEntity<?> borrarAsignaturaAdHoc(@RequestBody AsignaturaAdHocDto asignaturaAdHocDto)
    {
        try
        {
            // El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            this.validarAsignaturaAdHocDto(asignaturaAdHocDto);

            Integer curso = asignaturaAdHocDto.getCurso();
            String etapa = asignaturaAdHocDto.getEtapa();
            String nombre = asignaturaAdHocDto.getNombre();

            // Recuperamos las asignaturas ad-hoc con ese nombre en el curso/etapa
            List<Asignatura> asignaturasAdHoc = this.iAsignaturaRepository.encontrarAsignaturaPorNombre(cursoAcademico, curso, etapa, nombre)
                    .stream().filter(Asignatura::isEsAdHoc).toList();

            if (asignaturasAdHoc.isEmpty())
            {
                log.error(Constants.ERR_ASIGNATURA_NO_AD_HOC_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ASIGNATURA_NO_AD_HOC_CODE, Constants.ERR_ASIGNATURA_NO_AD_HOC_MESSAGE);
            }

            // Borramos las filas NO_MATR materializadas (datos brutos) de esa asignatura
            this.iDatosBrutoAlumnoMatriculaRepository.borrarPorAsignaturaYCursoYEtapa(cursoAcademico, nombre, curso, etapa);

            // Borramos las asignaturas ad-hoc (las matrículas asociadas caen por FK ON DELETE CASCADE)
            for (Asignatura asignaturaAdHoc : asignaturasAdHoc)
            {
                this.iAsignaturaRepository.delete(asignaturaAdHoc);
            }
            this.iAsignaturaRepository.flush();

            log.info("INFO - Asignatura ad-hoc '" + nombre + "' borrada de " + curso + " " + etapa + " (cursoAcademico " + cursoAcademico + ")");

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo borrar la asignatura ad-hoc";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene (o crea si no existe) el {@link CursoEtapaGrupo} global con grupo {@link Constants#SIN_GRUPO_ASIGNADO}.
     *
     * @param curso el curso.
     * @param etapa la etapa.
     * @return el {@link CursoEtapaGrupo} persistido.
     */
    private CursoEtapaGrupo obtenerOCrearCursoEtapaGrupoSinGrupo(String cursoAcademico, Integer curso, String etapa)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.SIN_GRUPO_ASIGNADO);

        Optional<CursoEtapaGrupo> cursoEtapaGrupoOpt = this.iCursoEtapaGrupoRepository.findById(idCursoEtapaGrupo);
        if (cursoEtapaGrupoOpt.isPresent())
        {
            return cursoEtapaGrupoOpt.get();
        }

        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
        return this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);
    }

    /**
     * Materializa como NO_MATR a todos los alumnos del curso/etapa para la asignatura indicada, creando filas
     * {@link DatosBrutoAlumnoMatricula} con estado {@link Constants#ESTADO_NO_MATRICULADO}.
     *
     * @param curso       el curso.
     * @param etapa       la etapa.
     * @param asignatura  el nombre de la asignatura ad-hoc.
     * @throws SchoolManagerServerException si no se encuentra el curso/etapa.
     */
    private void materializarNoMatriculados(String cursoAcademico, Integer curso, String etapa, String asignatura) throws SchoolManagerServerException
    {
        CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

        List<Object[]> alumnos = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosDistintosPorCursoYEtapa(cursoAcademico, curso, etapa);

        for (Object[] alumno : alumnos)
        {
            String nombreAlumno = (String) alumno[0];
            String apellidosAlumno = (String) alumno[1];

            DatosBrutoAlumnoMatricula datosBruto = new DatosBrutoAlumnoMatricula();
            datosBruto.setNombre(nombreAlumno);
            datosBruto.setApellidos(apellidosAlumno);
            datosBruto.setAsignatura(asignatura);
            datosBruto.setAsignado(false);
            datosBruto.setEstadoMatricula(Constants.ESTADO_NO_MATRICULADO);
            datosBruto.setCursoEtapa(cursoEtapa);

            this.iDatosBrutoAlumnoMatriculaRepository.save(datosBruto);
        }

        this.iDatosBrutoAlumnoMatriculaRepository.flush();
    }

    /**
     * Valida que el curso académico no sea nulo/vacío y que exista en la base de datos.
     *
     * @param cursoAcademico el curso académico a validar.
     * @throws SchoolManagerServerException si el curso académico es nulo/vacío o no existe.
     */
    private void validarCursoAcademico(String cursoAcademico) throws SchoolManagerServerException
    {
        if (cursoAcademico == null || cursoAcademico.isEmpty())
        {
            log.error(Constants.ERR_CURSO_ACADEMICO_NULO_VACIO_MESSAGE);
            throw new SchoolManagerServerException(Constants.ERR_CURSO_ACADEMICO_NULO_VACIO_CODE, Constants.ERR_CURSO_ACADEMICO_NULO_VACIO_MESSAGE);
        }

        Optional<CursoAcademico> cursoAcademicoEntity = this.iCursoAcademicoRepository.findByCursoAcademico(cursoAcademico);
        if (cursoAcademicoEntity.isEmpty())
        {
            log.error(Constants.ERR_CURSO_ACADEMICO_NO_EXISTE_MESSAGE);
            throw new SchoolManagerServerException(Constants.ERR_CURSO_ACADEMICO_NO_EXISTE_CODE, Constants.ERR_CURSO_ACADEMICO_NO_EXISTE_MESSAGE);
        }
    }

    /**
     * Valida que el cuerpo de la asignatura ad-hoc tenga nombre, curso y etapa.
     *
     * @param asignaturaAdHocDto el cuerpo a validar.
     * @throws SchoolManagerServerException si algún campo es nulo o vacío.
     */
    private void validarAsignaturaAdHocDto(AsignaturaAdHocDto asignaturaAdHocDto) throws SchoolManagerServerException
    {
        if (asignaturaAdHocDto == null ||
            asignaturaAdHocDto.getNombre() == null || asignaturaAdHocDto.getNombre().isEmpty() ||
            asignaturaAdHocDto.getCurso() == null ||
            asignaturaAdHocDto.getEtapa() == null || asignaturaAdHocDto.getEtapa().isEmpty())
        {
            log.error(Constants.ERR_ASIGNATURA_AD_HOC_DATOS_INVALIDOS_MESSAGE);
            throw new SchoolManagerServerException(Constants.ERR_ASIGNATURA_AD_HOC_DATOS_INVALIDOS_CODE, Constants.ERR_ASIGNATURA_AD_HOC_DATOS_INVALIDOS_MESSAGE);
        }
    }

    /**
     * Recupera una instancia de {@code Asignatura} construyéndola a partir de los parámetros proporcionados.
     *
     * @param curso                el identificador del curso académico.
     * @param etapa                la etapa o nivel educativo del curso.
     * @param nombreAsignatura     el nombre de la asignatura.
     * @param asignaturaEncontrada un {@code Optional} que contiene una instancia existente de {@code Asignatura}
     *                             utilizada para completar campos específicos.
     * @return una instancia de {@code Asignatura} poblada con los parámetros proporcionados y detalles de {@code asignaturaEncontrada}.
     */
    private Asignatura getAsignatura(String cursoAcademico, Integer curso, String etapa, String nombreAsignatura, Optional<Asignatura> asignaturaEncontrada)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
        idCursoEtapaGrupo.setCursoAcademico(cursoAcademico);
        idCursoEtapaGrupo.setCurso(curso);
        idCursoEtapaGrupo.setEtapa(etapa);
        idCursoEtapaGrupo.setGrupo(Constants.SIN_GRUPO_ASIGNADO);

        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

        IdAsignatura idAsignatura = new IdAsignatura();
        idAsignatura.setNombre(nombreAsignatura);
        idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);

        Asignatura asignatura = new Asignatura();
        asignatura.setIdAsignatura(idAsignatura);
        asignatura.setHoras(asignaturaEncontrada.get().getHoras());
        asignatura.setBloqueId(asignaturaEncontrada.get().getBloqueId());
        asignatura.setSinDocencia(asignaturaEncontrada.get().isSinDocencia());
        asignatura.setDesdoble(asignaturaEncontrada.get().isDesdoble());

        return asignatura;
    }
}
