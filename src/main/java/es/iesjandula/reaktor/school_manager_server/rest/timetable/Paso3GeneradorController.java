package es.iesjandula.reaktor.school_manager_server.rest.timetable;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.ValidadorDatosDto;
import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorInfoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorRestriccionesImpartir;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorRestriccionesReduccion;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorRestriccionesImpartir;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorRestriccionesReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.IDiaTramoTipoHorarioRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.generador.IGeneradorInstanciaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.generador.IGeneradorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.generador.IGeneradorRestriccionesImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.generador.IGeneradorRestriccionesReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.services.timetable.GeneradorService;
import es.iesjandula.reaktor.school_manager_server.services.timetable.ValidadorDatosService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/schoolManager/generador")
public class Paso3GeneradorController
{
    @Autowired
    private ValidadorDatosService validadorDatosService ;

    @Autowired
    private IGeneradorRepository generadorRepository ;

    @Autowired
    private IProfesorReduccionRepository profesorReduccionRepository ;

    @Autowired
    private IDiaTramoTipoHorarioRepository diaTramoTipoHorarioRepository ;

    @Autowired
    private IImpartirRepository impartirRepository ;
    
    @Autowired
    private IGeneradorRestriccionesImpartirRepository generadorRestriccionesImpartirRepository ;

