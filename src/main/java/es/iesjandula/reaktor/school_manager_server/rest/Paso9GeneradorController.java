package es.iesjandula.reaktor.school_manager_server.rest;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorInfoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.SesionBaseDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ValidadorDatosDto;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionBase;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionBase;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDiaTramoTipoHorarioRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorInstanciaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorSesionBaseRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.services.DiaTramoTipoHorarioService;
import es.iesjandula.reaktor.school_manager_server.services.GeneradorService;
import es.iesjandula.reaktor.school_manager_server.services.ValidadorDatosService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/schoolManager/generador")
public class Paso9GeneradorController
{
    @Autowired
    private DiaTramoTipoHorarioService diaTramoTipoHorarioService ;

    @Autowired
    private ValidadorDatosService validadorDatosService ;

    @Autowired
    private IGeneradorRepository generadorRepository ;

    @Autowired
    private IProfesorRepository profesorRepository ;

    @Autowired
    private IAsignaturaRepository asignaturaRepository ;

    @Autowired
    private IDiaTramoTipoHorarioRepository diaTramoTipoHorarioRepository ;
    
    @Autowired
    private IGeneradorSesionBaseRepository generadorSesionBaseRepository ;

    @Autowired
    private IGeneradorInstanciaRepository generadorInstanciaRepository ;

