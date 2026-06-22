package es.iesjandula.reaktor.school_manager_server.rest.manager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoCursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto2;
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto3;
import es.iesjandula.reaktor.school_manager_server.dtos.BloqueAsignaturaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CargaCsvResultDto;
import es.iesjandula.reaktor.school_manager_server.dtos.BloqueConAsignaturasDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDesdobleAsignadoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDesdoblePeticionDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDisponibleDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioDto;
import es.iesjandula.reaktor.school_manager_server.dtos.EspacioFijoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.HorasYBloquesDto;
import es.iesjandula.reaktor.school_manager_server.dtos.MatriculaDto;
import es.iesjandula.reaktor.school_manager_server.models.Alumno;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Bloque;
import es.iesjandula.reaktor.school_manager_server.models.CursoAcademico;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;
import es.iesjandula.reaktor.school_manager_server.models.EspacioDesdoble;
import es.iesjandula.reaktor.school_manager_server.models.EspacioFijo;
import es.iesjandula.reaktor.school_manager_server.models.EspacioSinDocencia;
import es.iesjandula.reaktor.school_manager_server.models.Matricula;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacio;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacioDesdoble;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IBloqueRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoAcademicoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioDesdobleRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioFijoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioSinDocenciaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver;
import es.iesjandula.reaktor.school_manager_server.services.manager.CursoEtapaService;
import es.iesjandula.reaktor.school_manager_server.services.manager.ParseoCsvConfiguracionBasicaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/crearGrupos")
public class Paso3CrearGruposController
{
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

    /** Repositorio de cursos académicos (para validar el curso académico de los espacios). */
    @Autowired
    private ICursoAcademicoRepository iCursoAcademicoRepository;

    /** Repositorio de espacios fijos (aulas de referencia asignadas a un grupo). */
    @Autowired
    private IEspacioFijoRepository iEspacioFijoRepository;

    /** Repositorio de espacios sin docencia (catálogo de espacios disponibles del instituto). */
    @Autowired
    private IEspacioSinDocenciaRepository iEspacioSinDocenciaRepository;

    /** Repositorio de espacios desdoble. */
    @Autowired
    private IEspacioDesdobleRepository iEspacioDesdobleRepository;

    /** Repositorio de bloques (conjuntos de optativas). */
    @Autowired
    private IBloqueRepository iBloqueRepository;

    /** Repositorio de reducciones (para materializar las tutorías por grupo al crear grupos). */
    @Autowired
    private es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository iReduccionRepository;

    /** Resolutor del curso académico activo (seleccionado = true en BBDD). */
    @Autowired
    private CursoAcademicoResolver cursoAcademicoResolver;