    @Autowired
    private IGeneradorRestriccionesReduccionRepository generadorRestriccionesReduccionRepository ;

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
    @RequestMapping(method = RequestMethod.POST, value = "/restricciones_impartir")
    public ResponseEntity<?> actualizarRestriccionesImpartir(@RequestHeader(value = "email") String email,
                                                             @RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                                             @RequestHeader(value = "curso") int curso,
                                                             @RequestHeader(value = "etapa") String etapa,
                                                             @RequestHeader(value = "grupo") String grupo,
                                                             @RequestHeader(value = "numeroRestriccion") int numeroRestriccion,
                                                             @RequestHeader(value = "diaDesc") String diaDesc,
                                                             @RequestHeader(value = "tramoDesc") String tramoDesc)
    {
        try
        {
            // Buscamos la asignatura
            Impartir impartir = this.buscarImpartir(nombreAsignatura, curso, etapa, grupo) ;

            // Buscamos la restricción de tipo de horario por número de sesión, profesor y asignatura
            Optional<GeneradorRestriccionesImpartir> generadorRestriccionesImpartirOptional = this.generadorRestriccionesImpartirRepository.buscarRestriccionesPorNumeroRestriccionImpartir(numeroRestriccion, impartir) ;

            // Si existe, la borramos
            if (generadorRestriccionesImpartirOptional.isPresent())
            {
                // La borramos
                this.generadorRestriccionesImpartirRepository.delete(generadorRestriccionesImpartirOptional.get()) ;
                this.generadorRestriccionesImpartirRepository.flush() ;
            }

            // Obtenemos las restricciones de tipo de horario de la asignatura
            DiaTramoTipoHorario diaTramoTipoHorario = this.diaTramoTipoHorarioRepository.buscarPorDiaDescTramoDesc(diaDesc, tramoDesc) ;

            if (!Constants.SIN_SELECCIONAR.equals(diaDesc) && !Constants.SIN_SELECCIONAR.equals(tramoDesc))
            {
                // Actualizamos la restricción de tipo de horario
                this.actualizarRestriccionesImpartirInternal(numeroRestriccion, impartir, diaTramoTipoHorario) ;
            }

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo actualizar la restricción de asignatura";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método que actualiza una restricción de tipo de horario
     * @param numeroRestriccion - Número de la restricción
     * @param impartir - Impartir
     * @param diaTramoTipoHorario - Día y tramo de tipo horario
     */
    private void actualizarRestriccionesImpartirInternal(int numeroRestriccion, Impartir impartir, DiaTramoTipoHorario diaTramoTipoHorario)
    {
        // Creamos una instancia de IdGeneradorRestriccionesImpartir
        IdGeneradorRestriccionesImpartir idGeneradorRestriccionesImpartir = new IdGeneradorRestriccionesImpartir() ;
        
        // Asignamos los valores a la instancia
        idGeneradorRestriccionesImpartir.setNumeroRestriccion(numeroRestriccion) ;   
        idGeneradorRestriccionesImpartir.setImpartir(impartir) ;
        idGeneradorRestriccionesImpartir.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

        // Creamos una instancia de GeneradorRestriccionesImpartir
        GeneradorRestriccionesImpartir generadorRestriccionesImpartirInstancia = new GeneradorRestriccionesImpartir() ;
        
        // Asignamos cada parámetro a la instancia
        generadorRestriccionesImpartirInstancia.setIdGeneradorRestriccionesImpartir(idGeneradorRestriccionesImpartir) ;
        generadorRestriccionesImpartirInstancia.setImpartir(impartir) ;
        generadorRestriccionesImpartirInstancia.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

        // Guardamos la instancia en la base de datos
        this.generadorRestriccionesImpartirRepository.saveAndFlush(generadorRestriccionesImpartirInstancia) ; 
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/restricciones_reduccion")
    public ResponseEntity<?> actualizarRestriccionesReduccion(@RequestHeader(value = "email") String email,
                                                              @RequestHeader(value = "nombreReduccion") String nombreReduccion,
                                                              @RequestHeader(value = "curso") int curso,
                                                              @RequestHeader(value = "etapa") String etapa,
                                                              @RequestHeader(value = "grupo") String grupo,
                                                              @RequestHeader(value = "numeroReduccion") int numeroReduccion,
                                                              @RequestHeader(value = "diaDesc") String diaDesc,
                                                              @RequestHeader(value = "tramoDesc") String tramoDesc)
    {
        try
        {
            // Buscamos la reducción
            ProfesorReduccion profesorReduccion = this.buscarProfesorReduccion(nombreReduccion, curso, etapa, grupo) ;

            // Buscamos la restricción de reducción por número de reducción, profesor y reducción
            Optional<GeneradorRestriccionesReduccion> generadorRestriccionesReduccionOptional = this.generadorRestriccionesReduccionRepository.buscarRestriccionesPorNumeroRestriccionProfesorReduccion(numeroReduccion, profesorReduccion) ;

            // Si existe, la borramos
            if (generadorRestriccionesReduccionOptional.isPresent())
            {
                // La borramos
                this.generadorRestriccionesReduccionRepository.delete(generadorRestriccionesReduccionOptional.get()) ;
                this.generadorRestriccionesReduccionRepository.flush() ;
            }

            // Obtenemos las restricciones de tipo de horario de la asignatura
            DiaTramoTipoHorario diaTramoTipoHorario = this.diaTramoTipoHorarioRepository.buscarPorDiaDescTramoDesc(diaDesc, tramoDesc) ;

            if (!Constants.SIN_SELECCIONAR.equals(diaDesc) && !Constants.SIN_SELECCIONAR.equals(tramoDesc))
            {
                // Actualizamos la restricción de tipo de horario
                this.actualizarRestriccionesReduccionInternal(numeroReduccion, profesorReduccion, diaTramoTipoHorario) ;
            }

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo actualizar la restricción de reducción";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método que actualiza una restricción de reducción
     * @param numeroRestriccion - Número de la restricción
     * @param profesorReduccion - ProfesorReduccion
     * @param diaTramoTipoHorario - Día y tramo de tipo horario
     */
    private void actualizarRestriccionesReduccionInternal(int numeroRestriccion, ProfesorReduccion profesorReduccion, DiaTramoTipoHorario diaTramoTipoHorario)
    {
        // Creamos una instancia de IdGeneradorRestriccionesReduccion
        IdGeneradorRestriccionesReduccion idGeneradorRestriccionesReduccion = new IdGeneradorRestriccionesReduccion() ;
        
        // Asignamos los valores a la instancia
        idGeneradorRestriccionesReduccion.setNumeroRestriccion(numeroRestriccion) ;   
        idGeneradorRestriccionesReduccion.setProfesorReduccion(profesorReduccion) ;
        idGeneradorRestriccionesReduccion.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

        // Creamos una instancia de GeneradorRestriccionesReduccion
        GeneradorRestriccionesReduccion generadorRestriccionesReduccionInstancia = new GeneradorRestriccionesReduccion() ;
        
        // Asignamos cada parámetro a la instancia
        generadorRestriccionesReduccionInstancia.setIdGeneradorRestriccionesReduccion(idGeneradorRestriccionesReduccion) ;
        generadorRestriccionesReduccionInstancia.setProfesorReduccion(profesorReduccion) ;
        generadorRestriccionesReduccionInstancia.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

        // Guardamos la instancia en la base de datos
        this.generadorRestriccionesReduccionRepository.saveAndFlush(generadorRestriccionesReduccionInstancia) ; 
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/restricciones_impartir")
    public ResponseEntity<?> obtenerRestriccionesImpartir(@RequestHeader(value = "email") String email,
                                                          @RequestHeader(value = "nombreAsignatura") String nombreAsignatura,
                                                          @RequestHeader(value = "curso") int curso,
                                                          @RequestHeader(value = "etapa") String etapa,
                                                          @RequestHeader(value = "grupo") String grupo)
    {
        try
        {
            // Buscamos la asignatura
            Impartir impartir = this.buscarImpartir(nombreAsignatura, curso, etapa, grupo) ;

            // Buscamos las restricciones de asignatura
            Optional<List<GeneradorRestriccionBaseDto>> restriccionesImpartirOptional = 
                    this.generadorRestriccionesImpartirRepository.buscarRestriccionesPorImpartirDto(impartir) ;

            return ResponseEntity.ok(restriccionesImpartirOptional.get()) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener las restricciones de asignatura";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/restricciones_reduccion")
    public ResponseEntity<?> obtenerRestriccionesReduccion(@RequestHeader(value = "email") String email,
                                                           @RequestHeader(value = "nombreReduccion") String nombreReduccion,
                                                           @RequestHeader(value = "curso") int curso,
                                                           @RequestHeader(value = "etapa") String etapa,
                                                           @RequestHeader(value = "grupo") String grupo)
    {
        try
        {
            // Buscamos la reducción
            ProfesorReduccion profesorReduccion = this.buscarProfesorReduccion(nombreReduccion, curso, etapa, grupo) ;

            // Buscamos las restricciones de asignatura
            Optional<List<GeneradorRestriccionBaseDto>> restriccionesReduccionOptional = 
                    this.generadorRestriccionesReduccionRepository.buscarRestriccionesReduccionProfesorDto(profesorReduccion) ;

            return ResponseEntity.ok(restriccionesReduccionOptional.get()) ;
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron obtener las restricciones de reducción";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método que busca una asignatura impartida por su nombre, curso, etapa y grupo
     * @param nombreAsignatura - Nombre de la asignatura
     * @param curso - Curso
     * @param etapa - Etapa
     * @param grupo - Grupo
     * @return Impartir - Impartir encontrada
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private Impartir buscarImpartir(String nombreAsignatura, int curso, String etapa, String grupo) throws SchoolManagerServerException
    {
        Optional<Impartir> impartirOptional = 
          this.impartirRepository.encontrarImpartirPorNombreAndCursoAndEtapaAndGrupo(nombreAsignatura, curso, etapa, grupo) ;

        if (!impartirOptional.isPresent())
        {
            String mensajeError = "La asignatura con nombre " + nombreAsignatura + " no está asignada a ningún profesor " +
                                  "en el curso " + curso + " de la etapa " + etapa + " y grupo " + grupo ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.IMPARTIR_NO_ENCONTRADA, mensajeError) ;
        }

        return impartirOptional.get() ;
    }

    /**
     * Método que busca una reducción por su nombre, curso, etapa y grupo
     * @param nombreReduccion - Nombre de la reducción
     * @param curso - Curso
     * @param etapa - Etapa
     * @param grupo - Grupo
     * @return Reduccion - Reducción encontrada
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private ProfesorReduccion buscarProfesorReduccion(String nombreReduccion, int curso, String etapa, String grupo) throws SchoolManagerServerException
    {
        Optional<ProfesorReduccion> profesorReduccionOptional =
          this.profesorReduccionRepository.encontrarProfesorReduccionPorNombreHorasCursoEtapaGrupo(nombreReduccion, curso, etapa, grupo) ;

        if (!profesorReduccionOptional.isPresent())
        {
            String mensajeError = "La reducción con nombre " + nombreReduccion + " no está asignada a ningún profesor " +
                                  "en el curso " + curso + " de la etapa " + etapa + " y grupo " + grupo ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.REDUCCION_NO_ENCONTRADA, mensajeError) ;
        }

        return profesorReduccionOptional.get() ;
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
            this.generadorService.configurarYarrancarGenerador(true) ;

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