    @Autowired
    private GeneradorService generadorService ;

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/estado")
    public ResponseEntity<?> obtenerEstadoGenerador()
    {
        try
        {
            // Obtenemos el estado del generador y sus detalles generales
            GeneradorInfoDto generadorInfoDto = this.generadorService.obtenerEstadoGenerador() ;

            // Devolver el DTO del generador
            return ResponseEntity.ok(generadorInfoDto) ;
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo obtener el estado del generador";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/diasSemana")
    public ResponseEntity<?> obtenerDiasSemana()
    {
        try
        {
            // Obtenemos los días de la semana
            List<String> diasSemana = this.diaTramoTipoHorarioService.obtenerDiasSemana() ;

            return ResponseEntity.ok(diasSemana) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener los días de la semana";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/tramosHorarios")
    public ResponseEntity<?> obtenerTramosHorarios()
    {
        try
        {
            // Obtenemos los tramos horarios
            List<String> tramosHorarios = this.diaTramoTipoHorarioService.obtenerTramosHorarios() ;

            return ResponseEntity.ok(tramosHorarios) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener los tramos horarios";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
    
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/sesionesBase")
    public ResponseEntity<?> actualizarSesionesBase(@RequestHeader(value = "email") String email,
                                                    @RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                                    @RequestHeader(value = "curso") int curso,
                                                    @RequestHeader(value = "etapa") String etapa,
                                                    @RequestHeader(value = "grupo") String grupo,
                                                    @RequestHeader(value = "numeroSesion") int numeroSesion,
                                                    @RequestHeader(value = "diaDesc") String diaDesc,
                                                    @RequestHeader(value = "tramoDesc") String tramoDesc)
    {
        try
        {
            // Buscamos el profesor
            Profesor profesor = this.buscarProfesor(email) ;

            // Buscamos la asignatura
            Asignatura asignatura = this.buscarAsignatura(nombreAsignatura, curso, etapa, grupo) ;

            // Buscamos la sesión base por número de sesión, profesor y asignatura
            Optional<GeneradorSesionBase> generadorSesionBaseOptional = this.generadorSesionBaseRepository.buscarSesionBasePorNumeroSesionProfesorAsignatura(numeroSesion, profesor, asignatura) ;

            // Si existe, la borramos
            if (generadorSesionBaseOptional.isPresent())
            {
                // La borramos
                this.generadorSesionBaseRepository.delete(generadorSesionBaseOptional.get()) ;
                this.generadorSesionBaseRepository.flush() ;
            }

            // Obtenemos las restricciones de tipo de horario de la asignatura
            DiaTramoTipoHorario diaTramoTipoHorario = this.diaTramoTipoHorarioRepository.buscarPorDiaDescTramoDesc(diaDesc, tramoDesc) ;

            if (!Constants.SIN_SELECCIONAR.equals(diaDesc) && !Constants.SIN_SELECCIONAR.equals(tramoDesc))
            {
                // Actualizamos la sesión base
                this.actualizarSesionesBaseInternal(numeroSesion, asignatura, profesor, diaTramoTipoHorario) ;
            }

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo actualizar la sesión base";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método que actualiza una sesión base
     * @param numeroSesion - Número de sesión
     * @param impartir - Impartir
     * @param diaTramoTipoHorario - Día y tramo de tipo horario
     */
    private void actualizarSesionesBaseInternal(int numeroSesion, Asignatura asignatura, Profesor profesor, DiaTramoTipoHorario diaTramoTipoHorario)
    {
        // Creamos una instancia de IdGeneradorSesionBase
        IdGeneradorSesionBase idGeneradorSesionBase = new IdGeneradorSesionBase() ;
        
        // Asignamos los valores a la instancia
        idGeneradorSesionBase.setNumeroSesion(numeroSesion) ;   
        idGeneradorSesionBase.setAsignatura(asignatura) ;
        idGeneradorSesionBase.setProfesor(profesor) ;
        idGeneradorSesionBase.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

        // Creamos una instancia de GeneradorSesionBase
        GeneradorSesionBase generadorSesionBaseInstancia = new GeneradorSesionBase() ;
        
        // Asignamos cada parámetro a la instancia
        generadorSesionBaseInstancia.setIdGeneradorSesionBase(idGeneradorSesionBase) ;
        generadorSesionBaseInstancia.setAsignatura(asignatura) ;
        generadorSesionBaseInstancia.setProfesor(profesor) ;
        generadorSesionBaseInstancia.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

        // Guardamos la instancia en la base de datos
        this.generadorSesionBaseRepository.saveAndFlush(generadorSesionBaseInstancia) ; 
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/sesionesBase")
    public ResponseEntity<?> obtenerSesionesBase(@RequestHeader(value = "email") String email,
                                                 @RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                                 @RequestHeader(value = "curso") int curso,
                                                 @RequestHeader(value = "etapa") String etapa,
                                                 @RequestHeader(value = "grupo") String grupo)
    {
        try
        {
            // Buscamos el profesor
            Profesor profesor = this.buscarProfesor(email) ;

            // Buscamos la asignatura
            Asignatura asignatura = this.buscarAsignatura(nombreAsignatura, curso, etapa, grupo) ;

            // Buscamos las sesiones base de la asignatura
            Optional<List<SesionBaseDto>> sesionesBaseOptional = 
                    this.generadorSesionBaseRepository.buscarSesionesBasePorAsignaturaProfesorDto(asignatura, profesor) ;

            return ResponseEntity.ok(sesionesBaseOptional.get()) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener las sesiones base";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método que busca un profesor por su email
     * @param email - Email del profesor
     * @return Profesor - Profesor encontrado
     * @throws SchoolManagerServerException
     */
    private Profesor buscarProfesor(String email) throws SchoolManagerServerException
    {
        Profesor profesor = this.profesorRepository.findByEmail(email) ;
                                
        if (profesor == null)
        {
            String mensajeError = "El profesor con email " + email + " no existe" ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.PROFESOR_NO_ENCONTRADO, mensajeError) ;
        }

        return profesor ;
    }

    /**
     * Método que busca una asignatura por su nombre, curso, etapa y grupo
     * @param nombreAsignatura - Nombre de la asignatura
     * @param curso - Curso
     * @param etapa - Etapa
     * @param grupo - Grupo
     * @return Asignatura - Asignatura encontrada
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private Asignatura buscarAsignatura(String nombreAsignatura, int curso, String etapa, String grupo) throws SchoolManagerServerException
    {
        Optional<Asignatura> asignaturaOptional = this.asignaturaRepository.findAsignaturasByCursoEtapaGrupoAndNombre(curso, etapa, grupo, nombreAsignatura) ;

        if (!asignaturaOptional.isPresent())
        {
            String mensajeError = "La asignatura con nombre " + nombreAsignatura + " no existe" ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, "La asignatura con nombre " + nombreAsignatura + " no existe") ;
        }

        return asignaturaOptional.get() ;
    }
    
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/lanzar")
    public ResponseEntity<?> arrancarGenerador()
    {
        try
        {
            // Realizamos una serie de validaciones previas 
            this.arrancarGeneradorValidacionesPrevias() ;

            // Creamos un nuevo generador en la base de datos
            Generador generador = new Generador() ;
            this.generadorRepository.saveAndFlush(generador) ;

            // Llamamos al método que configura y lanza el generador
            this.generadorService.configurarYarrancarGenerador() ;

            // Devolvemos un OK
            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            String mensajeError = "ERROR - No se pudo arrancar el generador";

            // Logueamos el error
            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

        /**
     * Método que realiza una serie de validaciones previas
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private void arrancarGeneradorValidacionesPrevias() throws SchoolManagerServerException
    {
        // Validaciones de datosprevias
        ValidadorDatosDto validadorDatosDto = this.validadorDatosService.validacionDatos() ;
        
        // Si hay mensajes de error, devolvemos un error
        if (!validadorDatosDto.getErroresDatos().isEmpty())
        {
            throw new SchoolManagerServerException(Constants.ERROR_VALIDACIONES_DATOS_INCORRECTOS, validadorDatosDto.getErroresDatos().toString()) ;
        }

        // Validamos si ya hay un generador en curso
        Optional<Generador> generadorEnCurso = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_GENERADOR_EN_CURSO) ;

        if (generadorEnCurso.isPresent())
        {
            String mensajeError = "Hay un generador en curso que fue lanzado el " + generadorEnCurso.get().getFechaInicio() ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.ERROR_CODE_GENERADOR_EN_CURSO, mensajeError) ;
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/forzarDetencion")
    public ResponseEntity<?> forzarDetencion()
    {
        try
        {
            // Buscamos el generador
            Optional<Generador> optionalGenerador = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_GENERADOR_EN_CURSO) ;

            // Si no existe, lanzamos una excepción
            if (!optionalGenerador.isPresent())
            {
                String mensajeError = "No hay un generador en curso" ;

                log.error(mensajeError) ;
                throw new SchoolManagerServerException(Constants.ERROR_CODE_NO_GENERADOR_EN_CURSO, mensajeError) ;
            }

            // Obtenemos el generador
            Generador generador = optionalGenerador.get() ;

            // Detenemos el generador, guardamos en la BBDD
            generador.pararGenerador(Constants.ESTADO_GENERADOR_DETENIDO) ;

            // Guardamos el generador en la BBDD
            this.generadorRepository.saveAndFlush(generador);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo forzar la detención del generador";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/soluciones")
    public ResponseEntity<?> seleccionarSolucion(@RequestHeader(value = "idGeneradorInstancia") Integer idGeneradorInstancia)
    {
        try
        {
            // Llamamos al servicio para seleccionar la solución
            this.generadorService.seleccionarSolucion(idGeneradorInstancia) ;

            // Devolvemos un OK
            return ResponseEntity.ok().build() ;
        }   
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo seleccionar la solución";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/soluciones")
    public ResponseEntity<?> eliminarSoluciones(@RequestHeader(value = "idGeneradorInstancia") Integer idGeneradorInstancia)
    {
        try 
        {
            // Buscamos la instancia del generador
            Optional<GeneradorInstancia> generadorInstanciaOptional = this.generadorInstanciaRepository.findById(idGeneradorInstancia) ;

            // Si no existe, devolvemos un error
            if (!generadorInstanciaOptional.isPresent())
            {
                String mensajeError = "La instancia del generador con id " + idGeneradorInstancia + " no existe" ;

                log.error(mensajeError) ;
                throw new SchoolManagerServerException(Constants.ERROR_CODE_GENERADOR_INSTANCIA_NO_ENCONTRADA, mensajeError) ;
            }

            // Eliminamos la instancia del generador y sus referencias
            this.generadorService.eliminarGeneradorInstancia(generadorInstanciaOptional.get()) ;

            // Devolvemos un OK
            return ResponseEntity.ok().build() ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {   
            String mensajeError = "ERROR - No se pudieron eliminar las soluciones";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
