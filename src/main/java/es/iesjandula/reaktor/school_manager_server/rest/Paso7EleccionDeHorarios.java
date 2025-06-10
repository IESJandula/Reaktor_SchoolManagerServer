package es.iesjandula.reaktor.school_manager_server.rest;

import es.iesjandula.reaktor.base.security.models.DtoUsuarioExtended;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.*;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.models.ids.*;
import es.iesjandula.reaktor.school_manager_server.repositories.*;
import es.iesjandula.reaktor.school_manager_server.services.ValidacionesGlobales;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/eleccionDeHorarios")
public class Paso7EleccionDeHorarios
{
    @Autowired
    private IProfesorRepository iProfesorRepository;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private IImpartirRepository iImpartirRepository;

    @Autowired
    private IReduccionRepository iReduccionRepository;

    @Autowired
    private IProfesorReduccionRepository iProfesorReduccionRepository;

    @Autowired
    private IDiasTramosRepository iDiasTramosRepository;

    @Autowired
    private IPreferenciasHorariasRepository iPreferenciasHorariasRepository;

    @Autowired
    private IObservacionesAdicionalesRepository iObservacionesAdicionalesRepository;

    @Autowired
    private ValidacionesGlobales validacionesGlobales;

    /**
     * Recupera una lista de profesores junto con sus horarios desde la base de datos.
     * <p>
     * Si no se encuentran profesores, se devuelve una respuesta con el estado correspondiente y detalles del error.
     * En caso de errores generales, se devuelve una respuesta con estado de error interno del servidor.
     * <p>
     * Este método está protegido y requiere que el usuario solicitante tenga el rol "ROLE_DIRECCION".
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@link ProfesorDto} si la operación es exitosa.
     * - 404 (NOT_FOUND) si no se encuentran profesores.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/profesores")
    public ResponseEntity<?> obtenerProfesoresHorarios()
    {
        try
        {
            List<Profesor> profesores = this.iProfesorRepository.findAll();

            if (profesores.isEmpty())
            {
                String mensajeError = "No se encontraron profesores registrados en la base de datos.";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_PROFESORES_ENCONTRADOS, mensajeError);
            }

            List<ProfesorDto> listaProfesorDto = profesores.stream().map(profesor ->
                    new ProfesorDto(
                            profesor.getNombre(),
                            profesor.getApellidos(),
                            profesor.getEmail()
                    )).collect(Collectors.toList());

            return ResponseEntity.ok().body(listaProfesorDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al obtener la lista completa de profesores.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene la lista de asignaturas asignadas a un departamento, a partir del correo electrónico del profesor.
     * <p>
     * Este método está restringido a usuarios con el rol "ROLE_PROFESOR".
     *
     * @param email el correo electrónico del profesor, utilizado para identificar el departamento asociado.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de objetos {@link ImpartirAsignaturaDto} si se encuentran asignaturas.
     * - 404 (NOT_FOUND) si no se encuentran asignaturas para el departamento.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
    public ResponseEntity<?> obtenerAsignaturas(@RequestHeader(value = "email") String email)
    {
        try
        {
            String departamento = this.iProfesorReduccionRepository.encontrarDepartamentoPorProfesor(email);

            List<ImpartirAsignaturaDto> asignaturaDtoSinGrupos = this.iAsignaturaRepository.encontrarAsignaturasPorDepartamento(departamento);

            if (asignaturaDtoSinGrupos.isEmpty())
            {
                String mensajeError = "No se encontraron asignaturas asociadas al departamento del profesor con email: " + email;
                log.warn(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURAS_NO_ENCONTRADAS_PARA_DEPARTAMENTO, mensajeError);
            }

            return ResponseEntity.ok().body(asignaturaDtoSinGrupos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar obtener las asignaturas del profesor con email: " + email;
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna una asignatura a un profesor en función de los datos suministrados en los encabezados de la solicitud.
     * <p>
     * Valida los roles del usuario autenticado y realiza comprobaciones para garantizar que
     * la asignación sea válida. Si todo es correcto, persiste la información en la base de datos.
     *
     * @param usuario          el usuario autenticado que realiza la solicitud.
     * @param nombreAsignatura el nombre de la asignatura a asignar.
     * @param horas            la cantidad de horas asignadas a la asignatura.
     * @param curso            el curso al que pertenece la asignatura.
     * @param etapa            la etapa educativa de la asignatura.
     * @param grupo            el grupo al que se asignará la asignatura.
     * @param email            el correo electrónico del profesor al que se asignará la asignatura.
     * @return una {@link ResponseEntity} con:
     * - 201 (CREATED) si la asignación se realiza correctamente.
     * - 409 (CONFLICT) si la asignatura ya ha sido asignada a otro profesor.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/asignaturas")
    public ResponseEntity<?> asignarAsignatura(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                               @RequestHeader(value = "nombre") String nombreAsignatura,
                                               @RequestHeader(value = "horas") Integer horas,
                                               @RequestHeader(value = "curso") Integer curso,
                                               @RequestHeader(value = "etapa") String etapa,
                                               @RequestHeader(value = "email") String email)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }

//            Buscar el departamento receptor
            List<DepartamentoDto> listDepartamentoDto = this.iAsignaturaRepository.encontrarDepartamentoReceptor(nombreAsignatura, curso, etapa);
            List<String> listDepartamento = listDepartamentoDto.stream().map(DepartamentoDto::getNombre).toList();

            String departamentoProfesor = this.iProfesorRepository.buscarDepartamentoPorEmail(email);

            Map<String, Long> mapAsignaturasPorDepartamento = new HashMap<>();
            Map<String, Long> mapAsignaturasAComparar = new HashMap<>();

//          Contamos las asignaturas que hay por nombre, horas, curso y etapa
            for (String departamento : listDepartamento)
            {
                Long asignaturaImpartir = Optional.ofNullable(this.iImpartirRepository.encontrarAsignaturaAsignada(nombreAsignatura, horas, curso, etapa, departamento)).orElse(0L); //Si es nulo a 0

                mapAsignaturasPorDepartamento.put(departamento, asignaturaImpartir);

//              Contamos los grupos que hay por asignaturas
                long cantidadGrupos = this.iAsignaturaRepository.contarGruposPorNombreCursoEtapaDepartamento(nombreAsignatura, curso, etapa, departamento);
                mapAsignaturasAComparar.put(departamento, cantidadGrupos);

            }

            boolean desdoble = this.iAsignaturaRepository.isDesdoble(nombreAsignatura, curso, etapa);

            List<Asignatura> asignaturas = this.iAsignaturaRepository.findNombreByCursoEtapaAndNombres(curso, etapa, nombreAsignatura);

            String grupo = Constants.GRUPO_INICIAL;
            if (asignaturas != null && !asignaturas.isEmpty() && asignaturas.get(0).isOptativa())
            {
                grupo = Constants.GRUPO_OPTATIVAS;
            }
            else
            {
//              Lista de impartidas actuales
                List<ImpartidaGrupoDeptDto> gruposDistintosPorAsignaturaImpartida = iImpartirRepository.encontrarGruposYDeptAsignaturaImpartidaPorNombreAndCursoEtapa(nombreAsignatura, curso, etapa);

//              Este stream filtra las asignaturas de la lista original que sean del departamento del profesor a asignar
                assert asignaturas != null;
                List<Asignatura> posiblesEnMiDept = asignaturas.stream()
                        .filter(a -> a.getDepartamentoReceptor()
                                .getNombre()
                                .equals(departamentoProfesor)).toList();

//              Este de aquí se queda con el primer grupo de la lista anterior que no este ya en la segunda, la de las impartidas
//              Si no quedan grupos libres le vuelve a dejar el A, lo que solo es visible si la asignatura tiene desdoble haciendo además que la asignacion sea inocua
                Optional<String> grupoLibre = posiblesEnMiDept.stream()
                        .map(a -> a.getIdAsignatura()
                                .getCursoEtapaGrupo()
                                .getIdCursoEtapaGrupo().getGrupo())
                        .filter(g -> gruposDistintosPorAsignaturaImpartida.stream().noneMatch(dto ->
                                dto.getGrupo().equals(g) &&
                                        dto.getDepartamento().equals(departamentoProfesor)
                        ))
                        .findFirst();
                grupo = grupoLibre.orElse(grupo);
            }

            Impartir asignarAsignatura = construirImpartir(email, nombreAsignatura, horas, curso, etapa, grupo);
            asignarAsignatura.setAsignadoDireccion(false);


            long yaAsignadas = mapAsignaturasPorDepartamento.getOrDefault(departamentoProfesor, 0L);
            long maxPermitidas = mapAsignaturasAComparar.getOrDefault(departamentoProfesor, 0L);


            if (!desdoble && yaAsignadas >= maxPermitidas)
            {
                List<ProfesorImpartirDto> listProfesores = this.iImpartirRepository.encontrarProfesorPorNombreAndCursoEtpa(nombreAsignatura, curso, etapa);
                String mensajeError = null;
                if (listProfesores.size() > 1)
                {
                    StringBuilder profesores = new StringBuilder();
                    for (int i = 0; i < listProfesores.size(); i++)
                    {
                        ProfesorImpartirDto profesorDto = listProfesores.get(i);

                        profesores.append(profesorDto.getNombre()).append(" ").append(profesorDto.getApellidos());

                        if (i < listProfesores.size() - 1)
                        {
                            profesores.append(", ");
                        }
                    }
                    mensajeError = "La asignatura " + nombreAsignatura + " ya ha sido asignada a los profesores " + profesores;
                    log.error(mensajeError);
                }
                else if (!listProfesores.isEmpty())
                {
                    ProfesorImpartirDto profesorDto = listProfesores.get(0);
                    mensajeError = "La asignatura " + nombreAsignatura + " ya ha sido asignada al profesor " + profesorDto.getNombre() + " " + profesorDto.getApellidos();
                    log.error(mensajeError);
                }

                throw new SchoolManagerServerException(Constants.ASIGNATURA_ASIGNADA_A_PROFESOR, mensajeError);
            }

            this.iImpartirRepository.saveAndFlush(asignarAsignatura);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar asignar la asignatura '" + nombreAsignatura + "' al profesor con email: " + email;
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene una lista de reducciones en función del rol del usuario autenticado.
     * <p>
     * Si el usuario tiene el rol de Dirección, se recuperan todas las reducciones del sistema.
     * En caso contrario, se obtienen únicamente las reducciones asociadas al profesorado.
     * Se aplican control de error para manejar el caso de que no se encuentren las reducciones asociadas al profesorado.
     *
     * @param usuario el usuario autenticado cuya función determina la lógica de recuperación de datos.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) si se recuperan correctamente las reducciones.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un fallo inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/reduccion")
    public ResponseEntity<?> obtenerReducciones(@AuthenticationPrincipal DtoUsuarioExtended usuario)
    {
        try
        {
            if (usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                List<ReduccionDto> listReduccion = this.iReduccionRepository.encontrarTodasReducciones();

                return ResponseEntity.ok().body(listReduccion);
            }
            List<ReduccionProfesoresDto> listReduccionesProfesores = this.iReduccionRepository.encontrarReduccionesParaProfesores();

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(listReduccionesProfesores);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar obtener las reducciones para el usuario: " + usuario.getEmail();
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera la lista de días, tramos horarios y tipos de horario disponibles en la base de datos.
     * <p>
     * Este método está restringido a usuarios con el rol de profesor y realiza la consulta a través
     * del repositorio correspondiente. En caso de que no se encuentren datos, se lanza una excepción
     * específica indicando su ausencia. Si ocurre un error general, se registra en el log y se devuelve
     * una respuesta de error adecuada.
     *
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) si se recuperan correctamente los datos como una lista de {@link DiasTramosTipoHorarioDto}.
     * - 404 (NOT_FOUND) si no se encuentra información en la base de datos.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/observaciones")
    public ResponseEntity<?> obtenerDiasTramosTipoHorario()
    {
        try
        {
            List<DiasTramosTipoHorarioDto> listDiasTramosTipoHorarioDto = this.iDiasTramosRepository.findByTipoHorario();

            if (listDiasTramosTipoHorarioDto.isEmpty())
            {
                String mensajeError = "No se han encontrado dias, tramos y tipos horarios en base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.DIAS_TRAMOS_TIPOS_HORARIOS_NO_ENCONTRADOS, mensajeError);
            }

            return ResponseEntity.ok().body(listDiasTramosTipoHorarioDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar obtener los dias, tramos y tipos horarios en base de datos";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Actualiza las observaciones y preferencias horarias de un profesor en función de los parámetros proporcionados.
     * <p>
     * Este método se encarga de guardar tanto las observaciones como las preferencias de tramos horarios,
     * realizando las validaciones necesarias y gestionando posibles excepciones.
     *
     * @param usuario             el usuario autenticado que realiza la solicitud, representado como {@link DtoUsuarioExtended}.
     * @param conciliacion        indica si el profesor solicita una observación conciliadora.
     * @param trabajarPrimeraHora indica si el profesor desea trabajar en la primera hora.
     * @param otrasObservaciones  observaciones adicionales sobre la disponibilidad horaria del profesor (opcional).
     * @param diasDesc            descripción del/de los día(s) aplicables para el horario.
     * @param tramo               el tramo horario que se desea modificar.
     * @param tipoHorario         el tipo de horario o jornada laboral.
     * @param email               el correo electrónico del profesor al que se aplican las observaciones.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la operación se realiza correctamente.
     * - 400 (NOT_FOUND) si se lanza una excepción de tipo {@link SchoolManagerServerException} durante la validación.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado o de acceso a la base de datos.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/observaciones")
    public ResponseEntity<?> actualizarObservaciones(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                                     @RequestHeader(value = "conciliacion") Boolean conciliacion,
                                                     @RequestHeader(value = "trabajarPrimeraHora") Boolean trabajarPrimeraHora,
                                                     @RequestHeader(value = "otrasObservaciones", required = false) String otrasObservaciones,
                                                     @RequestHeader(value = "dia") String diasDesc,
                                                     @RequestHeader(value = "tramo") Integer tramo,
                                                     @RequestHeader(value = "tipoHorario") String tipoHorario,
                                                     @RequestHeader(value = "email") String email)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }

            Profesor profesor = this.iProfesorRepository.findByEmail(email);
            if (profesor == null)
            {
                String mensajeError = "No se encontró ningún profesor con el email: " + email;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError);
            }

            IdObservacionesAdicionales idObservacionesAdicionales = new IdObservacionesAdicionales(profesor);

            ObservacionesAdicionales observacionesAdicionales = new ObservacionesAdicionales(idObservacionesAdicionales, conciliacion, trabajarPrimeraHora, otrasObservaciones);

            this.iObservacionesAdicionalesRepository.saveAndFlush(observacionesAdicionales);

            Optional<List<PreferenciasHorariasProfesor>> listPreferenciasHorariasProfesorABuscar = this.iPreferenciasHorariasRepository.encontrarPrefenciasPorEmail(email);

            if (listPreferenciasHorariasProfesorABuscar.isPresent() && listPreferenciasHorariasProfesorABuscar.get().size() == 3)
            {
                this.iPreferenciasHorariasRepository.deleteAll(listPreferenciasHorariasProfesorABuscar.get());
            }

            tramo--;

            Integer dias = this.iDiasTramosRepository.encontrarTodoPorTramoAndTipoHorarioAndDiasDesc(tramo, tipoHorario, diasDesc);
            if (dias == null)
            {
                String mensajeError = "No se pudo encontrar el identificador del día/tramo/tipoHorario especificado.";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.DIAS_TRAMOS_TIPOS_HORARIOS_NO_ENCONTRADOS, mensajeError);
            }

            IdDiasTramosTipoHorario idDiasTramosTipoHorario = new IdDiasTramosTipoHorario(dias, tramo, tipoHorario);

            DiasTramosTipoHorario diasTramosTipoHorarioAGuardar = new DiasTramosTipoHorario();
            diasTramosTipoHorarioAGuardar.setIdDiasTramosTipoHorario(idDiasTramosTipoHorario);

            IdPreferenciasHorariasProfesor idPreferenciasHorariasProfesor = new IdPreferenciasHorariasProfesor(profesor, diasTramosTipoHorarioAGuardar);

            PreferenciasHorariasProfesor preferenciasHorariasProfesor = new PreferenciasHorariasProfesor();
            preferenciasHorariasProfesor.setIdPreferenciasHorariasProfesor(idPreferenciasHorariasProfesor);

            this.iPreferenciasHorariasRepository.saveAndFlush(preferenciasHorariasProfesor);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.PROFESOR_NO_ENCONTRADO)
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
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar actualizar las observaciones del profesor.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera los datos de las observaciones adicionales(conciliacion, preferencias, detalles adicionales) de un profesor a traves de su email.
     * <p>
     * Devuelve un dto de salida con la información de los campos equivalentes encontrados en el front.
     *
     * @param email el correo electrónico del profesor del que se desea obtener la información.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de observaciones adicionales.
     * - 404 (NOT_FOUND) si no se encuentra información asociada al profesor.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la consulta.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/observaciones/usuario")
    public ResponseEntity<?> obtenerObservacionesAdicionales(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                                             @RequestHeader(value = "email") String email)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }
            Profesor profesorEncontrado = iProfesorRepository.findByEmail(email);
            Optional<ObservacionesAdicionales> observacionesEncontradas = iObservacionesAdicionalesRepository.findByIdObservacionesAdicionales_Profesor(profesorEncontrado);

            ObservacionesDto observacionesDto = new ObservacionesDto();

            if (observacionesEncontradas.isPresent())
            {
                observacionesDto.setTieneObservaciones(true);
                observacionesDto.setConciliacion(observacionesEncontradas.get().getConciliacion());
                observacionesDto.setTrabajarPrimeraHora(observacionesEncontradas.get().getTrabajarPrimeraHora());
                observacionesDto.setOtrasObservaciones(observacionesEncontradas.get().getOtrasObservaciones());
            }

            return ResponseEntity.ok().body(observacionesDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al recuperar las observaciones del profesor.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera la lista de dias con sus tramos horarios asociados a un profesor en función de su correo electrónico.
     * <p>
     * Devuelve un dto de salida con la información necesaria para mostrar las preferencias del profesor.
     *
     * @param email el correo electrónico del profesor del que se desea obtener la información.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de franjas horarias.
     * - 404 (NOT_FOUND) si no se encuentra información asociada al profesor.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la consulta.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/preferencias/usuario")
    public ResponseEntity<?> obtenerTramosHorariosUsuario(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                                          @RequestHeader(value = "email") String email)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }

            Optional<List<PreferenciasHorariasProfesor>> preferenciasHorariasEncontradas = iPreferenciasHorariasRepository.encontrarPrefenciasPorEmail(email);
            TramosHorariosUsuarioDto tramosHorariosUsuarioDto = new TramosHorariosUsuarioDto();
            List<DiasTramosTipoHorarioDto> tramosDto = new ArrayList<>(List.of());
            if (preferenciasHorariasEncontradas.isPresent() && !preferenciasHorariasEncontradas.get().isEmpty())
            {
                tramosHorariosUsuarioDto.setTieneObservaciones(true);

                for (PreferenciasHorariasProfesor tramo : preferenciasHorariasEncontradas.get())
                {
                    DiasTramosTipoHorarioDto tramoElegido = new DiasTramosTipoHorarioDto();
                    tramoElegido.setDia(String.valueOf(tramo.getIdPreferenciasHorariasProfesor().getDiasTramosTipoHorario().getIdDiasTramosTipoHorario().getDia()));
                    tramoElegido.setTramo(tramo.getIdPreferenciasHorariasProfesor().getDiasTramosTipoHorario().getIdDiasTramosTipoHorario().getTramo());
                    tramoElegido.setTipoHorario(String.valueOf(tramo.getIdPreferenciasHorariasProfesor().getDiasTramosTipoHorario().getIdDiasTramosTipoHorario().getTipoHorario()));
                    tramosDto.add(tramoElegido);
                }
                tramosHorariosUsuarioDto.setTramosHorarios(tramosDto);
            }

            return ResponseEntity.ok().body(tramosHorariosUsuarioDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al recuperar las preferencias del profesor.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera la lista de asignaturas y reducciones asociadas a un profesor en función de su correo electrónico.
     * <p>
     * El resultado se estructura en un mapa con dos claves principales:
     * "asignaturas", que contiene la lista de asignaturas con sus respectivos detalles, y
     * "reduccionAsignadas", que contiene la lista de reducciones asignadas al profesor.
     *
     * @param email el correo electrónico del profesor del que se desea obtener la información.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y un mapa con las claves "asignaturas" y "reduccionAsignadas" si la consulta se realiza correctamente.
     * - 404 (NOT_FOUND) si no se encuentra información asociada al profesor.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado durante la consulta.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/solicitudes")
    public ResponseEntity<?> obtenerSolicitudes(@RequestHeader(value = "email") String email)
    {
        try
        {
            List<ImpartirHorasDto> listAsignaturasImpartidas = this.iImpartirRepository.encontrarAsignaturasImpartidasPorEmail(email);

            // Convertimos la listAsignaturasImpartidas en dto.
            List<ImpartirTipoDto> listAsignaturasImpartidasDto = listAsignaturasImpartidas.stream().map(impartir ->
                    new ImpartirTipoDto(
                            Constants.TIPO_ASIGNATURA,
                            impartir.getNombre(),
                            impartir.getHoras(),
                            impartir.getCupoHoras(),
                            impartir.getCurso(),
                            impartir.getEtapa(),
                            impartir.getGrupo(),
                            impartir.getAsignadoDireccion()
                    )).collect(Collectors.toList());

            List<ReduccionProfesoresDto> listReduccionProfesoresDto = this.iProfesorReduccionRepository.encontrarReudccionesPorProfesor(email);

            // Convertimos la listReduccionProfesoresDto en dto.
            List<ReduccionAsignadaDto> listReduccionAsignadaDto = listReduccionProfesoresDto.stream().map(reduccion ->
                    new ReduccionAsignadaDto(
                            Constants.TIPO_REDUCCION,
                            reduccion.getNombre(),
                            reduccion.getHoras()
                    )).collect(Collectors.toList());

            Map<String, Object> solicitud = new HashMap<>();
            solicitud.put("asigunaturas", listAsignaturasImpartidasDto);
            solicitud.put("reduccionAsignadas", listReduccionAsignadaDto);

            return ResponseEntity.ok().body(solicitud);
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar obtener las solicitudes del profesor con correo electrónico: " + email;
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina solicitudes relacionadas con asignaturas o reducciones de profesorado según los parámetros proporcionados.
     * <p>
     * El tipo de solicitud a eliminar (asignatura o reducción) se determina a partir de los encabezados incluidos
     * en la solicitud. El acceso está restringido según los permisos del usuario autenticado.
     *
     * @param usuario          el usuario autenticado que realiza la solicitud, representado como {@link DtoUsuarioExtended}.
     * @param email            el correo electrónico del profesor asociado a la solicitud (opcional).
     * @param nombreAsignatura el nombre de la asignatura a eliminar, requerido si se trata de una solicitud de asignatura.
     * @param horasAsignatura  las horas asociadas a la asignatura, opcional.
     * @param curso            el curso escolar de la asignatura, opcional.
     * @param etapa            la etapa educativa de la asignatura, opcional.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la eliminación se realizó correctamente.
     * - 404 (NOT_FOUND) si no se encuentra la solicitud a eliminar.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/solicitudes")
    public ResponseEntity<?> eliminarSolicitudes(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                                 @RequestHeader(value = "email", required = false) String email,
                                                 @RequestHeader(value = "nombreAsignatura", required = false) String nombreAsignatura,
                                                 @RequestHeader(value = "horasAsignatura", required = false) Integer horasAsignatura,
                                                 @RequestHeader(value = "curso", required = false) Integer curso,
                                                 @RequestHeader(value = "etapa", required = false) String etapa,
                                                 @RequestHeader(value = "grupo", required = false) String grupo,
                                                 @RequestHeader(value = "nombreReduccion", required = false) String nombreReduccion,
                                                 @RequestHeader(value = "horasReduccion", required = false) Integer horasReduccion)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }

            if (nombreAsignatura != null && !nombreAsignatura.isEmpty())
            {
                Impartir asignaturaImpartidaABorrar = construirSolicitudImpartir(email, nombreAsignatura, horasAsignatura, curso, etapa, grupo);
                this.iImpartirRepository.delete(asignaturaImpartidaABorrar);
                return ResponseEntity.ok().build();
            }

            ProfesorReduccion profesorReduccionABorrar = construirSoliciturReduccionProfesores(email, nombreReduccion, horasReduccion);

            this.iProfesorReduccionRepository.delete(profesorReduccionABorrar);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar eliminar las solicitudes.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Guarda una solicitud de asignación de asignatura procesando los encabezados de la solicitud
     * y persistiendo la información en la base de datos.
     * <p>
     * En caso de error durante la operación, se devuelven respuestas apropiadas indicando la causa.
     *
     * @param email            el correo electrónico del usuario que realiza la solicitud.
     * @param nombreAsignatura el nombre de la asignatura que se desea asignar.
     * @param horasAsignatura  el número de horas asignadas a la asignatura.
     * @param curso            el curso académico al que pertenece la asignatura.
     * @param etapa            la etapa educativa correspondiente a la asignatura.
     * @param grupoAntiguo     el identificador del grupo previo de la asignatura.
     * @param grupoNuevo       el nuevo identificador de grupo al que se desea asignar la asignatura.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si la asignación se guarda correctamente.
     * - 404 (NOT_FOUND) si no se encuentra la solicitud de asignación.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error durante el procesamiento.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/solicitudes")
    public ResponseEntity<?> guardarSolicitudes(@RequestHeader(value = "email") String email,
                                                @RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                                @RequestHeader(value = "horasAsignatura") Integer horasAsignatura,
                                                @RequestHeader(value = "curso") Integer curso,
                                                @RequestHeader(value = "etapa") String etapa,
                                                @RequestHeader(value = "grupoAntiguo") String grupoAntiguo,
                                                @RequestHeader(value = "grupoNuevo") String grupoNuevo)
    {
        try
        {
            Impartir asignaturaImpartidaAGuardar = construirSolicitudGuardarImpartir(email, nombreAsignatura, horasAsignatura, curso, etapa, grupoAntiguo, grupoNuevo);
            asignaturaImpartidaAGuardar.setAsignadoDireccion(true);
            this.iImpartirRepository.saveAndFlush(asignaturaImpartidaAGuardar);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar guardar la solicitud.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Recupera los grupos de asignaturas en función del nombre, horas, curso y etapa proporcionados.
     * <p>
     * El método filtra los grupos que coincidan con los parámetros especificados en los encabezados de la solicitud.
     *
     * @param nombreAsignatura el nombre de la asignatura a buscar.
     * @param horasAsignatura  el número de horas asignadas a la asignatura.
     * @param curso            el curso académico de la asignatura.
     * @param etapa            la etapa educativa correspondiente a la asignatura.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y una lista de {@link GrupoAsignaturaDto} si se encuentran resultados.
     * - 404 (NOT_FOUND) si no se encuentra ningún grupo que coincida.
     * - 500 (INTERNAL_SERVER_ERROR) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/gruposAsignaturas")
    public ResponseEntity<?> obtenerGruposDeAsignaturas(@RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                                        @RequestHeader(value = "horasAsignatura") Integer horasAsignatura,
                                                        @RequestHeader(value = "curso") Integer curso,
                                                        @RequestHeader(value = "etapa") String etapa)
    {
        try
        {
            List<GrupoAsignaturaDto> grupoAsignaturaDtos = this.iAsignaturaRepository.encontrarGrupoPorNombreAndHorasAndCursoAndEtapa(nombreAsignatura, horasAsignatura, curso, etapa);

            if (grupoAsignaturaDtos.isEmpty())
            {
                String mensajeError = "No se han encontró grupos para esa asignatura";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.GRUPOS_NO_ENCONTRADOS_PARA_ASIGNATURA, mensajeError);
            }

            return ResponseEntity.ok().body(grupoAsignaturaDtos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Se produjo un error inesperado al intentar obtener los grupos de asignaturas.";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Construye una instancia de {@link ProfesorReduccion} para un profesor y reducción especificados.
     * <p>
     * Busca la reducción asignada al profesor identificado por su correo electrónico, con el nombre y las horas indicadas.
     *
     * @param email           el correo electrónico del profesor.
     * @param nombreReduccion el nombre de la reducción asignada al profesor.
     * @param horasReduccion  el número de horas de la reducción.
     * @return una instancia de {@link ProfesorReduccion} con los datos correspondientes.
     * @throws SchoolManagerServerException si no se encuentra una reducción asignada al profesor con los datos especificados.
     */
    private ProfesorReduccion construirSoliciturReduccionProfesores(String email, String nombreReduccion, Integer horasReduccion) throws SchoolManagerServerException
    {
        ReduccionProfesoresDto reduccionProfesoresDto = this.iProfesorReduccionRepository.encontrarReudccionPorProfesor(email, nombreReduccion, horasReduccion);

        if (reduccionProfesoresDto == null)
        {
            String mensajeError = "No se ha encontrado una reducción con esos datos asignada a este profesor";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.REDUCCION_NO_ASIGNADA_A_PROFESOR, mensajeError);
        }

