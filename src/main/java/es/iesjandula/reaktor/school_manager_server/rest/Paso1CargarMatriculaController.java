package es.iesjandula.reaktor.school_manager_server.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import es.iesjandula.reaktor.school_manager_server.dtos.*;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.interfaces.IParseoDatosBrutos;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
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
                String mensajeError = "No se han encontrado asignaturas para " + curso + etapa;

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
    public ResponseEntity<?> cargarMatriculas()
    {
        try
        {
            List<CursoEtapaDto> listCursoEtapa = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosMatriculaPorEtapaYCurso();

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
     * - 200 (OK) si la operación se realiza correctamente.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error durante el proceso de eliminación.
     */
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
                String mensajeError = "No se ha encontrado matriculas para " + curso + " " + etapa;

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

//          Obtenemos el curosEtapaGrupo y lo borramos
            List<CursoEtapaGrupoDto> cursoEtapaGrupoDto = this.iCursoEtapaGrupoRepository.encontrarCursoEtapaGruposCreados(curso, etapa);
            CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
            for (CursoEtapaGrupoDto cursoEtapaGrupoDtoABorrar : cursoEtapaGrupoDto)
            {
                IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(curso, etapa, cursoEtapaGrupoDtoABorrar.getGrupo());
                cursoEtapaGrupo.setEsoBachillerato(cursoEtapaGrupoDtoABorrar.getEsBachillerato());
                cursoEtapaGrupo.setHorarioMatutino(cursoEtapaGrupoDtoABorrar.getHorarioMatutino());
                cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

                this.iCursoEtapaGrupoRepository.delete(cursoEtapaGrupo);
            }

            Bloque bloque = new Bloque();
            for (Long cantidadBloques : bloques)
            {
                bloque.setId(cantidadBloques);
                this.iBloqueRepository.delete(bloque);
            }

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
            List<DatosMatriculaDto> listDatosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarDatosMatriculaPorCursoYEtapa(curso, etapa);

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
     * Actualiza el estado de matrícula de un alumno en una asignatura específica, dentro de un curso y etapa determinados.
     *
     * @param nombre     el nombre del alumno.
     * @param apellidos  los apellidos del alumno.
     * @param asignatura la asignatura relacionada con la matrícula.
     * @param curso      el curso académico correspondiente.
     * @param etapa      la etapa educativa correspondiente.
     * @param estado     el nuevo estado de la matrícula (por ejemplo, ACTIVO o INACTIVO).
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) si la actualización es exitosa.
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
//            TODO: cuando un alumno ya está asignado a un grupo y lo matriculas en usa asignatura optativa
            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(nombre, apellidos, asignatura, curso, etapa);

            if (datosBrutoAlumnoMatriculas == null)
            {
                String mensajeError = "El alumno " + nombre + apellidos + " no está matriculado en " + curso + etapa;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.MATRICULA_ALUMNO_NO_ENCONTRADA, mensajeError);
            }

