package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.*;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.services.CursoEtapaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/crearGrupos")
public class Paso3CrearGruposController
{
    @Autowired
    private ICursoEtapaRepository iCursoEtapaRepository;

    @Autowired
    private CursoEtapaService cursoEtapaService;

    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    @Autowired
    private IAlumnoRepository iAlumnoRepository;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private IMatriculaRepository iMatriculaRepository;

    @Autowired
    private IImpartirRepository iImpartirRepository;

    /**
     * Crea un nuevo grupo para un curso y etapa determinados.
     * <p>
     * El método valida la existencia del curso y etapa, calcula el identificador del grupo
     * asignando una letra en función del número de grupos ya existentes para esa combinación,
     * establece valores por defecto y guarda el nuevo grupo en la base de datos.
     *
     * @param curso el identificador del curso, enviado en la cabecera de la solicitud (HTTP header).
     * @param etapa la etapa educativa, enviada en la cabecera de la solicitud (HTTP header).
     * @return una {@link ResponseEntity} que contiene:
     *         - 200 (OK) si el grupo se crea correctamente.
     *         - 400 (BAD_REQUEST) si la validación del curso o etapa falla.
     *         - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la creación del grupo.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/grupos")
    public ResponseEntity<?> crearGrupo(@RequestHeader(value = "curso", required = true) Integer curso,
                                        @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            // Validamos y obtenemos el curso y etapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Numero de veces repetido el Curso Etapa en la BD
            int contador = this.iCursoEtapaGrupoRepository.cuentaCursoEtapaGruposCreados(curso, etapa);

            // Asignar la letra A
            String grupo = Constants.GRUPO_INICIAL;

            // Asignar la letra según el numero de veces que este repetido en BD
            for (int i = 0; i < contador; i++)
            {
                grupo = incrementarGrupo(grupo);
            }

            // Crear registro de Curso Etapa Grupo
            CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();

            // Crear campo de id con todos los campos
            IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(curso, etapa, grupo);

            // Asignar el id al registro de Curso Etapa
            cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

            // Asignamos por defecto el horario matutino a true
            cursoEtapaGrupo.setHorarioMatutino(true);

            // Indicamos si es ESO o Bachillerato
            cursoEtapaGrupo.setEsoBachillerato(cursoEtapa.isEsoBachillerato());

            // Insertar en BD
            this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

            // Log de información antes de la respuesta
            log.info("INFO - Grupo creado correctamente para el curso: {} y etapa: {}", curso, etapa);

            // Devolver la respuesta indicando que el grupo ha sido creado correctamente
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo crear el grupo";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene la lista de alumnos pertenecientes a un curso, etapa y grupo específicos.
     * <p>
     * La información devuelta incluye el nombre, apellidos y grupo de cada alumno.
     * Los datos se obtienen consultando las matrículas asociadas a los parámetros proporcionados.
     *
     * @param curso el identificador del curso, enviado en la cabecera de la solicitud (HTTP header).
     * @param etapa la etapa educativa, enviada en la cabecera de la solicitud (HTTP header).
     * @param grupo la letra que identifica el grupo dentro del curso y etapa, enviada en la cabecera (HTTP header).
     * @return una {@link ResponseEntity} que contiene:
     *         - una lista de {@link AlumnoDto2} si la operación es exitosa.
     *         - 500 (INTERNAL_SERVER_ERROR) si ocurre un error durante la consulta.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/gruposAlumnos")
    public ResponseEntity<?> obtenerAlumnosConGrupo(@RequestHeader(value = "curso", required = true) Integer curso,
                                                    @RequestHeader(value = "etapa", required = true) String etapa,
                                                    @RequestHeader(value = "grupo", required = true) String grupo)
    {
        try
        {
            // Crear la lista de Alumnos a devolver
            List<Integer> idsDeAlumnosDelGrupo = this.iMatriculaRepository.encontrarIdAlumnoPorCursoEtapaYGrupo(curso, etapa, grupo);

            List<AlumnoDto2> alumnosEnGrupo = new ArrayList<>();

            for (Integer idAlumno : idsDeAlumnosDelGrupo)
            {
                AlumnoDto2 alumnoDto2 = new AlumnoDto2();
                Optional<Alumno> alumnoEncontrado = this.iAlumnoRepository.findById(idAlumno);
                if (alumnoEncontrado.isPresent())
                {
                    alumnoDto2.setNombre(alumnoEncontrado.get().getNombre());
                    alumnoDto2.setApellidos(alumnoEncontrado.get().getApellidos());
                    alumnoDto2.setGrupo(grupo);
                }
                alumnosEnGrupo.add(alumnoDto2);
            }

            // Log de información antes de la respuesta
            log.info("INFO - Lista con nombres y apellidos de los alumnos asignados y pendientes de asignar");

            // Devolver la lista de Alumnos
            return ResponseEntity.ok().body(alumnosEnGrupo);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo obtener la lista de alumnos";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene la lista de alumnos que no están asignados a ningún grupo,
     * filtrando por curso y etapa educativa específicos.
     * <p>
     * La información se recupera a partir de las matrículas registradas para los alumnos
     * que aún no han sido organizados en grupos.
     *
     * @param curso el identificador del curso, enviado en la cabecera de la solicitud (HTTP header).
     * @param etapa la etapa educativa, enviada en la cabecera de la solicitud (HTTP header).
     * @return una {@link ResponseEntity} que contiene:
     *         - una lista de {@link AlumnoDto3} si se encuentran alumnos sin grupo asignado.
     *         - 404 (NOT_FOUND) si no se encuentran alumnos para el curso y etapa indicados.
     *         - 500 (INTERNAL_SERVER_ERROR) si ocurre un error durante el procesamiento de la solicitud.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/gruposAlumnosTotales")
    public ResponseEntity<?> obtenerAlumnosSinGrupos(@RequestHeader(value = "curso", required = true) Integer curso,
                                                     @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            List<AlumnoDto3> listaDatosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAlumnosByCursoEtapa(curso, etapa);

            if (listaDatosBrutoAlumnoMatriculas.isEmpty())
            {
                String mensajeError = "No se ha alumnos para " + curso + " " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_ALUMNOS_ENCONTRADOS, mensajeError);
            }
            return ResponseEntity.ok().body(listaDatosBrutoAlumnoMatriculas);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo obtener la lista de alumnos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna una lista de alumnos a un grupo específico dentro de un curso y etapa determinados.
     * <p>
     * El método valida la existencia del curso y la etapa, busca los datos de matrícula asociados a cada alumno
     * (por nombre y apellidos), y registra la información del alumno, la asignatura correspondiente
     * y la matrícula en base a los datos encontrados.
     *
     * @param alumnos lista de objetos {@link AlumnoDto2} con los datos de los alumnos que se desean asignar.
     * @param curso el identificador del curso (enviado en la cabecera HTTP) al que pertenecen los alumnos.
     * @param etapa la etapa educativa (enviada en la cabecera HTTP) correspondiente al curso.
     * @param grupo el identificador del grupo (enviado en la cabecera HTTP) al que se asignarán los alumnos.
     * @return un objeto {@link ResponseEntity} con:
     *         - HTTP 200 OK si la asignación se realiza correctamente.
     *         - HTTP 400 BAD REQUEST con un mensaje de error si ocurre una excepción controlada.
     *         - HTTP 500 INTERNAL SERVER ERROR con un mensaje de error si se produce una excepción inesperada.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/gruposAlumnos")
    public ResponseEntity<?> asignarAlumnos(@RequestBody List<AlumnoDto2> alumnos,
                                            @RequestHeader(value = "curso", required = true) Integer curso,
                                            @RequestHeader(value = "etapa", required = true) String etapa,
                                            @RequestHeader(value = "grupo", required = true) String grupo)
    {
        try
        {
            // Validamos y obtenemos el curso y etapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Por cada alumno proporcionado en la lista de entrada se buscan en el repo de DatosBrutosAlumnoMatricula
            for (AlumnoDto2 alumnoDatosBrutos : alumnos)
            {

                // Buscar los registros del alumno en DatosBrutosAlumnoMatricula
                List<DatosBrutoAlumnoMatricula> datosBrutoAlumnoMatriculaAsignaturasOpt =
                        this.iDatosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(alumnoDatosBrutos.getNombre(), alumnoDatosBrutos.getApellidos(), cursoEtapa);

                for (DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaAsignaturaOpt : datosBrutoAlumnoMatriculaAsignaturasOpt)
                {
                    if (datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals(Constants.ESTADO_MATRICULADO) || datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals(Constants.ESTADO_PENDIENTE))
                    {
                        // Registramos el alumno
                        Alumno alumno = this.asignarAlumnosRegistrarAlumno(curso, etapa, grupo, alumnoDatosBrutos.getNombre(), alumnoDatosBrutos.getApellidos());

                        // Registramos la asignatura
                        Asignatura asignatura = this.asignarAlumnosRegistrarAsignatura(curso, etapa, grupo, datosBrutoAlumnoMatriculaAsignaturaOpt.getAsignatura(), cursoEtapa.isEsoBachillerato());

                        // Registramos la matricula
                        this.asignarAlumnosRegistrarMatricula(datosBrutoAlumnoMatriculaAsignaturaOpt, alumno, asignatura);
                    }
                }
            }

            // Log de información antes de la respuesta
            log.info("INFO - Alumnos asignados correctamente al grupo {} para el curso {} y etapa {}", grupo, curso, etapa);

            // Devolver mensaje de OK
            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo asignar los alumnos al grupo";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina la asignación de un alumno a un grupo, según los datos proporcionados en un objeto {@link AlumnoDto2}.
     * <p>
     * Este método está restringido a usuarios con el rol de dirección. Utiliza el servicio {@code alumnoService}
     * para llevar a cabo el proceso de eliminación. Si la operación se realiza correctamente, el alumno
     * se desasigna del grupo y se eliminan los registros correspondientes.
     *
     * @param alumnoDto objeto {@link AlumnoDto2} que contiene los datos del alumno que se desea desasignar del grupo.
     * @return un objeto {@link ResponseEntity} con:
     *         - HTTP 200 OK si el alumno se ha desasignado correctamente.
     *         - HTTP 400 BAD REQUEST si ocurre una {@link SchoolManagerServerException} controlada.
     *         - HTTP 500 INTERNAL SERVER ERROR si se produce una excepción inesperada durante la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/gruposAlumnos")
    public ResponseEntity<?> borrarAlumno(@RequestBody AlumnoDto2 alumnoDto)
    {
        try
        {
//            TODO: Hacer que se borren los grupos de optativas
            List<MatriculaDto> listaAlumnosABorrar = iMatriculaRepository.encontrarAlumnoPorNombreYApellidosYGrupo(alumnoDto.getNombre(), alumnoDto.getApellidos(), alumnoDto.getGrupo());
            List<Integer> idAlumnos = new ArrayList<>();

            if (listaAlumnosABorrar.isEmpty())
            {
                String mensajeError = "No se encontraron alumnos para borrar";

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_ALUMNOS_ENCONTRADOS, mensajeError);
            }

            // Por cada matricula del Alumno
            for (MatriculaDto matriculaDtoAlumnoABorrar : listaAlumnosABorrar)
            {
                List<Integer> listaIds = iMatriculaRepository.encontrarIdAlumnoPorCursoEtapaGrupoYNombre(matriculaDtoAlumnoABorrar.getCurso(),
                        matriculaDtoAlumnoABorrar.getEtapa(),
                        matriculaDtoAlumnoABorrar.getGrupo(),
                        matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                        matriculaDtoAlumnoABorrar.getNombreAlumno(),
                        matriculaDtoAlumnoABorrar.getApellidosAlumno());

                idAlumnos.add(listaIds.get(listaIds.size() - 1)); //Añado el id encontrado a la lista general

                for (Integer id : listaIds)
                {
                    if (this.iMatriculaRepository.numeroAlumnos() == 1)
                    {
//                      Comprobamos si la asignatura que está asociada al alumno está asignada a un profesor
                        if (!iImpartirRepository.encontrarAsignaturaImpartidaPorNombreAndCursoEtpa(matriculaDtoAlumnoABorrar.getNombreAsignatura(), matriculaDtoAlumnoABorrar.getCurso(),
                                matriculaDtoAlumnoABorrar.getEtapa()).isEmpty())
                        {
                            String mensajeError = "No se puede borrar el alumno ya que hay asignaturas asignadas a profesores";
                            log.error(mensajeError);
                            throw new SchoolManagerServerException(Constants.ASIGNATURA_ASIGNADA_A_PROFESOR, mensajeError);
                        }
                    }
//                  Eliminar el registro en la tabla Matricula
                    this.iMatriculaRepository.borrarPorTodo(matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa(),
                            matriculaDtoAlumnoABorrar.getNombreAsignatura(), id);
                }

                //Si la matricula actual de asignatura del alumno es la unica de su grupo
                if (this.iMatriculaRepository.numeroAsignaturasPorNombreYGrupo(
                        matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                        matriculaDtoAlumnoABorrar.getCurso(),
                        matriculaDtoAlumnoABorrar.getEtapa(),
                        matriculaDtoAlumnoABorrar.getGrupo()) == 0) {

                    Optional<Asignatura> asignaturaEncontrada = iAsignaturaRepository
                            .encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(
                                    matriculaDtoAlumnoABorrar.getCurso(),
                                    matriculaDtoAlumnoABorrar.getEtapa(),
                                    matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                                    matriculaDtoAlumnoABorrar.getGrupo());

                    // Primero borramos la asignatura actual
                    if (asignaturaEncontrada.isPresent()) {
                        iAsignaturaRepository.delete(asignaturaEncontrada.get());

                        // Verificar si quedan más grupos con esta asignatura
                        Long gruposRestantes = iAsignaturaRepository.contarGruposPorAsignatura(
                                matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                                matriculaDtoAlumnoABorrar.getCurso(),
                                matriculaDtoAlumnoABorrar.getEtapa());

                        // Solo si no quedan más grupos, creamos la asignatura sin grupo
                        if (gruposRestantes == 0) {
                            // Creo la asignatura sin el grupo
                            Asignatura asignatura = getAsignatura(matriculaDtoAlumnoABorrar, asignaturaEncontrada);
                            this.iAsignaturaRepository.saveAndFlush(asignatura);
                        }
                    }
                }

                //Convertir a false el campo asignacion de los alumnos borrados
                IdCursoEtapa idCursoEtapa = new IdCursoEtapa(matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa());
                CursoEtapa cursoEtapa = new CursoEtapa(idCursoEtapa, matriculaDtoAlumnoABorrar.isEsoBachillerato());

                List<DatosBrutoAlumnoMatricula> datosBrutoAlumnoMatricula =
                        this.iDatosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(matriculaDtoAlumnoABorrar.getNombreAlumno(), matriculaDtoAlumnoABorrar.getApellidosAlumno(), cursoEtapa);

                for (DatosBrutoAlumnoMatricula datosAlumnoBorrado : datosBrutoAlumnoMatricula)
                {
                    datosAlumnoBorrado.setAsignado(false);
                }

                // Guardar el registro en la tabla DatosBrutoAlumnoMatricula
                this.iDatosBrutoAlumnoMatriculaRepository.saveAllAndFlush(datosBrutoAlumnoMatricula);
            }
            List<Integer> listaIdsSinDuplicados = idAlumnos.stream()
                    .distinct()
                    .toList();
            for (Integer id : listaIdsSinDuplicados)
            {
                iAlumnoRepository.deleteByNombreAndApellidosAndId(alumnoDto.getNombre(), alumnoDto.getApellidos(), id);
            }

            // Log de información antes de la respuesta
            log.info("INFO - Alumno desasignado correctamente");

            // Devolver mensaje de OK
            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            if (schoolManagerServerException.getCode() == Constants.SIN_ALUMNOS_ENCONTRADOS)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo borrar el alumno";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    public static Asignatura getAsignatura(MatriculaDto matriculaDtoAlumnoABorrar, Optional<Asignatura> asignaturaEncontrada)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
        idCursoEtapaGrupo.setCurso(matriculaDtoAlumnoABorrar.getCurso());
        idCursoEtapaGrupo.setEtapa(matriculaDtoAlumnoABorrar.getEtapa());
        idCursoEtapaGrupo.setGrupo(Constants.SIN_GRUPO_ASIGNADO);

        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

        IdAsignatura idAsignatura = new IdAsignatura();
        idAsignatura.setNombre(matriculaDtoAlumnoABorrar.getNombreAsignatura());
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
     * Actualiza el turno horario (mañana o tarde) para un grupo específico dentro de un curso y etapa determinados.
     * <p>
     * Este método permite modificar la propiedad {@code esHorarioMatutino} de un grupo,
     * identificando de forma única dicho grupo por su curso, etapa y letra.
     * La operación está restringida a usuarios con rol de dirección.
     *
     * @param curso identificador del curso. Obligatorio.
     * @param etapa etapa educativa del grupo. Obligatoria.
     * @param grupo identificador del grupo dentro del curso y etapa. Obligatorio.
     * @param esHorarioMatutino indica si el grupo tiene turno de mañana ({@code true}) o no ({@code false}). Obligatorio.
     * @return un objeto {@link ResponseEntity} con:
     *         - HTTP 200 OK si el turno horario se actualizó correctamente.
     *         - HTTP 400 BAD REQUEST si no se encontró el grupo indicado.
     *         - HTTP 500 INTERNAL SERVER ERROR si ocurrió un error inesperado durante la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/turnoHorario")
    public ResponseEntity<?> actualizarTurnoHorario(@RequestHeader(value = "curso", required = true) Integer curso,
                                                    @RequestHeader(value = "etapa", required = true) String etapa,
                                                    @RequestHeader(value = "grupo", required = true) String grupo,
                                                    @RequestHeader(value = "esHorarioMatutino", required = true) Boolean esHorarioMatutino)
    {
        try
        {
            Optional<CursoEtapaGrupo> cursoEtapaGrupoOptional = this.iCursoEtapaGrupoRepository.findById(new IdCursoEtapaGrupo(curso, etapa, grupo));

            if (cursoEtapaGrupoOptional.isEmpty())
            {
                String mensajeError = "ERROR en la actualización del turno horario - No se encontró el curso " + curso + " " + etapa + " " + grupo;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.CURSO_ETAPA_GRUPO_NO_ENCONTRADO, mensajeError);
            }

            CursoEtapaGrupo cursoEtapaGrupo = cursoEtapaGrupoOptional.get();

            // Actualizamos el turno horario
            cursoEtapaGrupo.setHorarioMatutino(esHorarioMatutino);

            // Guardamos el curso etapa grupo
            this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

            // Log de información antes de la respuesta
            log.info("INFO - Turno horario actualizado correctamente {} {} {} {}", curso, etapa, grupo, esHorarioMatutino);

            // Devolvemos mensaje de OK
            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo actualizar el turno horario";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Incrementa la letra del grupo (A -> B, B -> C, etc.)
     *
     * @param grupo letra actual del grupo
     * @return siguiente letra del grupo
     */
    private String incrementarGrupo(String grupo) {
        char letraGrupo = grupo.charAt(0);
        return String.valueOf((char) (letraGrupo + 1));
    }


