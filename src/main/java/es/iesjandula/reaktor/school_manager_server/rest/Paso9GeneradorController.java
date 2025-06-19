package es.iesjandula.reaktor.school_manager_server.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.generator.core.CreadorSesiones;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultados;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultadosParams;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreads;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreadsParams;
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionesBase;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRestriccionesBase;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.services.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.services.ValidadorDatosService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/schoolManager/generador")
public class Paso9GeneradorController
{

    @Autowired
    private ValidadorDatosService validadorDatosService ;

    @Autowired
    private IGeneradorRepository generadorRepository ;

    @Autowired
    private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository ;   

    @Autowired
    private IImpartirRepository impartirRepository ;

    @Autowired
    private IGeneradorRestriccionesBase generadorRestriccionesBase ;

    @Autowired
    private AsignaturaService asignaturaService ; 
    
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/lanzar")
    public ResponseEntity<?> arrancarGenerador()
    {
        try
        {
            // Realizamos una serie de validaciones previas 
            this.validacionesPrevias() ;

            // Obtengo todos los cursos, etapas y grupos de BBDD
            List<CursoEtapaGrupo> cursos = this.cursoEtapaGrupoRepository.buscarTodosLosCursosEtapasGrupos() ;
            
            // Creamos dos mapas de correlacionador de cursos
            Map<String, Integer> mapCorrelacionadorCursosMatutinos   = new HashMap<String, Integer>() ;
            Map<String, Integer> mapCorrelacionadorCursosVespertinos = new HashMap<String, Integer>() ;
            
            // Creamos los mapas de correlacionador de cursos
            this.crearMapasGruposMatutinosVespertinos(cursos, mapCorrelacionadorCursosMatutinos, mapCorrelacionadorCursosVespertinos) ;
            
            // Creamos las sesiones
            CreadorSesiones creadorSesiones = this.crearSesiones(mapCorrelacionadorCursosMatutinos, mapCorrelacionadorCursosVespertinos) ;

            // Creamos los manejadores y threads
            ManejadorThreads manejadorThreads = this.crearManejadorThreads(mapCorrelacionadorCursosMatutinos,
                                                                            mapCorrelacionadorCursosVespertinos,
                                                                            creadorSesiones) ;
            // Creamos una nueva generación
            Generador generador = new Generador();
            generador.setFechaInicio(new Date());
            generador.setEstado(Constants.ESTADO_EN_CURSO);

            this.generadorRepository.saveAndFlush(generador);

            // Iniciamos el proceso
            manejadorThreads.iniciarProceso() ;

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

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/forzarDetencion")
    public ResponseEntity<?> forzarDetencion()
    {
        try
        {
            Optional<Generador> generador = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_EN_CURSO);

            if (generador.isPresent())
            {
                generador.get().setEstado(Constants.ESTADO_DETENIDO);
                generador.get().setFechaFin(new Date());

                this.generadorRepository.saveAndFlush(generador.get());
            }

            return ResponseEntity.ok().build();
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales  
            String mensajeError = "ERROR - No se pudo forzar la detención del generador";

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
    private void validacionesPrevias() throws SchoolManagerServerException
    {
        // Validaciones previas
        List<String> mensajesError = this.validadorDatosService.validacionDatos() ;
        
        // Si hay mensajes de error, devolvemos un error
        if (!mensajesError.isEmpty())
        {
            throw new SchoolManagerServerException(Constants.ERROR_VALIDACIONES_DATOS_INCORRECTOS, mensajesError.toString()) ;
        }

        // Validaciones previas del generador
        this.validacionesPreviasGenerador() ;
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private void validacionesPreviasGenerador() throws SchoolManagerServerException
    {
        // Validamos si ya hay un generador en curso
        Optional<Generador> generadorEnCurso = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_EN_CURSO) ;

        if (generadorEnCurso.isPresent())
        {
            String mensajeError = "Hay un generador en curso que fue lanzado el " + generadorEnCurso.get().getFechaInicio() ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.ERROR_CODE_GENERADOR_EN_CURSO, mensajeError) ;
        }
    }

    /**
     * Método que crea los mapas de correlacionador de cursos matutinos y vespertinos
     * @param cursos - Lista de cursos
     * @param mapCorrelacionadorCursosMatutinos - Mapa de correlacionador de cursos matutinos
     * @param mapCorrelacionadorCursosVespertinos - Mapa de correlacionador de cursos vespertinos
     */
    private void crearMapasGruposMatutinosVespertinos(List<CursoEtapaGrupo> cursos,
                                                        Map<String, Integer> mapCorrelacionadorCursosMatutinos,
                                                        Map<String, Integer> mapCorrelacionadorCursosVespertinos)
    {
        // Creamos dos índices para los mapas que irán incrementandose de 5 en 5
        int indiceMatutino = 0 ;
        int indiceVespertino = 0 ;

        // Realizo un bucle para distinguir entre matutinos y vespertinos
        for (CursoEtapaGrupo curso : cursos)
        {
            if (curso.getHorarioMatutino())
            {
                mapCorrelacionadorCursosMatutinos.put(curso.getCursoEtapaGrupoString(), indiceMatutino) ;
                indiceMatutino = indiceMatutino + 5 ;
            }
            else
            {
                mapCorrelacionadorCursosVespertinos.put(curso.getCursoEtapaGrupoString(), indiceVespertino) ;
                indiceVespertino = indiceVespertino + 5 ;
            }
        }
    }
    
    /**
     * Método que crea las sesiones
     * @param mapCorrelacionadorCursosMatutinos - Mapa de correlacionador de cursos matutinos
     * @param mapCorrelacionadorCursosVespertinos - Mapa de correlacionador de cursos vespertinos
     * @return CreadorSesiones - Creador de sesiones
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private CreadorSesiones crearSesiones(Map<String, Integer> mapCorrelacionadorCursosMatutinos,
                                          Map<String, Integer> mapCorrelacionadorCursosVespertinos) throws SchoolManagerServerException
    {
        // Creamos una instancia de CreadorSesiones para añadir las asignatura y profesor a la sesión específica
        CreadorSesiones creadorSesiones = new CreadorSesiones() ;
        
        // Obtenemos toda la configuración de impartición de asignaturas y profesores de BBDD
        List<Impartir> impartirList = this.impartirRepository.findAll() ;

        // Para cada fila de impartir, verificamos si existe algún tipo de restricción base
        for (Impartir impartir : impartirList)
        {
            // Obtenemos la lista de restricciones base de la asignatura de BBDD
            Optional<List<GeneradorSesionesBase>> restriccionesBaseOptional = this.generadorRestriccionesBase.buscarRestriccionesBasePorIdImpartir(impartir.getIdImpartir()) ;
            
            List<RestriccionHoraria> restriccionesHorarias = null ;

            // Si hay restricciones base, las añadimos a la lista
            if (restriccionesBaseOptional.isPresent())
            {
                // Creamos una lista de restricciones horarias
                restriccionesHorarias = new ArrayList<RestriccionHoraria>() ;

                // Obtenemos la lista de restricciones base
                List<GeneradorSesionesBase> restriccionesBaseList = restriccionesBaseOptional.get() ;

                // Obtenemos el curso, etapa y grupo de la asignatura, y si es matutino o vespertino
                CursoEtapaGrupo cursoEtapaGrupo = impartir.getAsignatura().getIdAsignatura().getCursoEtapaGrupo() ;
                boolean tipoHorarioMatutino     = cursoEtapaGrupo.getHorarioMatutino() ;

                // Iteramos para cada restricción base
                for (GeneradorSesionesBase restriccionBase : restriccionesBaseList)
                {
                    // Obtenemos el curso, etapa y grupo en formato String
                    String cursoEtapaGrupoString = cursoEtapaGrupo.getCursoEtapaGrupoString() ;

                    // Obtenemos el día y el tramo de la restricción base
                    // Obtenemos el día y el tramo de la restricción base
                    int dia   = restriccionBase.getIdGeneradorSesionesBase().getDiasTramosTipoHorario().getDia() ;
                    int tramo = restriccionBase.getIdGeneradorSesionesBase().getDiasTramosTipoHorario().getTramo() ;

                    // Vemos si el tipo de horario es matutino o vespertino
                    if (tipoHorarioMatutino)
                    {
                        // Añadimos la restricción horaria a la lista
                    restriccionesHorarias.add(new RestriccionHoraria.Builder(mapCorrelacionadorCursosMatutinos.get(cursoEtapaGrupoString))
                                                                    .asignarUnDiaTramoConcreto(dia, tramo)
                                                                    .build()) ;
                    }
                    else
                    {
                        // Añadimos la restricción horaria a la lista
                        restriccionesHorarias.add(new RestriccionHoraria.Builder(mapCorrelacionadorCursosVespertinos.get(cursoEtapaGrupoString))
                                                                        .asignarUnDiaTramoConcreto(dia, tramo)
                                                                        .build()) ;
                    }
                }

                // Creamos el conjunto de sesiones asociadas a la asignatura y profesor
                creadorSesiones.crearSesiones(impartir.getAsignatura(), impartir.getProfesor(), tipoHorarioMatutino, restriccionesHorarias) ;
            }
        }

        return creadorSesiones ;
    }

    /**
     * Método que crea los manejadores y threads
     * @param mapCorrelacionadorCursosMatutinos - Mapa de correlacionador de cursos matutinos
     * @param mapCorrelacionadorCursosVespertinos - Mapa de correlacionador de cursos vespertinos
     * @param creadorSesiones - Creador de sesiones
     * @return ManejadorThreads - Manejador de threads
     */
    private ManejadorThreads crearManejadorThreads(Map<String, Integer> mapCorrelacionadorCursosMatutinos,
                                                   Map<String, Integer> mapCorrelacionadorCursosVespertinos,
                                                   CreadorSesiones creadorSesiones)
    {
        ManejadorResultadosParams manejadorResultadosParams =  new ManejadorResultadosParams.Builder()
                                .setUmbralMinimoSolucion(Constants.UMBRAL_MINIMO_SOLUCION)
                                .setUmbralMinimoError(Constants.UMBRAL_MINIMO_ERROR)
                                .build();

        // Crear el manejador de resultados con los umbrales definidos en Constants
        ManejadorResultados manejadorResultados = new ManejadorResultados(manejadorResultadosParams) ;

        // Obtenemos el número de cursos matutinos que tenemos en el instituto
        int numeroCursosMatutinos		  	    = mapCorrelacionadorCursosMatutinos.size() ;
        int numeroCursosVespertinos		  		= mapCorrelacionadorCursosVespertinos.size() ;

        ManejadorThreadsParams manejadorThreadsParams = 
        new ManejadorThreadsParams.Builder()
                                  .setNumeroCursosMatutinos(numeroCursosMatutinos)
                                  .setNumeroCursosVespertinos(numeroCursosVespertinos)
                                  .setFactorNumeroSesionesInsertadas(Constants.FACTOR_NUMERO_SESIONES_INSERTADAS) // Factor de puntuación por número de sesiones insertadas
                                  .setFactorSesionesConsecutivasProfesor(Constants.FACTOR_SESIONES_CONSECUTIVAS_PROFESOR) // Factor de puntuación por sesiones consecutivas de profesor
                                  .setFactorSesionesConsecutivasProfesorMatVes(Constants.FACTOR_SESIONES_CONSECUTIVAS_PROFESOR_MAT_VES) // Factor de puntuación por sesiones consecutivas de profesor en la primera hora vespertina
                                  .setMapCorrelacionadorCursosMatutinos(mapCorrelacionadorCursosMatutinos) // Mapa de correlacionador de cursos (debe ser rellenado con los datos reales)
                                  .setMapCorrelacionadorCursosVespertinos(mapCorrelacionadorCursosVespertinos) // Mapa de correlacionador de cursos (debe ser rellenado con los datos reales)
                                  .setPoolSize(Constants.THREAD_POOL_SIZE)                     // Tamaño del pool
                                  .setNumeroThreadPorIteracion(Constants.THREAD_POR_ITERACION) // Número de threads por iteración
                                  .setManejadorResultados(manejadorResultados)
                                  .setAsignaturaService(this.asignaturaService)
                                  .build() ;

        // Obtenemos la lista de listas de sesiones
        List<List<Sesion>> listaListaSesiones = creadorSesiones.getListaDeListaSesiones() ;

        return new ManejadorThreads(manejadorThreadsParams, listaListaSesiones) ;
    }
}