        return construirProfesorReduccion(email, nombreReduccion, horasReduccion);
    }

    /**
     * Construye una instancia de {@code IdImpartir} con los parámetros proporcionados.
     * <p>
     * Esta instancia incluye los identificadores compuestos relacionados con la asignatura y el profesor.
     *
     * @param email            el correo electrónico del profesor.
     * @param nombreAsignatura el nombre de la asignatura.
     * @param curso            el curso al que pertenece la asignatura.
     * @param etapa            la etapa educativa de la asignatura (por ejemplo, primaria, secundaria).
     * @param grupoAntiguo     el grupo antiguo asociado a la asignatura.
     * @return una instancia de {@code IdImpartir} que contiene los identificadores compuestos.
     */
    private IdImpartir construirIdImpartir(String email, String nombreAsignatura, Integer curso, String etapa, String grupoAntiguo)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
        idCursoEtapaGrupo.setCurso(curso);
        idCursoEtapaGrupo.setEtapa(etapa);
        idCursoEtapaGrupo.setGrupo(grupoAntiguo);

        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

        IdAsignatura idAsignatura = new IdAsignatura();
        idAsignatura.setNombre(nombreAsignatura);
        idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);

        Asignatura asignatura = new Asignatura();
        asignatura.setIdAsignatura(idAsignatura);

        Profesor profesor = new Profesor();
        profesor.setEmail(email);

        return new IdImpartir(asignatura, profesor);
    }

    /**
     * Construye una instancia de la clase {@code Impartir} asociándola con
     * un identificador único y estableciendo el número de horas asignadas.
     *
     * @param email            el correo electrónico de la persona responsable de impartir la asignatura.
     * @param nombreAsignatura el nombre de la asignatura a impartir.
     * @param horasAsignatura  la cantidad de horas asignadas a la asignatura.
     * @param curso            el curso en el que se imparte la asignatura.
     * @param etapa            la etapa educativa en la que se imparte la asignatura.
     * @param grupo            el identificador del grupo asignado a la asignatura.
     * @return una nueva instancia de {@code Impartir} con los detalles especificados.
     */
    private Impartir construirImpartir(String email, String nombreAsignatura, Integer horasAsignatura, Integer curso, String etapa, String grupo)
    {

        IdImpartir idImpartir = construirIdImpartir(email, nombreAsignatura, curso, etapa, grupo);

        Impartir impartir = new Impartir();
        impartir.setIdImpartir(idImpartir);
        impartir.setCupoHoras(horasAsignatura);

        return impartir;
    }

    /**
     * Construye un objeto {@code Impartir} para guardar una asignación docente de una asignatura y profesor dados.
     * <p>
     * Este método verifica la existencia de una asignación docente previa, la elimina y crea una nueva asignación
     * con el grupo y detalles actualizados.
     *
     * @param email            el correo electrónico del profesor.
     * @param nombreAsignatura el nombre de la asignatura.
     * @param horasAsignatura  la cantidad de horas asignadas a la asignatura.
     * @param curso            el curso académico de la asignación.
     * @param etapa            la etapa educativa (por ejemplo, primaria, secundaria).
     * @param grupoAntiguo     el grupo anterior asignado al profesor para esta asignación docente.
     * @param grupoNuevo       el nuevo grupo que se asignará al profesor para esta asignación docente.
     * @return una nueva instancia de {@code Impartir} que representa la asignación docente actualizada.
     * @throws SchoolManagerServerException si la asignación docente previa no existe o no se puede encontrar.
     */
    private Impartir construirSolicitudGuardarImpartir(String email, String nombreAsignatura, Integer horasAsignatura, Integer curso, String etapa, String grupoAntiguo, String grupoNuevo) throws SchoolManagerServerException
    {

        IdImpartir idImpartirGrupoViejo = construirIdImpartir(email, nombreAsignatura, curso, etapa, grupoAntiguo);

        Optional<Impartir> asignaturaImpartida = this.iImpartirRepository.findById(idImpartirGrupoViejo);
        if (asignaturaImpartida.isEmpty())
        {
            String mensajeError = "No existe una asignatura asignada con esos datos";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ASIGNADA_A_PROFESOR, mensajeError);
        }

        this.iImpartirRepository.delete(asignaturaImpartida.get());

        IdImpartir idImpartirGrupoNuevo = construirIdImpartir(email, nombreAsignatura, curso, etapa, grupoNuevo);

        Impartir impartir = new Impartir();
        impartir.setIdImpartir(idImpartirGrupoNuevo);
        impartir.setCupoHoras(horasAsignatura);

        return impartir;
    }

    /**
     * Construye un objeto {@link ProfesorReduccion} asignando el correo electrónico,
     * el nombre de la reducción y las horas de reducción a sus componentes correspondientes.
     *
     * @param email           el correo electrónico asociado al profesor.
     * @param nombreReduccion el nombre de la reducción.
     * @param horasReduccion  el número de horas asociadas a la reducción.
     * @return el objeto {@link ProfesorReduccion} construido con los datos asignados.
     */
    private ProfesorReduccion construirProfesorReduccion(String email, String nombreReduccion, Integer horasReduccion)
    {
        Profesor profesor = new Profesor();
        profesor.setEmail(email);

        IdReduccion idReduccion = new IdReduccion();
        idReduccion.setNombre(nombreReduccion);
        idReduccion.setHoras(horasReduccion);

        Reduccion reduccion = new Reduccion();
        reduccion.setIdReduccion(idReduccion);

        IdProfesorReduccion idProfesorReduccion = new IdProfesorReduccion();
        idProfesorReduccion.setProfesor(profesor);
        idProfesorReduccion.setReduccion(reduccion);

        ProfesorReduccion profesorReduccion = new ProfesorReduccion();
        profesorReduccion.setIdProfesorReduccion(idProfesorReduccion);

        return profesorReduccion;
    }

    /**
     * Construye y devuelve un objeto {@code Impartir} basado en los datos proporcionados.
     * <p>
     * Verifica previamente si la asignatura está asignada al profesor identificado por el correo electrónico.
     * Si no existe tal asignación, lanza una excepción.
     *
     * @param email            el correo electrónico del profesor.
     * @param nombreAsignatura el nombre de la asignatura a impartir.
     * @param horasAsignatura  la cantidad de horas asignadas a la asignatura.
     * @param curso            el curso al que pertenece la asignatura.
     * @param etapa            la etapa educativa (por ejemplo, primaria, secundaria).
     * @param grupo            el grupo dentro del curso.
     * @return la entidad {@code Impartir} creada con los datos proporcionados.
     * @throws SchoolManagerServerException si no se encuentra la asignatura asignada al profesor.
     */
    private Impartir construirSolicitudImpartir(String email, String nombreAsignatura, Integer horasAsignatura, Integer curso, String etapa, String grupo) throws SchoolManagerServerException
    {
        ImpartirDto asignaturaImpartidaDto = this.iImpartirRepository.encontrarAsignaturaImpartidaPorEmail(email, nombreAsignatura, horasAsignatura, curso, etapa, grupo);

        if (asignaturaImpartidaDto == null)
        {
            String mensajeError = "No se han encontrado una asignatura con esos datos asignadas a este profesor";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ASIGNADA_A_PROFESOR, mensajeError);
        }

        return construirImpartir(email, nombreAsignatura, horasAsignatura, curso, etapa, grupo);
    }
}