    /**
     * Método para asignar alumnos y registrarlos en la tabla Alumno
     *
     * @param datosBrutoAlumnoMatriculaAsignaturaOpt - El objeto DatosBrutoAlumnoMatricula que contiene los datos del alumno a asignar
     * @return Alumno - El alumno asignado y registrado en la tabla Alumno
     */
    private Alumno asignarAlumnosRegistrarAlumno(int curso, String etapa, String grupo, String nombreAlumno, String apellidosAlumno)
    {
        // Creamos un nuevo Alumno
        Alumno alumno = null;

        // Buscamos el alumno primero a partir de la tabla matricula
        Optional<Alumno> optionalAlumno = this.iMatriculaRepository.buscarAlumnoPorCursoEtapaNombreApellidos(curso, etapa, grupo, nombreAlumno, apellidosAlumno);

        // Si existe, lo asignamos
        if (optionalAlumno.isPresent())
        {
            alumno = optionalAlumno.get();
        }
        else
        {
            Optional<Alumno> optionalAlumnoA = this.iAlumnoRepository.findByNombreAndApellidos(nombreAlumno, apellidosAlumno);

            if (optionalAlumnoA.isPresent())
            {
                alumno = optionalAlumnoA.get();
            }
            else
            {
                // Creamos un nuevo Alumno
                alumno = new Alumno();

                // Asignar cada uno de los campos
                alumno.setNombre(nombreAlumno);
                alumno.setApellidos(apellidosAlumno);

                // Guardar el registro en la tabla Alumno
                this.iAlumnoRepository.saveAndFlush(alumno);
            }
        }

        return alumno;
    }