//          Si el alumno ya está asignado a un grupo al cambiar el estado de la matrícula se añade o se borra
            if (this.iMatriculaRepository.encontrarAlumnoPorNombreAndApellidosAndCursoAndEtapa(nombre, apellidos, curso, etapa) != null)
            {
//              Si el estado de la matricula es MATR o PEND
                if (estado.equals(Constants.ESTADO_MATRICULADO) || estado.equals(Constants.ESTADO_PENDIENTE))
                {
                    if (this.iMatriculaRepository.encontrarMatriculaPorNombreAndApellidosAndCursoAndEtapa(nombre, apellidos, asignatura, curso, etapa) == null)
                    {
//                      Buscamos el alumno en base de datos
                        Optional<Alumno> alumno = this.iAlumnoRepository.findByNombreAndApellidos(nombre, apellidos);
                        if (alumno.isEmpty())
                        {
                            String mensajeError = "El alumno " + nombre + apellidos + " no existe en base de datos";
                            log.error(mensajeError);
                            throw new SchoolManagerServerException(Constants.ALUMNO_NO_ENCONTRADO, mensajeError);
                        }

//                      Buscamos el grupo al que pertenece el alumno
                        List<String> listaGrupos = this.iMatriculaRepository.encontrarGrupoPorNombreAndApellidosAndCursoAndEtapa(alumno.get().getNombre(), alumno.get().getApellidos(), curso, etapa);

                        if (listaGrupos.isEmpty())
                        {
                            String mensajeError = "El alumno no tiene grupo asignado";
                            log.error(mensajeError);
                            throw new SchoolManagerServerException(Constants.GRUPO_NO_ENCONTRADO, mensajeError);
                        }

                        String grupo = listaGrupos.stream().findFirst().get();

                        //Si la lista de grupos contiene entradas de optativas
                        if (listaGrupos.contains(Constants.GRUPO_OPTATIVAS)){

                           // Buscamos la asignatura en base de datos
                            Optional<Asignatura> asignaturaExistente = this.iAsignaturaRepository
                                    .encontrarAsignaturaPorNombreYCursoYEtapaYGrupoOptativas(curso, etapa, asignatura);
                            Bloque bloqueDeOptativas = new Bloque();
                            if (asignaturaExistente.isPresent()){
                                bloqueDeOptativas = asignaturaExistente.get().getBloqueId();
                            }

//                      Usamos la asignatura existente o crear una nueva si no existe
                            Bloque finalBloqueDeOptativas = bloqueDeOptativas;
                            Asignatura asignaturaParaMatricula = asignaturaExistente.orElseGet(() -> {
                                AsignaturaSinGrupoDto asignaturaABuscar = this.iAsignaturaRepository
                                        .encontrarPorCursoYEtapaYNombre(curso, etapa, asignatura);

                                if (asignaturaABuscar == null)
                                {
                                    String mensajeError = "No se encontró la asignatura " + asignatura +" para el curso " + curso + " y etapa " + etapa;
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
                                    curso,
                                    etapa,
                                    asignatura,
                                    Constants.SIN_GRUPO_ASIGNADO);  // Buscamos específicamente el grupo "sin grupo"

                            if (asignaturaGrupoZ.isPresent()) {
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
                        else{
//                      Buscamos la asignatura en base de datos
                        Optional<Asignatura> asignaturaExistente = this.iAsignaturaRepository
                                .encontrarAsignaturaPorNombreYCursoYEtapaYGrupoOSinEl(curso, etapa, asignatura, grupo);
                        Bloque bloqueDeOptativas = new Bloque();
                            if (asignaturaExistente.isPresent()){
                                bloqueDeOptativas = asignaturaExistente.get().getBloqueId();
                            }
//                      Usamos la asignatura existente o crear una nueva si no existe
                        Bloque finalBloqueDeOptativas = bloqueDeOptativas;
                        Asignatura asignaturaParaMatricula = asignaturaExistente.orElseGet(() -> {
                            AsignaturaSinGrupoDto asignaturaABuscar = this.iAsignaturaRepository
                                    .encontrarPorCursoYEtapaYNombre(curso, etapa, asignatura);

                            if (asignaturaABuscar == null)
                            {
                                String mensajeError = "No se encontró la asignatura " + asignatura +" para el curso " + curso + " y etapa " + etapa;
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
                            idCursoEtapaGrupo.setCurso(curso);
                            idCursoEtapaGrupo.setEtapa(etapa);

                            if(asignaturaABuscar.getIdBloque()!=null){
                                idCursoEtapaGrupo.setGrupo(Constants.GRUPO_OPTATIVAS);
                            }
                            else{
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

// 1) Recupero la fila antigua:
                            Asignatura vieja = asignaturaExistente.get();
// 2) Recupero el CursoEtapaGrupo “(curso, etapa, "A")” de BD:
                            CursoEtapaGrupo cegA;
                            if(vieja.isOptativa()){
                                 cegA = iCursoEtapaGrupoRepository
                                        .findById(new IdCursoEtapaGrupo(curso, etapa, Constants.GRUPO_OPTATIVAS))
                                        .orElseThrow();
                            }
                            else{
                                 cegA = iCursoEtapaGrupoRepository
                                        .findById(new IdCursoEtapaGrupo(curso, etapa, grupo))
                                        .orElseThrow();
                            }
// 3) Creo la nueva Asignatura con grupo="A":
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

// 4) Después, creo la Matricula sobre “copia”:
                            IdMatricula idMat = new IdMatricula(copia, alumno.get());
                            iMatriculaRepository.saveAndFlush(new Matricula(idMat));

// 5) Por último, si quiero eliminar la fila vieja ("sin grupo"), hago:
                            iAsignaturaRepository.delete(vieja);
                            iAsignaturaRepository.flush();

                        datosBrutoAlumnoMatriculas.setAsignado(true);
                        datosBrutoAlumnoMatriculas.setEstadoMatricula(estado);

                            //Primero buscamos si existe la asignatura con grupo "sin grupo" y la borramos
                            Optional<Asignatura> asignaturaGrupoZ = iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(
                                    curso,
                                    etapa,
                                    asignatura,
                                    Constants.SIN_GRUPO_ASIGNADO);  // Buscamos específicamente el grupo "sin grupo"

                            if (asignaturaGrupoZ.isPresent()) {
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
                    Matricula matricula = this.iMatriculaRepository.encontrarMatriculaPorNombreAndApellidosAndCursoAndEtapa(nombre, apellidos, asignatura, curso, etapa);
                    if (matricula != null)
                    {
                        datosBrutoAlumnoMatriculas.setAsignado(false);
                        this.iMatriculaRepository.delete(matricula);
//                      Después de borrar confirmamos la operación
                        this.iMatriculaRepository.flush();

                        // Si la matricula actual de asignatura del alumno es la única de su grupo
                        if (this.iMatriculaRepository.numeroAsignaturasPorNombreYGrupo(
                                asignatura, curso, etapa,
                                matricula.getIdMatricula().getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getGrupo()) == 0)
                        {

                            Optional<Asignatura> asignaturaEncontrada = iAsignaturaRepository
                                    .encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(
                                            curso, etapa, asignatura,
                                            matricula.getIdMatricula().getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getGrupo());

                            // Primero borramos la asignatura actual
                            if (asignaturaEncontrada.isPresent())
                            {
                                iAsignaturaRepository.delete(asignaturaEncontrada.get());

                                // Verificar si quedan más grupos con esta asignatura
                                Long gruposRestantes = iAsignaturaRepository.contarGruposPorAsignatura(
                                        asignatura,
                                        curso,
                                        etapa);

                                // Solo si no quedan más grupos, creamos la asignatura sin grupo
                                if (gruposRestantes == 0)
                                {
                                    // Creo la asignatura sin el grupo
                                    Asignatura asignaturaSinGrupo = getAsignatura(
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

    private Asignatura getAsignatura(Integer curso, String etapa, String nombreAsignatura, Optional<Asignatura> asignaturaEncontrada)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
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
     * - 200 (OK) si la matrícula es realizada con éxito.
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

            DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(nombre, apellidos, asignatura, curso, etapa);

            if (datosBrutoAlumnoMatriculas != null)
            {
                String mensajeError = "El alumnos " + nombre + apellidos + " ya está matriculado en " + curso + etapa;

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
     * - 200 (OK) si la matrícula es eliminada con éxito.
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
}