    /** Servicio de carga por fichero CSV (reutilizado para la carga de alumnos de este paso). */
    @Autowired
    private ParseoCsvConfiguracionBasicaService parseoCsvConfiguracionBasicaService;

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
     * - una lista de {@link AlumnoDto2} si la operación es exitosa.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error durante la consulta.
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
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<Integer> idsDeAlumnosDelGrupo = this.iMatriculaRepository.encontrarIdAlumnoPorCursoEtapaYGrupo(cursoAcademico, curso, etapa, grupo);

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
     * - una lista de {@link AlumnoDto3} si se encuentran alumnos sin grupo asignado.
     * - 404 (NOT_FOUND) si no se encuentran alumnos para el curso y etapa indicados.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error durante el procesamiento de la solicitud.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/gruposAlumnosTotales")
    public ResponseEntity<?> obtenerAlumnosSinGrupos(@RequestHeader(value = "curso", required = true) Integer curso,
                                                     @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<AlumnoDto3> listaDatosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAlumnosByCursoEtapa(cursoAcademico, curso, etapa);

            if (listaDatosBrutoAlumnoMatriculas.isEmpty())
            {
                String mensajeError = "No se han encontrado alumnos para " + curso + " " + etapa;

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
     * @param curso   el identificador del curso (enviado en la cabecera HTTP) al que pertenecen los alumnos.
     * @param etapa   la etapa educativa (enviada en la cabecera HTTP) correspondiente al curso.
     * @param grupo   el identificador del grupo (enviado en la cabecera HTTP) al que se asignarán los alumnos.
     * @return un objeto {@link ResponseEntity} con:
     * - HTTP 200 OK si la asignación se realiza correctamente.
     * - HTTP 400 BAD REQUEST con un mensaje de error si ocurre una excepción controlada.
     * - HTTP 500 INTERNAL SERVER ERROR con un mensaje de error si se produce una excepción inesperada.
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

            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            // Los grupos ya no se crean manualmente: se eligen de una lista fija A-H en el frontend. Por ello, al
            // asignar alumnos garantizamos que exista el CursoEtapaGrupo destino (creándolo si no existe).
            this.obtenerOCrearCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo, cursoEtapa.isEsoBachillerato());

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
                        Alumno alumno = this.asignarAlumnosRegistrarAlumno(cursoAcademico, curso, etapa, grupo, alumnoDatosBrutos.getNombre(), alumnoDatosBrutos.getApellidos());

                        // Registramos la asignatura
                        Asignatura asignatura = this.asignarAlumnosRegistrarAsignatura(cursoAcademico, curso, etapa, grupo, datosBrutoAlumnoMatriculaAsignaturaOpt.getAsignatura(), cursoEtapa.isEsoBachillerato());

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
     * - HTTP 200 OK si el alumno se ha desasignado correctamente.
     * - HTTP 400 BAD REQUEST si ocurre una {@link SchoolManagerServerException} controlada.
     * - HTTP 500 INTERNAL SERVER ERROR si se produce una excepción inesperada durante la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/gruposAlumnos")
    public ResponseEntity<?> borrarAlumno(@RequestBody AlumnoDto2 alumnoDto)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<MatriculaDto> listaAlumnosABorrar = iMatriculaRepository.encontrarAlumnoPorNombreYApellidosYGrupo(cursoAcademico, alumnoDto.getNombre(), alumnoDto.getApellidos(), alumnoDto.getGrupo());
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
                List<Integer> listaIds = iMatriculaRepository.encontrarIdAlumnoPorCursoEtapaGrupoYNombre(cursoAcademico,
                        matriculaDtoAlumnoABorrar.getCurso(),
                        matriculaDtoAlumnoABorrar.getEtapa(),
                        matriculaDtoAlumnoABorrar.getGrupo(),
                        matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                        matriculaDtoAlumnoABorrar.getNombreAlumno(),
                        matriculaDtoAlumnoABorrar.getApellidosAlumno());

                idAlumnos.add(listaIds.get(listaIds.size() - 1)); //Añado el id encontrado a la lista general

                for (Integer id : listaIds)
                {
                    if (this.iMatriculaRepository.numeroAlumnos(cursoAcademico) == 1)
                    {
//                      Comprobamos si la asignatura que está asociada al alumno está asignada a un profesor
                        if (!iImpartirRepository.encontrarAsignaturaImpartidaPorNombreAndCursoEtpa(cursoAcademico, matriculaDtoAlumnoABorrar.getNombreAsignatura(), matriculaDtoAlumnoABorrar.getCurso(),
                                matriculaDtoAlumnoABorrar.getEtapa()).isEmpty())
                        {
                            String mensajeError = "No se puede borrar el alumno ya que hay asignaturas asignadas a profesores";
                            log.error(mensajeError);
                            throw new SchoolManagerServerException(Constants.ASIGNATURA_ASIGNADA_A_PROFESOR, mensajeError);
                        }
                    }
//                  Eliminar el registro en la tabla Matricula
                    this.iMatriculaRepository.borrarPorTodo(cursoAcademico, matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa(),
                            matriculaDtoAlumnoABorrar.getNombreAsignatura(), id);
                }

                //Si la matricula actual de asignatura del alumno es la unica de su grupo
                if (this.iMatriculaRepository.numeroAsignaturasPorNombreYGrupo(
                        cursoAcademico,
                        matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                        matriculaDtoAlumnoABorrar.getCurso(),
                        matriculaDtoAlumnoABorrar.getEtapa(),
                        matriculaDtoAlumnoABorrar.getGrupo()) == 0)
                {

                    Optional<Asignatura> asignaturaEncontrada = iAsignaturaRepository
                            .encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(
                                    cursoAcademico,
                                    matriculaDtoAlumnoABorrar.getCurso(),
                                    matriculaDtoAlumnoABorrar.getEtapa(),
                                    matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                                    matriculaDtoAlumnoABorrar.getGrupo());

                    // Primero borramos la asignatura actual
                    if (asignaturaEncontrada.isPresent())
                    {
                        iAsignaturaRepository.delete(asignaturaEncontrada.get());

                        // Verificar si quedan más grupos con esta asignatura
                        Long gruposRestantes = iAsignaturaRepository.contarGruposPorAsignatura(
                                cursoAcademico,
                                matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                                matriculaDtoAlumnoABorrar.getCurso(),
                                matriculaDtoAlumnoABorrar.getEtapa());

                        // Solo si no quedan más grupos, creamos la asignatura sin grupo
                        if (gruposRestantes == 0)
                        {
                            // Creo la asignatura sin el grupo
                            Asignatura asignatura = getAsignatura(cursoAcademico, matriculaDtoAlumnoABorrar, asignaturaEncontrada);
                            this.iAsignaturaRepository.saveAndFlush(asignatura);
                        }
                    }
                }

                //Convertir a false el campo asignacion de los alumnos borrados
                IdCursoEtapa idCursoEtapa = new IdCursoEtapa(cursoAcademico, matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa());
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

    /**
     * Construye y devuelve una instancia de {@link Asignatura} a partir de los datos proporcionados
     * en un {@link MatriculaDto} y, opcionalmente, una asignatura existente.
     *
     * @param matriculaDtoAlumnoABorrar DTO que contiene información sobre curso, etapa, grupo y
     *                                  nombre de la asignatura.
     * @param asignaturaEncontrada      {@link Optional} que puede contener una asignatura ya
     *                                  existente, utilizada como referencia para completar datos
     *                                  adicionales.
     * @return una nueva instancia de {@link Asignatura} con los datos combinados del DTO y, si
     * está presente, de la asignatura encontrada.
     */
    public static Asignatura getAsignatura(String cursoAcademico, MatriculaDto matriculaDtoAlumnoABorrar, Optional<Asignatura> asignaturaEncontrada)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
        idCursoEtapaGrupo.setCursoAcademico(cursoAcademico);
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
     * @param curso             identificador del curso. Obligatorio.
     * @param etapa             etapa educativa del grupo. Obligatoria.
     * @param grupo             identificador del grupo dentro del curso y etapa. Obligatorio.
     * @param esHorarioMatutino indica si el grupo tiene turno de mañana ({@code true}) o no ({@code false}). Obligatorio.
     * @return un objeto {@link ResponseEntity} con:
     * - HTTP 200 OK si el turno horario se actualizó correctamente.
     * - HTTP 400 BAD REQUEST si no se encontró el grupo indicado.
     * - HTTP 500 INTERNAL SERVER ERROR si ocurrió un error inesperado durante la operación.
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
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            Optional<CursoEtapaGrupo> cursoEtapaGrupoOptional = this.iCursoEtapaGrupoRepository.findById(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo));

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

