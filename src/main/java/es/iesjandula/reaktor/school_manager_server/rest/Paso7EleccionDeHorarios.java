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

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/profesores")
    public ResponseEntity<?> obtenerProfesores()
    {
        try
        {
            List<Profesor> profesores = this.iProfesorRepository.findAll();

            if (profesores.isEmpty())
            {
                String mensajeError = "Error - No se han encontrado profesores en base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
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
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

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
                String mensajeError = "Error - No se han encontrado asignaturas para ese departamento";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            return ResponseEntity.ok().body(asignaturaDtoSinGrupos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/asignaturas")
    public ResponseEntity<?> asignarAsignatura(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                               @RequestHeader(value = "nombre") String nombreAsignatura,
                                               @RequestHeader(value = "horas") Integer horas,
                                               @RequestHeader(value = "curso") Integer curso,
                                               @RequestHeader(value = "etapa") String etapa,
                                               @RequestHeader(value = "grupo") Character grupo,
                                               @RequestHeader(value = "email") String email)
    {
        try
        {
            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                this.validacionesGlobales.validacionesGlobalesPreviasEleccionHorarios();
            }

            Impartir asignaturaImpartir = this.iImpartirRepository.encontrarAsignaturaAsignada(nombreAsignatura, horas, curso, etapa, grupo);

            if (!usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION) && !usuario.getRoles().contains(BaseConstants.ROLE_ADMINISTRADOR) && asignaturaImpartir != null)
            {
                String mensajeError = "Error - Ya se ha asignado esa asignatura a otro profesor";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }


            Impartir asignarAsignatura = construirImpartir(email, nombreAsignatura, horas, curso, etapa, grupo);
            asignarAsignatura.setAsignadoDireccion(false);

            this.iImpartirRepository.saveAndFlush(asignarAsignatura);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/reduccion")
    public ResponseEntity<?> obtenerReducciones(@AuthenticationPrincipal DtoUsuarioExtended usuario)
    {
        try
        {
            if (usuario.getRoles().contains(BaseConstants.ROLE_DIRECCION))
            {
                List<ReduccionDto> listReduccion = this.iReduccionRepository.encontrarTodasReducciones();

                if (listReduccion.isEmpty())
                {
                    String mensajeError = "Error - No se han encontro reducciones en la base de datos";
                    log.error(mensajeError);
                    throw new SchoolManagerServerException(1, mensajeError);
                }

                return ResponseEntity.ok().body(listReduccion);
            }
            List<ReduccionProfesoresDto> listReduccionesProfesores = this.iReduccionRepository.encontrarReduccionesParaProfesores();

            if (listReduccionesProfesores.isEmpty())
            {
                String mensajeError = "Error - No se han encontro reducciones en la base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }
            return ResponseEntity.ok().body(listReduccionesProfesores);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/observaciones")
    public ResponseEntity<?> obtenerDiasTramosTipoHorario()
    {
        try
        {
            List<DiasTramosTipoHorarioDto> listDiasTramosTipoHorarioDto = this.iDiasTramosRepository.findByTipoHorario();

            if (listDiasTramosTipoHorarioDto.isEmpty())
            {
                String mensajeError = "Error - No se han encontrado dias, tramos y tipos horarios en base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            return ResponseEntity.ok().body(listDiasTramosTipoHorarioDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

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

            Profesor profesor = new Profesor();
            profesor.setEmail(email);

            IdObservacionesAdicionales idObservacionesAdicionales = new IdObservacionesAdicionales(profesor);

            ObservacionesAdicionales observacionesAdicionales = new ObservacionesAdicionales(idObservacionesAdicionales, conciliacion, trabajarPrimeraHora, otrasObservaciones);

            this.iObservacionesAdicionalesRepository.saveAndFlush(observacionesAdicionales);

            List<PreferenciasHorariasProfesor> listPreferenciasHorariasProfesorABuscar = this.iPreferenciasHorariasRepository.encontrarPrefenciasPorEmail(email);
            if (listPreferenciasHorariasProfesorABuscar.size() == 3)
            {
                this.iPreferenciasHorariasRepository.deleteAll(listPreferenciasHorariasProfesorABuscar);
            }

            tramo--;

            Integer dias = this.iDiasTramosRepository.encontrarTodoPorTramoAndTipoHorarioAndDiasDesc(tramo, tipoHorario, diasDesc);

            IdDiasTramosTipoHorario idDiasTramosTipoHorario = new IdDiasTramosTipoHorario(dias, tramo, tipoHorario);

            DiasTramosTipoHorario diasTramosTipoHorarioAGuardar = new DiasTramosTipoHorario();
            diasTramosTipoHorarioAGuardar.setIdDiasTramosTipoHorario(idDiasTramosTipoHorario);

            IdPreferenciasHorariasProfesor idPreferenciasHorariasProfesor = new IdPreferenciasHorariasProfesor(profesor, diasTramosTipoHorarioAGuardar);

            PreferenciasHorariasProfesor preferenciasHorariasProfesor = new PreferenciasHorariasProfesor();
            preferenciasHorariasProfesor.setIdPreferenciasHorariasProfesor(idPreferenciasHorariasProfesor);

            this.iPreferenciasHorariasRepository.saveAndFlush(preferenciasHorariasProfesor);

            return ResponseEntity.ok().build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/solicitudes")
    public ResponseEntity<?> obtenerSolicitudes(@RequestHeader(value = "email") String email)
    {
        try
        {
            List<ImpartirHorasDto> listAsignaturasImpartidas = this.iImpartirRepository.encontrarAsignaturasImpartidasPorEmail(email);

            String tipoAsignatura = "Asignatura";

            // Convertimos la listAsignaturasImpartidas en dto.
            List<ImpartirTipoDto> listAsignaturasImpartidasDto = listAsignaturasImpartidas.stream().map(impartir ->
                    new ImpartirTipoDto(
                            tipoAsignatura,
                            impartir.getNombre(),
                            impartir.getHoras(),
                            impartir.getCupoHoras(),
                            impartir.getCurso(),
                            impartir.getEtapa(),
                            impartir.getGrupo(),
                            impartir.getAsignadoDireccion()
                    )).collect(Collectors.toList());

            List<ReduccionProfesoresDto> listReduccionProfesoresDto = this.iProfesorReduccionRepository.encontrarReudccionesPorProfesor(email);

            String tipoReduccion = "Reduccion";

            // Convertimos la listReduccionProfesoresDto en dto.
            List<ReduccionAsignadaDto> listReduccionAsignadaDto = listReduccionProfesoresDto.stream().map(reduccion ->
                    new ReduccionAsignadaDto(
                            tipoReduccion,
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
            String mensajeError = "ERROR - No se pudo encontrar asignaturas y reducciones asociadas a este profesor";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/solicitudes")
    public ResponseEntity<?> eliminarSolicitudes(@AuthenticationPrincipal DtoUsuarioExtended usuario,
                                                 @RequestHeader(value = "email", required = false) String email,
                                                 @RequestHeader(value = "nombreAsignatura", required = false) String nombreAsignatura,
                                                 @RequestHeader(value = "horasAsignatura", required = false) Integer horasAsignatura,
                                                 @RequestHeader(value = "curso", required = false) Integer curso,
                                                 @RequestHeader(value = "etapa", required = false) String etapa,
                                                 @RequestHeader(value = "grupo", required = false) Character grupo,
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

            return ResponseEntity.ok().build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/solicitudes")
    public ResponseEntity<?> guardarSolicitudes(@RequestHeader(value = "email") String email,
                                                @RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                                @RequestHeader(value = "horasAsignatura") Integer horasAsignatura,
                                                @RequestHeader(value = "curso") Integer curso,
                                                @RequestHeader(value = "etapa") String etapa,
                                                @RequestHeader(value = "grupoAntiguo") Character grupoAntiguo,
                                                @RequestHeader(value = "grupoNuevo") Character grupoNuevo)
    {
        try
        {
            Impartir asignaturaImpartidaAGuardar = construirSolicitudGuardarImpartir(email, nombreAsignatura, horasAsignatura, curso, etapa, grupoAntiguo, grupoNuevo);
            asignaturaImpartidaAGuardar.setAsignadoDireccion(true);
            this.iImpartirRepository.saveAndFlush(asignaturaImpartidaAGuardar);
            return ResponseEntity.ok().build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/gruposAsignaturas")
    public ResponseEntity<?> obtenerGrupos(@RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                           @RequestHeader(value = "horasAsignatura") Integer horasAsignatura,
                                           @RequestHeader(value = "curso") Integer curso,
                                           @RequestHeader(value = "etapa") String etapa)
    {
        try
        {
            List<GrupoAsignaturaDto> grupoAsignaturaDtos = this.iAsignaturaRepository.encontrarGrupoPorNombreAndHorasAndCursoAndEtapa(nombreAsignatura, horasAsignatura, curso, etapa);

            if (grupoAsignaturaDtos.isEmpty())
            {
                String mensajeError = "Error - No se han encontro grupos para esa asignatrua";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            return ResponseEntity.ok().body(grupoAsignaturaDtos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo acceder a la base de datos";

            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    private ProfesorReduccion construirSoliciturReduccionProfesores(String email, String nombreReduccion, Integer horasReduccion) throws SchoolManagerServerException
    {
        ReduccionProfesoresDto reduccionProfesoresDto = this.iProfesorReduccionRepository.encontrarReudccionPorProfesor(email, nombreReduccion, horasReduccion);

        if (reduccionProfesoresDto == null)
        {
            String mensajeError = "Error - No se han encontro una reduccion con esos datos asignada a este profesor";
            log.error(mensajeError);
            throw new SchoolManagerServerException(1, mensajeError);
        }

        return construirProfesorReduccion(email, nombreReduccion, horasReduccion);
    }

    private IdImpartir construirIdImpartir(String email, String nombreAsignatura, Integer curso, String etapa, Character grupoAntiguo)
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

    private Impartir construirImpartir(String email, String nombreAsignatura, Integer horasAsignatura, Integer curso, String etapa, Character grupo)
    {

        IdImpartir idImpartir = construirIdImpartir(email, nombreAsignatura, curso, etapa, grupo);

        Impartir impartir = new Impartir();
        impartir.setIdImpartir(idImpartir);
        impartir.setCupoHoras(horasAsignatura);

        return impartir;
    }

    private Impartir construirSolicitudGuardarImpartir(String email, String nombreAsignatura, Integer horasAsignatura, Integer curso, String etapa, Character grupoAntiguo, Character grupoNuevo) throws SchoolManagerServerException
    {

        IdImpartir idImpartirGrupoViejo = construirIdImpartir(email, nombreAsignatura, curso, etapa, grupoAntiguo);

        Optional<Impartir> asignaturaImpartida = this.iImpartirRepository.findById(idImpartirGrupoViejo);
        if (asignaturaImpartida.isEmpty())
        {
            String mensajeError = "Error - No existe una asignatura asignada con esos datos";
            log.error(mensajeError);
            throw new SchoolManagerServerException(1, mensajeError);
        }

        this.iImpartirRepository.delete(asignaturaImpartida.get());

        IdImpartir idImpartirGrupoNuevo = construirIdImpartir(email, nombreAsignatura, curso, etapa, grupoNuevo);

        Impartir impartir = new Impartir();
        impartir.setIdImpartir(idImpartirGrupoNuevo);
        impartir.setCupoHoras(horasAsignatura);

        return impartir;
    }

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

    private Impartir construirSolicitudImpartir(String email, String nombreAsignatura, Integer horasAsignatura, Integer curso, String etapa, Character grupo) throws SchoolManagerServerException
    {
        ImpartirDto asignaturaImpartidaDto = this.iImpartirRepository.encontrarAsignaturaImpartidaPorEmail(email, nombreAsignatura, horasAsignatura, curso, etapa, grupo);

        if (asignaturaImpartidaDto == null)
        {
            String mensajeError = "Error - No se han encontrado una asignatura con esos datos asignadas a este profesor";
            log.error(mensajeError);
            throw new SchoolManagerServerException(1, mensajeError);
        }

        return construirImpartir(email, nombreAsignatura, horasAsignatura, curso, etapa, grupo);
    }
}