    /**
     * Método para obtener la asignatura
     *
     * @param curso            - El identificador del curso para el cual se desea obtener la asignatura
     * @param etapa            - La etapa educativa asociada al curso para el cual se desea obtener la asignatura
     * @param grupo            - El grupo asociado al curso y etapa para el cual se desea obtener la asignatura
     * @param nombreAsignatura - El nombre de la asignatura para el cual se desea obtener la asignatura
     * @param horas            - Las horas de la asignatura
     * @param esoBachillerato  - Indica si es ESO o Bachillerato
     * @return Asignatura - La asignatura encontrada en la base de datos
     */
    private Asignatura asignarAlumnosRegistrarAsignatura(Integer curso, String etapa, String grupo, String nombreAsignatura, Boolean esoBachillerato)
    {
        Asignatura asignatura = new Asignatura();

        // Primero buscamos la asignatura existente primero por su nombre, curso, etapa y grupo
        Optional<Asignatura> optionalAsignatura = this.iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(curso, etapa, nombreAsignatura, grupo);

        // Si existe, la asignamos
        if (optionalAsignatura.isPresent())
        {
            // Obtenemos la asignatura
            asignatura = optionalAsignatura.get();
        }
        else
        {
            // Buscamos la asignatura por su nombre, curso, etapa y grupo 'Sin grupo'
            optionalAsignatura = this.iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(curso, etapa, nombreAsignatura, Constants.SIN_GRUPO_ASIGNADO);

            // Si existe, la asignamos
            if (optionalAsignatura.isPresent())
            {
                // Obtenemos la asignatura
                asignatura = optionalAsignatura.get();

                // La borramos
                this.iAsignaturaRepository.delete(asignatura);

//                Comprobamos si la asignatura está asignada a un bloque
                if (asignatura.getBloqueId() != null)
                {
                    if (this.iCursoEtapaGrupoRepository.buscarCursosEtapasGrupoOptativas(curso, etapa) == null)
                    {
                        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                        cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(curso, etapa, Constants.GRUPO_OPTATIVAS));
                        this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);
                    }

                    asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().setGrupo(Constants.GRUPO_OPTATIVAS);
                }
                else
                {
                    // Cambiamos el valor por el del grupo
                    asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().setGrupo(grupo);
                }

                // Guardamos la asignatura
                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }
            else // Si llegamos aquí, es porque no existe la asignatura
            {
                // Buscamos la asignatura para obtener las horas y los bloques
                Optional<HorasYBloquesDto> asignaturaExistente = this.iAsignaturaRepository.encontrarAsignaturaPorCursoEtapaNombre(curso, etapa, nombreAsignatura);

                // Creamos una instancia del la clave primaria de la asignatura
                IdAsignatura idAsignatura = new IdAsignatura();

                // Asignamos cada uno de los campos
                CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();

                if (asignaturaExistente.get().getBloques() != null)
                {
                    cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(curso, etapa, Constants.GRUPO_OPTATIVAS));
                }
                else
                {
                    cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(curso, etapa, grupo));
                }

                idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);
                idAsignatura.setNombre(nombreAsignatura);

                // Indicamos si es ESO o Bachillerato
                asignatura.setEsoBachillerato(esoBachillerato);

                // Asignamos la clave primaria a la asignatura
                asignatura.setIdAsignatura(idAsignatura);

                // Asignamos las horas, si tiene docencia y si tiene desdoble
                asignatura.setHoras(asignaturaExistente.get().getHoras());
                asignatura.setSinDocencia(asignaturaExistente.get().isSinDocencia());
                asignatura.setDesdoble(asignaturaExistente.get().isDesdoble());

                if (asignaturaExistente.get().getBloques() != null)
                {
                    // Creamos una instancia del bloque
                    Bloque bloque = new Bloque();
                    bloque.setId(asignaturaExistente.get().getBloques());

                    asignatura.setBloqueId(bloque);
                }

                // Guardamos la asignatura
                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }
        }

        return asignatura;
    }

    /**
     * Método para obtener la matricula
     *
     * @param datosBrutoAlumnoMatriculaAsignaturaOpt - El objeto DatosBrutoAlumnoMatricula que contiene los datos del alumno a asignar
     * @param alumno                                 - El alumno a asignar
     * @param asignatura                             - La asignatura a asignar
     */
    private void asignarAlumnosRegistrarMatricula(DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaAsignaturaOpt, Alumno alumno, Asignatura asignatura)
    {
        // Obtenemos la matricula
        IdMatricula idMatricula = new IdMatricula();
        idMatricula.setAsignatura(asignatura);
        idMatricula.setAlumno(alumno);

        Matricula matricula = new Matricula();
        matricula.setIdMatricula(idMatricula);

        datosBrutoAlumnoMatriculaAsignaturaOpt.setAsignado(true);

        this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(datosBrutoAlumnoMatriculaAsignaturaOpt);

        if (datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals(Constants.ESTADO_MATRICULADO) ||
                datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals(Constants.ESTADO_PENDIENTE))
        {
            // Guardar el registro en la tabla Matricula
            this.iMatriculaRepository.saveAndFlush(matricula);
        }
    }
}