            // Debemos actualizar también aquel curso y etapa cuyo grupo sea "Optativas"
            Optional<CursoEtapaGrupo> cursoEtapaGrupoOptativasOptional = this.iCursoEtapaGrupoRepository.findById(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.GRUPO_OPTATIVAS));
            if (cursoEtapaGrupoOptativasOptional.isPresent())
            {
                // Obtenemos y actualizamos el horario matutino a true o false
                CursoEtapaGrupo cursoEtapaGrupoOptativas = cursoEtapaGrupoOptativasOptional.get();
                cursoEtapaGrupoOptativas.setHorarioMatutino(esHorarioMatutino);

                // Actualizamos la BBDD
                this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupoOptativas);
            }

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
     * Carga alumnos desde un fichero CSV (multipart, campo {@code csv}) para el curso académico activo
     * (seleccionado = true, resuelto internamente).
     * <p>
     * El CSV tiene 2 columnas con cabecera {@code nombreApellidos,cursoEtapaGrupo} (la primera fila es cabecera y
     * se ignora como dato). Cada fila da de alta a un alumno en su curso/etapa, de modo que aparezca en la lista de
     * "Alumnos disponibles" de esta vista. La carga es idempotente (los alumnos ya existentes se omiten) y reutiliza
     * {@link ParseoCsvConfiguracionBasicaService}, {@code CsvParserUtil} y {@link CargaCsvResultDto}.
     *
     * @param archivoCsv el fichero CSV con los alumnos.
     * @return una {@link ResponseEntity} con el {@link CargaCsvResultDto} resumen de la carga.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/alumnos/csv", consumes = "multipart/form-data")
    public ResponseEntity<?> cargarAlumnosDesdeCsv(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv)
    {
        try
        {
            CargaCsvResultDto resultado = this.parseoCsvConfiguracionBasicaService.cargarAlumnosDesdeCsv(archivoCsv);

            log.info("INFO - Carga CSV de alumnos completada: {} creados, {} omitidos, {} procesados",
                    resultado.getCreados(), resultado.getOmitidos(), resultado.getProcesados());

            return ResponseEntity.ok(resultado);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo cargar el fichero CSV de alumnos";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Carga alumnos desde un fichero CSV (multipart, campo {@code csv}) y los ASIGNA a su grupo en "Creación de
     * grupos", para el curso académico activo (seleccionado = true, resuelto internamente).
     * <p>
     * El CSV tiene el formato {@code alumnos_cursoEtapaGrupo.csv}: 2 columnas con cabecera
     * {@code nombreApellidos,cursoEtapaGrupo} (la primera fila es cabecera y se ignora como dato), donde
     * {@code cursoEtapaGrupo} es "curso etapa grupo" (p. ej. "1 ESO A", el último término es el grupo).
     * <p>
     * Comportamiento:
     * <ol>
     *   <li>Se crean automáticamente los {@link CursoEtapaGrupo} que no existan para ese curso/etapa/grupo del curso
     *       académico activo y se asignan los alumnos a su grupo (reutilizando la lógica de {@code asignarAlumnos}).</li>
     *   <li>VALIDACIÓN: todos los alumnos del fichero deben existir en los datos brutos
     *       ({@link DatosBrutoAlumnoMatricula}) del curso/etapa/curso académico. Si alguno NO existe, se ABORTA toda
     *       la importación (transaccional, no se persiste nada) y se devuelve un 400 cuyo mensaje incluye el nombre y
     *       apellidos de los alumnos no encontrados.</li>
     * </ol>
     * La operación es {@code @Transactional}: cualquier fallo revierte todos los cambios.
     *
     * @param archivoCsv el fichero CSV con los alumnos y su curso/etapa/grupo.
     * @return una {@link ResponseEntity} con el {@link CargaCsvResultDto} resumen, o un 400 con los alumnos no encontrados.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @Transactional
    @RequestMapping(method = RequestMethod.POST, value = "/alumnos/asignarPorFichero", consumes = "multipart/form-data")
    public ResponseEntity<?> asignarAlumnosPorFichero(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            List<AlumnoCursoEtapaGrupoDto> filas = this.parseoCsvConfiguracionBasicaService.parsearAlumnosCursoEtapaGrupo(archivoCsv);

            // Caché de CursoEtapa por clave "curso|etapa" para no validar/consultar repetidamente
            Map<String, CursoEtapa> cursoEtapaCache = new LinkedHashMap<>();

            // 1) VALIDACIÓN previa: todos los alumnos del fichero deben existir en datos brutos. Si alguno no existe,
            //    se recopila (nombre + apellidos) y se aborta sin persistir nada.
            List<String> alumnosNoEncontrados = new ArrayList<>();

            for (AlumnoCursoEtapaGrupoDto fila : filas)
            {
                CursoEtapa cursoEtapa = this.obtenerCursoEtapaCacheado(cursoEtapaCache, fila.getCurso(), fila.getEtapa());

                List<DatosBrutoAlumnoMatricula> datosAlumno =
                        this.iDatosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(fila.getNombre(), fila.getApellidos(), cursoEtapa);

                if (datosAlumno.isEmpty())
                {
                    alumnosNoEncontrados.add(fila.getNombre() + " " + fila.getApellidos() + " (" + fila.getCurso() + " " + fila.getEtapa() + ")");
                }
            }

            if (!alumnosNoEncontrados.isEmpty())
            {
                String mensajeError = Constants.ERR_ALUMNOS_NO_EN_DATOS_BRUTOS_MESSAGE + String.join("; ", alumnosNoEncontrados);
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERR_ALUMNOS_NO_EN_DATOS_BRUTOS_CODE, mensajeError);
            }

            // 2) ASIGNACIÓN: autocreación de grupos y registro de alumno/asignatura/matrícula por cada fila
            int alumnosProcesados = 0;

            for (AlumnoCursoEtapaGrupoDto fila : filas)
            {
                CursoEtapa cursoEtapa = this.obtenerCursoEtapaCacheado(cursoEtapaCache, fila.getCurso(), fila.getEtapa());

                // Garantizamos que el grupo destino exista (creándolo si no existe)
                this.obtenerOCrearCursoEtapaGrupo(cursoAcademico, fila.getCurso(), fila.getEtapa(), fila.getGrupo(), cursoEtapa.isEsoBachillerato());

                List<DatosBrutoAlumnoMatricula> datosAlumno =
                        this.iDatosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(fila.getNombre(), fila.getApellidos(), cursoEtapa);

                for (DatosBrutoAlumnoMatricula datosBruto : datosAlumno)
                {
                    if (datosBruto.getEstadoMatricula().equals(Constants.ESTADO_MATRICULADO) || datosBruto.getEstadoMatricula().equals(Constants.ESTADO_PENDIENTE))
                    {
                        Alumno alumno = this.asignarAlumnosRegistrarAlumno(cursoAcademico, fila.getCurso(), fila.getEtapa(), fila.getGrupo(), fila.getNombre(), fila.getApellidos());

                        Asignatura asignatura = this.asignarAlumnosRegistrarAsignatura(cursoAcademico, fila.getCurso(), fila.getEtapa(), fila.getGrupo(), datosBruto.getAsignatura(), cursoEtapa.isEsoBachillerato());

                        this.asignarAlumnosRegistrarMatricula(datosBruto, alumno, asignatura);
                    }
                }

                alumnosProcesados++;
            }

            CargaCsvResultDto resultado = new CargaCsvResultDto();
            resultado.setProcesados(filas.size());
            resultado.setCreados(alumnosProcesados);

            log.info("INFO - Carga por fichero de alumnos por grupo completada para {}: {} alumnos asignados", cursoAcademico, alumnosProcesados);

            return ResponseEntity.ok(resultado);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo asignar los alumnos por fichero a sus grupos";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene (y cachea) el {@link CursoEtapa} del curso académico activo validando su existencia. Se usa en la
     * carga por fichero de alumnos para no validar/consultar repetidamente el mismo curso/etapa.
     *
     * @param cache  caché por clave "curso|etapa".
     * @param curso  el curso.
     * @param etapa  la etapa.
     * @return el {@link CursoEtapa} validado.
     * @throws SchoolManagerServerException si el curso/etapa no existe.
     */
    private CursoEtapa obtenerCursoEtapaCacheado(Map<String, CursoEtapa> cache, Integer curso, String etapa) throws SchoolManagerServerException
    {
        String clave = curso + "|" + etapa;

        CursoEtapa cursoEtapa = cache.get(clave);

        if (cursoEtapa == null)
        {
            cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);
            cache.put(clave, cursoEtapa);
        }

        return cursoEtapa;
    }

    /**
     * Asigna un espacio del catálogo del instituto (aula de referencia) a un curso, etapa y grupo concretos.
     * <p>
     * El espacio debe existir previamente en el catálogo (creado en la ventana de configuración básica). Al
     * asignarlo, el espacio pasa a ser un espacio fijo vinculado al grupo y se elimina del catálogo de espacios
     * disponibles (sin docencia) y de desdoble si existiera. El curso académico se resuelve internamente
     * (seleccionado = true), el cliente no lo envía.
     *
     * @param espacioFijoDto - El DTO con el nombre del espacio y el curso, etapa y grupo de destino.
     * @return una {@link ResponseEntity} con el resultado de la operación.
     */
    @Transactional
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/espacios/fijo", consumes = "application/json")
    public ResponseEntity<?> crearEspacioFijo(@RequestBody EspacioFijoDto espacioFijoDto)
    {
        try
        {
            // El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
            espacioFijoDto.setCursoAcademico(this.cursoAcademicoResolver.resolver());

            // Validamos el DTO del espacio
            this.validarEspacioDto(espacioFijoDto);

            // Validamos que tenga curso, etapa y grupo
            if (espacioFijoDto.getCurso() == null || espacioFijoDto.getEtapa() == null || espacioFijoDto.getGrupo() == null)
            {
                log.error(Constants.ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_CODE, Constants.ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_MESSAGE);
            }

            IdEspacio idEspacio = new IdEspacio(espacioFijoDto.getCursoAcademico(), espacioFijoDto.getNombre());

            // Un aula que se usa como desdoble SÍ puede asignarse a la vez como aula de referencia (fijo): caso real
            // de un grupo que en las optativas no sale de su aula de referencia (mientras el resto de grupos van a las
            // suyas), de modo que esa aula es referencia del grupo y, además, desdoble del bloque. Por eso NO se
            // rechaza el alta de fijo aunque el aula ya esté usada como desdoble.

            IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(espacioFijoDto.getCursoAcademico(), espacioFijoDto.getCurso(), espacioFijoDto.getEtapa(), espacioFijoDto.getGrupo());

            Optional<CursoEtapaGrupo> cursoEtapaGrupoOptional = this.iCursoEtapaGrupoRepository.findById(idCursoEtapaGrupo);

            if (cursoEtapaGrupoOptional.isEmpty())
            {
                log.error(Constants.ERR_ESPACIO_FIJO_GRUPO_NO_EXISTE_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_FIJO_GRUPO_NO_EXISTE_CODE, Constants.ERR_ESPACIO_FIJO_GRUPO_NO_EXISTE_MESSAGE);
            }

            CursoEtapaGrupo cursoEtapaGrupo = cursoEtapaGrupoOptional.get();

            // SWAP (lógica transaccional): el aula de referencia es única por curso/etapa/grupo. Si el grupo destino
            // ya tenía asignado un espacio distinto, eliminamos esa asociación previa y devolvemos ese espacio al
            // catálogo de disponibles (sin docencia) antes de insertar la nueva asignación.
            List<EspacioFijo> fijosDelGrupo = this.iEspacioFijoRepository.buscarPorCursoEtapaGrupo(
                    espacioFijoDto.getCursoAcademico(), espacioFijoDto.getCurso(), espacioFijoDto.getEtapa(), espacioFijoDto.getGrupo());

            for (EspacioFijo fijoExistente : fijosDelGrupo)
            {
                if (!fijoExistente.getEspacioId().getNombre().equals(espacioFijoDto.getNombre()))
                {
                    this.iEspacioFijoRepository.delete(fijoExistente);

                    // Devolvemos el espacio anterior al catálogo de disponibles para que pueda reasignarse
                    if (!this.iEspacioSinDocenciaRepository.existsById(fijoExistente.getEspacioId()))
                    {
                        EspacioSinDocencia espacioDevuelto = new EspacioSinDocencia();
                        espacioDevuelto.setEspacioId(fijoExistente.getEspacioId());
                        this.iEspacioSinDocenciaRepository.saveAndFlush(espacioDevuelto);
                    }
                }
            }

            Optional<EspacioFijo> existente = this.iEspacioFijoRepository.findById(idEspacio);

            if (existente.isPresent())
            {
                // Actualizamos el grupo asociado al espacio fijo existente
                EspacioFijo espacio = existente.get();
                espacio.setCursoEtapaGrupo(cursoEtapaGrupo);
                this.iEspacioFijoRepository.saveAndFlush(espacio);
            }
            else
            {
                // Al pasar a ser aula de referencia (fijo), el aula se consume y sale del pool de disponibles
                // (sin docencia). El desdoble NO se toca aquí: si el aula ya estaba usada como desdoble de algún
                // bloque, esa relación (espacio ↔ bloque) se conserva, ya que un aula puede ser a la vez referencia
                // de un grupo y desdoble de un bloque.
                if (this.iEspacioSinDocenciaRepository.existsById(idEspacio))
                {
                    this.iEspacioSinDocenciaRepository.deleteById(idEspacio);
                }

                EspacioFijo espacio = new EspacioFijo();
                espacio.setEspacioId(idEspacio);
                espacio.setCursoEtapaGrupo(cursoEtapaGrupo);
                this.iEspacioFijoRepository.saveAndFlush(espacio);
            }

            log.info("INFO - Espacio fijo asignado correctamente al grupo {} {} {}", espacioFijoDto.getCurso(), espacioFijoDto.getEtapa(), espacioFijoDto.getGrupo());

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo asignar el espacio fijo";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene la lista de espacios fijos (aulas de referencia ya asignadas a un grupo) del curso académico activo.
     * <p>
     * El curso académico se resuelve internamente (seleccionado = true). El frontend filtra por el curso, etapa y
     * grupo seleccionados para mostrar los espacios ya asignados a ese grupo.
     *
     * @return una {@link ResponseEntity} con la lista de espacios fijos asignados.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/espacios/fijo")
    public ResponseEntity<?> obtenerEspaciosFijo()
    {
        try
        {
            // El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            List<EspacioFijoDto> espaciosFijoDto = this.iEspacioFijoRepository.buscarPorCursoAcademico(cursoAcademico);

            return ResponseEntity.ok(espaciosFijoDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener los espacios fijos";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene la lista de espacios DISPONIBLES del curso académico activo, es decir, los espacios del catálogo del
     * instituto (creados en la ventana de configuración básica) que todavía NO están asignados como aula de
     * referencia a ningún curso, etapa y grupo.
     * <p>
     * El curso académico se resuelve internamente (seleccionado = true); el cliente no lo envía.
     *
     * @return una {@link ResponseEntity} con la lista de espacios disponibles.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/espacios/disponibles")
    public ResponseEntity<?> obtenerEspaciosDisponibles()
    {
        try
        {
            // El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            List<EspacioDisponibleDto> espaciosDisponiblesDto = this.iEspacioSinDocenciaRepository.buscarDisponiblesPorCursoAcademico(cursoAcademico);

            return ResponseEntity.ok(espaciosDisponiblesDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener los espacios disponibles";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Desasigna un espacio fijo (aula de referencia) de su curso, etapa y grupo, devolviéndolo al catálogo de
     * espacios disponibles del instituto.
     * <p>
     * El espacio no se elimina del instituto: se borra de la tabla de espacios fijos y se vuelve a registrar como
     * espacio del catálogo (sin docencia), de modo que vuelve a estar disponible para una nueva asignación. El curso
     * académico se resuelve internamente (seleccionado = true); el cliente no lo envía.
     *
     * @param espacioFijoDto - El DTO con el nombre del espacio fijo a desasignar.
     * @return una {@link ResponseEntity} con el resultado de la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/espacios/fijo", consumes = "application/json")
    public ResponseEntity<?> borrarEspacioFijo(@RequestBody EspacioFijoDto espacioFijoDto)
    {
        try
        {
            // El curso académico activo se resuelve internamente (seleccionado = true); el cliente no lo envía
            espacioFijoDto.setCursoAcademico(this.cursoAcademicoResolver.resolver());

            // Validamos el DTO del espacio
            this.validarEspacioDto(espacioFijoDto);

            IdEspacio idEspacio = new IdEspacio(espacioFijoDto.getCursoAcademico(), espacioFijoDto.getNombre());

            if (!this.iEspacioFijoRepository.existsById(idEspacio))
            {
                log.error(Constants.ERR_ESPACIO_NO_EXISTE_EN_FIJO_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NO_EXISTE_EN_FIJO_CODE, Constants.ERR_ESPACIO_NO_EXISTE_EN_FIJO_MESSAGE);
            }

            // Desasignamos: lo quitamos de espacios fijos
            this.iEspacioFijoRepository.deleteById(idEspacio);

            // Y lo devolvemos al catálogo de espacios disponibles (sin docencia) para que pueda reasignarse
            if (!this.iEspacioSinDocenciaRepository.existsById(idEspacio))
            {
                EspacioSinDocencia espacio = new EspacioSinDocencia();
                espacio.setEspacioId(idEspacio);
                this.iEspacioSinDocenciaRepository.saveAndFlush(espacio);
            }

            log.info("INFO - Espacio fijo desasignado correctamente y devuelto al catálogo");

            return ResponseEntity.noContent().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo desasignar el espacio fijo";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene los bloques (conjuntos de optativas) del curso y etapa indicados, junto con las asignaturas que los
     * componen. El curso académico se resuelve internamente (seleccionado = true); el cliente no lo envía.
     * <p>
     * Se utiliza en la asignación de aulas de desdoble: dirección elige un bloque y ve las asignaturas asociadas
     * antes de asignarle un aula.
     *
     * @param curso - El curso (cabecera).
     * @param etapa - La etapa (cabecera).
     * @return una {@link ResponseEntity} con la lista de {@link BloqueConAsignaturasDto}.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/bloques")
    public ResponseEntity<?> obtenerBloquesConAsignaturas(@RequestHeader(value = "curso", required = true) Integer curso,
                                                          @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            List<BloqueAsignaturaDto> filas = this.iAsignaturaRepository.encontrarBloquesConAsignaturasPorCursoEtapa(cursoAcademico, curso, etapa);

            // Agrupamos las asignaturas por bloque, conservando el orden de aparición
            Map<Long, BloqueConAsignaturasDto> bloquesPorId = new LinkedHashMap<>();

            for (BloqueAsignaturaDto fila : filas)
            {
                BloqueConAsignaturasDto bloqueDto = bloquesPorId.computeIfAbsent(fila.getBloqueId(),
                        id -> new BloqueConAsignaturasDto(id, "Bloque " + id, new ArrayList<>()));

                bloqueDto.getAsignaturas().add(fila.getAsignatura());
            }

            return ResponseEntity.ok(new ArrayList<>(bloquesPorId.values()));
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener los bloques con sus asignaturas";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna un aula de desdoble (espacio del catálogo del instituto) a un bloque concreto.
     * <p>
     * IMPORTANTE: un aula de desdoble NO se consume. Como las optativas de bloques distintos pueden compartir aula,
     * asignar un aula como desdoble a un bloque NO la quita del catálogo ni del pool de disponibles: el mismo
     * espacio puede ser aula de desdoble de varios bloques. Por eso esta operación NO mueve el espacio de tabla:
     * únicamente añade la relación (espacio, bloque) en {@link EspacioDesdoble}, dejando el espacio en su sitio del
     * catálogo (sin docencia).
     * <p>
     * Regla de disponibilidad: solo se puede usar como desdoble un aula DISPONIBLE, es decir, presente en el
     * catálogo y NO ocupada como aula de referencia (fijo). Como al asignar un fijo el espacio sale de sin docencia,
     * exigir que el espacio exista en sin docencia equivale a "está en el catálogo y no es fijo". Así, un aula de
     * referencia no se ofrece como desdoble. El curso académico se resuelve internamente (seleccionado = true).
     *
     * @param peticionDto - El DTO con el nombre del espacio y el id del bloque de destino.
     * @return una {@link ResponseEntity} con el resultado de la operación.
     */
    @Transactional
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/espacios/desdoble", consumes = "application/json")
    public ResponseEntity<?> crearEspacioDesdoble(@RequestBody EspacioDesdoblePeticionDto peticionDto)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            // Validamos el curso académico
            this.validarCursoAcademico(cursoAcademico);

            // Validamos el nombre del espacio
            if (peticionDto.getNombre() == null || peticionDto.getNombre().isEmpty())
            {
                log.error(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_CODE, Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
            }

            // Validamos que se haya indicado el bloque y que exista
            if (peticionDto.getBloqueId() == null)
            {
                log.error(Constants.ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_CODE, "Debes indicar el bloque al que asignar el aula de desdoble");
            }

            if (!this.iBloqueRepository.existePorId(peticionDto.getBloqueId()))
            {
                log.error(Constants.ERR_ESPACIO_FIJO_GRUPO_NO_EXISTE_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_FIJO_GRUPO_NO_EXISTE_CODE, "El bloque indicado no existe");
            }

            // La asignación de desdoble es POR ASIGNATURA: validamos que se haya indicado la asignatura y que esta
            // pertenezca realmente al bloque. Así, cada asignatura del bloque puede tener su propia aula.
            String asignatura = peticionDto.getAsignatura();
            if (asignatura == null || asignatura.isEmpty())
            {
                log.error(Constants.ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_CODE, Constants.ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_MESSAGE);
            }

            if (!this.iAsignaturaRepository.existeAsignaturaEnBloque(peticionDto.getBloqueId(), asignatura))
            {
                log.error(Constants.ERR_DESDOBLE_ASIGNATURA_NO_EN_BLOQUE_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_DESDOBLE_ASIGNATURA_NO_EN_BLOQUE_CODE, Constants.ERR_DESDOBLE_ASIGNATURA_NO_EN_BLOQUE_MESSAGE);
            }

            IdEspacio idEspacio = new IdEspacio(cursoAcademico, peticionDto.getNombre());

            // El aula debe pertenecer al CATÁLOGO del instituto, es decir, estar en sin docencia (disponible) O ya
            // usada como aula de referencia (fijo). El uso como fijo NO consume el aula para desdoble: un mismo
            // espacio puede ser, a la vez, aula de referencia de un grupo y aula de desdoble de un bloque (caso real
            // de un grupo que en las optativas no sale de su aula). Por eso el catálogo se ofrece íntegro para
            // desdoble y solo se rechaza el aula que no exista en ninguna de las dos tablas (no está en el catálogo).
            boolean enCatalogo = this.iEspacioSinDocenciaRepository.existsById(idEspacio) || this.iEspacioFijoRepository.existsById(idEspacio);
            if (!enCatalogo)
            {
                log.error(Constants.ERR_ESPACIO_NO_DISPONIBLE_DESDOBLE_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NO_DISPONIBLE_DESDOBLE_CODE, Constants.ERR_ESPACIO_NO_DISPONIBLE_DESDOBLE_MESSAGE);
            }

            // Referencia al bloque (solo necesitamos su id para la clave foránea, igual que en el paso de bloques)
            Bloque bloque = new Bloque();
            bloque.setId(peticionDto.getBloqueId());

            // TOPE por asignatura (REEMPLAZO): cada asignatura del bloque admite como mucho un aula. Si la asignatura
            // ya tenía un aula distinta asignada, la eliminamos antes de insertar la nueva, de modo que nunca queden
            // dos aulas para la misma asignatura (y, por tanto, nunca más aulas que asignaturas tiene el bloque).
            List<EspacioDesdoble> previosDeLaAsignatura = this.iEspacioDesdobleRepository.buscarPorBloqueYAsignatura(cursoAcademico, peticionDto.getBloqueId(), asignatura);
            for (EspacioDesdoble previo : previosDeLaAsignatura)
            {
                if (!previo.getIdEspacioDesdoble().getEspacioId().getNombre().equals(peticionDto.getNombre()))
                {
                    this.iEspacioDesdobleRepository.delete(previo);
                }
            }
            this.iEspacioDesdobleRepository.flush();

            // Clave de la asignación: (espacio, bloque, asignatura). El espacio NO se mueve de tabla: permanece en el
            // catálogo y sigue disponible para asignarse como desdoble a otras asignaturas/bloques. Operación idempotente.
            IdEspacioDesdoble idEspacioDesdoble = new IdEspacioDesdoble(idEspacio, peticionDto.getBloqueId(), asignatura);

            // Salvaguarda del TOPE: el nº de aulas asignadas al bloque nunca puede superar el nº de asignaturas del
            // bloque. Como la asignación es por asignatura (1 aula/asignatura, con reemplazo) y la asignatura pertenece
            // al bloque, esto se cumple por construcción; verificamos igualmente al dar de alta una asignación nueva.
            boolean esAltaNueva = !this.iEspacioDesdobleRepository.existsById(idEspacioDesdoble);
            if (esAltaNueva)
            {
                long aulasAsignadas = this.iEspacioDesdobleRepository.contarPorBloque(cursoAcademico, peticionDto.getBloqueId());
                long asignaturasDelBloque = this.iAsignaturaRepository.contarAsignaturasDeBloque(peticionDto.getBloqueId());
                if (aulasAsignadas >= asignaturasDelBloque)
                {
                    log.error(Constants.ERR_DESDOBLE_TOPE_AULAS_MESSAGE);
                    throw new SchoolManagerServerException(Constants.ERR_DESDOBLE_TOPE_AULAS_CODE, Constants.ERR_DESDOBLE_TOPE_AULAS_MESSAGE);
                }
            }

            EspacioDesdoble espacioDesdoble = this.iEspacioDesdobleRepository.findById(idEspacioDesdoble).orElseGet(EspacioDesdoble::new);
            espacioDesdoble.setIdEspacioDesdoble(idEspacioDesdoble);
            espacioDesdoble.setBloque(bloque);
            this.iEspacioDesdobleRepository.saveAndFlush(espacioDesdoble);

            log.info("INFO - Aula de desdoble {} asignada correctamente a la asignatura {} del bloque {}", peticionDto.getNombre(), asignatura, peticionDto.getBloqueId());

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo asignar el aula de desdoble";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene la lista de aulas de desdoble ya asignadas (con su bloque) del curso académico activo. El frontend
     * filtra por el bloque seleccionado. El curso académico se resuelve internamente (seleccionado = true).
     *
     * @return una {@link ResponseEntity} con la lista de {@link EspacioDesdobleAsignadoDto}.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/espacios/desdoble")
    public ResponseEntity<?> obtenerEspaciosDesdoble()
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            List<EspacioDesdobleAsignadoDto> espaciosDesdobleDto = this.iEspacioDesdobleRepository.buscarAsignadosPorCursoAcademico(cursoAcademico);

            return ResponseEntity.ok(espaciosDesdobleDto);
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener las aulas de desdoble";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Desasigna un aula de desdoble de UN bloque concreto: elimina únicamente la relación (espacio, bloque).
     * <p>
     * Como el aula de desdoble nunca se sacó del catálogo (no se consume), aquí NO hay que devolver nada al pool:
     * el espacio sigue en sin docencia y sigue disponible. Tampoco se ven afectadas las asignaciones del mismo
     * espacio a otros bloques. La operación es transaccional. El curso académico se resuelve internamente.
     *
     * @param peticionDto - El DTO con el nombre del aula y el id del bloque del que se desasigna.
     * @return una {@link ResponseEntity} con el resultado de la operación.
     */
    @Transactional
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/espacios/desdoble", consumes = "application/json")
    public ResponseEntity<?> borrarEspacioDesdoble(@RequestBody EspacioDesdoblePeticionDto peticionDto)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            // Validamos el curso académico
            this.validarCursoAcademico(cursoAcademico);

            // Validamos el nombre del espacio
            if (peticionDto.getNombre() == null || peticionDto.getNombre().isEmpty())
            {
                log.error(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_CODE, Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
            }

            // Validamos que se haya indicado el bloque del que desasignar
            if (peticionDto.getBloqueId() == null)
            {
                log.error(Constants.ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_FIJO_GRUPO_INCOMPLETO_CODE, "Debes indicar el bloque del que desasignar el aula de desdoble");
            }

            // La asignación es por asignatura: validamos que se haya indicado la asignatura de la que desasignar
            String asignatura = peticionDto.getAsignatura();
            if (asignatura == null || asignatura.isEmpty())
            {
                log.error(Constants.ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_CODE, Constants.ERR_DESDOBLE_ASIGNATURA_NULA_VACIA_MESSAGE);
            }

            IdEspacio idEspacio = new IdEspacio(cursoAcademico, peticionDto.getNombre());
            IdEspacioDesdoble idEspacioDesdoble = new IdEspacioDesdoble(idEspacio, peticionDto.getBloqueId(), asignatura);

            if (!this.iEspacioDesdobleRepository.existsById(idEspacioDesdoble))
            {
                log.error(Constants.ERR_ESPACIO_NO_EXISTE_EN_DESDOBLE_MESSAGE);
                throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NO_EXISTE_EN_DESDOBLE_CODE, Constants.ERR_ESPACIO_NO_EXISTE_EN_DESDOBLE_MESSAGE);
            }

            // Desasignamos solo la relación (espacio, bloque, asignatura). El espacio permanece en el catálogo (sin
            // docencia) y sus asignaciones a otras asignaturas/bloques no se tocan.
            this.iEspacioDesdobleRepository.deleteById(idEspacioDesdoble);

            log.info("INFO - Aula de desdoble {} desasignada de la asignatura {} del bloque {}", peticionDto.getNombre(), asignatura, peticionDto.getBloqueId());

            return ResponseEntity.noContent().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo desasignar el aula de desdoble";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Valida que el DTO del espacio tenga curso académico y nombre no nulos ni vacíos.
     *
     * @param espacioDto - El DTO del espacio a validar.
     * @return El curso académico encontrado.
     * @throws SchoolManagerServerException si el espacio es nulo o vacío.
     */
    private CursoAcademico validarEspacioDto(EspacioDto espacioDto) throws SchoolManagerServerException
    {
        // Validamos el curso académico
        CursoAcademico cursoAcademicoEntity = this.validarCursoAcademico(espacioDto.getCursoAcademico());

        // Validamos el nombre
        if (espacioDto.getNombre() == null || espacioDto.getNombre().isEmpty())
        {
            log.error(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
            throw new SchoolManagerServerException(Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_CODE, Constants.ERR_ESPACIO_NOMBRE_NULO_VACIO_MESSAGE);
        }

        return cursoAcademicoEntity;
    }

    /**
     * Valida que el curso académico no sea nulo ni vacío y exista en la base de datos.
     *
     * @param cursoAcademico - El curso académico a validar.
     * @return El curso académico encontrado.
     * @throws SchoolManagerServerException si el curso académico es nulo, vacío o no existe.
     */
    private CursoAcademico validarCursoAcademico(String cursoAcademico) throws SchoolManagerServerException
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

        return cursoAcademicoEntity.get();
    }

    /**
     * Obtiene el {@link CursoEtapaGrupo} indicado y, si no existe, lo crea con los valores por defecto.
     * <p>
     * Sustituye a la antigua creación manual de grupos: como ahora el grupo se elige de una lista fija (A-H),
     * el grupo se materializa en BBDD en el momento en que se le asignan alumnos.
     *
     * @param curso           el curso.
     * @param etapa           la etapa.
     * @param grupo           la letra del grupo (A-H).
     * @param esoBachillerato indica si el curso/etapa es ESO o Bachillerato.
     */
    private void obtenerOCrearCursoEtapaGrupo(String cursoAcademico, Integer curso, String etapa, String grupo, boolean esoBachillerato)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo);

        CursoEtapaGrupo cursoEtapaGrupo = this.iCursoEtapaGrupoRepository.findById(idCursoEtapaGrupo).orElse(null);

        if (cursoEtapaGrupo == null)
        {
            cursoEtapaGrupo = new CursoEtapaGrupo();
            cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

            // Por defecto, horario matutino y el ESO/Bachillerato del curso/etapa
            cursoEtapaGrupo.setHorarioMatutino(true);
            cursoEtapaGrupo.setEsoBachillerato(esoBachillerato);

            cursoEtapaGrupo = this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

            log.info("INFO - Grupo {} materializado para el curso {} y etapa {}", grupo, curso, etapa);
        }

        // Materializamos las tutorías por grupo: tantas tutorías como grupos tenga el curso/etapa
        this.sincronizarTutoriasPorGrupo(cursoEtapaGrupo, curso, etapa, grupo);
    }

    /**
     * Materializa (sincroniza) las tutorías por grupo a partir de las tutorías "plantilla" cargadas por CSV a nivel
     * curso/etapa.
     * <p>
     * <b>Modelado y reconciliación CSV ↔ grupo</b>: las tutorías del CSV se persisten sin docencia
     * ({@code cursoEtapaGrupo} nulo) con el nombre sintetizado {@code "Tutoría <curso>º <etapa>"} (p. ej.
     * "Tutoría 1º ESO"); esa fila actúa como PLANTILLA del curso/etapa (define las horas y sobrevive aunque se
     * añadan o quiten grupos). A medida que se crean grupos, por cada plantilla se crea una tutoría POR GRUPO con:
     * <ul>
     *   <li>nombre {@code "Tutoría <curso>º <etapa> <grupo>"} (incluye el grupo, p. ej. "Tutoría 1º ESO A"),</li>
     *   <li>las mismas horas que la plantilla (la PK {@code (nombre, horas)} queda única gracias al grupo),</li>
     *   <li>{@code decideDireccion = true} (las tutorías las propone el equipo directivo),</li>
     *   <li>relación de docencia con el {@link CursoEtapaGrupo} de ese grupo.</li>
     * </ul>
     * Así, si existen "1 ESO A" y "1 ESO B" habrá dos tutorías de 1º ESO ("Tutoría 1º ESO A" y "Tutoría 1º ESO B")
     * disponibles para asignar al profesorado. La operación es idempotente (si la tutoría por grupo ya existe, se
     * respeta) y no rompe las tutorías ya cargadas por CSV (la plantilla sin docencia se conserva intacta).
     *
     * @param cursoEtapaGrupo el grupo recién materializado o ya existente al que vincular las tutorías.
     * @param curso           el curso.
     * @param etapa           la etapa.
     * @param grupo           la letra del grupo.
     */
    private void sincronizarTutoriasPorGrupo(CursoEtapaGrupo cursoEtapaGrupo, Integer curso, String etapa, String grupo)
    {
        // Solo materializamos tutorías para grupos reales (A, B, …); ignoramos los grupos técnicos/catálogo.
        if (Constants.SIN_GRUPO_ASIGNADO.equals(grupo) || Constants.GRUPO_OPTATIVAS.equals(grupo) || Constants.GRUPO_CATALOGO_CURSO_ETAPA.equals(grupo))
        {
            return;
        }

        String cursoAcademico = cursoEtapaGrupo.getIdCursoEtapaGrupo().getCursoAcademico();

        String nombrePlantilla = Constants.PREFIJO_REDUCCION_TUTORIA + curso + "º " + etapa;

        List<Reduccion> tutoriasPlantilla = this.iReduccionRepository.findTutoriasPlantillaByNombre(cursoAcademico, nombrePlantilla);

        for (Reduccion plantilla : tutoriasPlantilla)
        {
            String nombrePorGrupo = nombrePlantilla + " " + grupo;
            IdReduccion idReduccionPorGrupo = new IdReduccion(cursoAcademico, nombrePorGrupo, plantilla.getIdReduccion().getHoras());

            // Idempotencia: si la tutoría por grupo ya existe, no la duplicamos
            if (this.iReduccionRepository.existsById(idReduccionPorGrupo))
            {
                continue;
            }

            Reduccion tutoriaPorGrupo = new Reduccion();
            tutoriaPorGrupo.setIdReduccion(idReduccionPorGrupo);
            tutoriaPorGrupo.setDecideDireccion(true);
            tutoriaPorGrupo.setCursoEtapaGrupo(cursoEtapaGrupo);
            this.iReduccionRepository.saveAndFlush(tutoriaPorGrupo);

            log.info("INFO - Tutoría '{}' ({}h) materializada para el grupo {} {} {}", nombrePorGrupo, idReduccionPorGrupo.getHoras(), curso, etapa, grupo);
        }
    }


    /**
     * Método para asignar alumnos y registrarlos en la tabla Alumno
     *
     * @param datosBrutoAlumnoMatriculaAsignaturaOpt - El objeto DatosBrutoAlumnoMatricula que contiene los datos del alumno a asignar
     * @return Alumno - El alumno asignado y registrado en la tabla Alumno
     */
    private Alumno asignarAlumnosRegistrarAlumno(String cursoAcademico, int curso, String etapa, String grupo, String nombreAlumno, String apellidosAlumno)
    {
        // Creamos un nuevo Alumno
        Alumno alumno = null;

        // Buscamos el alumno primero a partir de la tabla matricula
        Optional<Alumno> optionalAlumno = this.iMatriculaRepository.buscarAlumnoPorCursoEtapaNombreApellidos(cursoAcademico, curso, etapa, grupo, nombreAlumno, apellidosAlumno);

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
     * @param esoBachillerato  - Indica si es ESO o Bachillerato
     * @return Asignatura - La asignatura encontrada en la base de datos
     */
    private Asignatura asignarAlumnosRegistrarAsignatura(String cursoAcademico, Integer curso, String etapa, String grupo, String nombreAsignatura, Boolean esoBachillerato)
    {
        Asignatura asignatura = new Asignatura();

        // Primero buscamos la asignatura existente primero por su nombre, curso, etapa y grupo
        Optional<Asignatura> optionalAsignatura = this.iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(cursoAcademico, curso, etapa, nombreAsignatura, grupo);

        // Si existe, la asignamos
        if (optionalAsignatura.isPresent())
        {
            // Obtenemos la asignatura
            asignatura = optionalAsignatura.get();
        }
        else
        {
            // Buscamos la asignatura por su nombre, curso, etapa y grupo 'Sin grupo'
            optionalAsignatura = this.iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(cursoAcademico, curso, etapa, nombreAsignatura, Constants.SIN_GRUPO_ASIGNADO);

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
                    if (this.iCursoEtapaGrupoRepository.buscarCursosEtapasGrupoOptativas(cursoAcademico, curso, etapa) == null)
                    {
                        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                        cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.GRUPO_OPTATIVAS));
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
                Optional<HorasYBloquesDto> asignaturaExistente = this.iAsignaturaRepository.encontrarAsignaturaPorCursoEtapaNombre(cursoAcademico, curso, etapa, nombreAsignatura);

                // Creamos una instancia del la clave primaria de la asignatura
                IdAsignatura idAsignatura = new IdAsignatura();

                // Asignamos cada uno de los campos
                CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();

                if (asignaturaExistente.get().getBloques() != null)
                {
                    cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.GRUPO_OPTATIVAS));
                }
                else
                {
                    cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo));
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
