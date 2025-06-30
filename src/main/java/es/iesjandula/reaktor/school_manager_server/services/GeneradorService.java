package es.iesjandula.reaktor.school_manager_server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.dtos.SesionBaseDto;
import es.iesjandula.reaktor.school_manager_server.generator.core.CreadorSesiones;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultados;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultadosParams;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreads;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreadsParams;
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorSesionBaseRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;

@Service
@Slf4j
public class GeneradorService
{
    @Autowired
    private IGeneradorRepository generadorRepository ;

    @Autowired
    private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository ;

    @Autowired
    private IImpartirRepository impartirRepository ;

    @Autowired
    private IGeneradorSesionBaseRepository generadorSesionBaseRepository ;

    @Autowired
    private AlmacenadorHorarioService almacenadorHorarioService ;

    @Autowired
    private AsignaturaService asignaturaService ;

    /**
     * Método que obtiene el generador en curso
     * @return Generador - Generador en curso
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public Generador obtenerGeneradorEnCurso() throws SchoolManagerServerException
    {
        // Buscamos el generador
        Optional<Generador> generador = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_EN_CURSO) ;

        // Si no existe, lanzamos una excepción
        if (!generador.isPresent())
        {
            String mensajeError = "No hay un generador en curso" ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.ERROR_CODE_NO_GENERADOR_EN_CURSO, mensajeError) ;
        }

        return generador.get() ;
    }

    /**
     * Método que actualiza el generador en la BBDD y genera el DTO
     * @param mensajeInformacion - Mensaje de información
     * @param estado - Estado del generador
     * @return Generador - Generador
     */
    public Generador actualizarGeneradorEnBBDD(String mensajeInformacion, String estado)
    {
        // Si hay un error, detenemos el generador
        Optional<Generador> generadorOptional = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_EN_CURSO) ;

        Generador generador = generadorOptional.get() ;

        // Actualizamos el estado del generador, parándolo
        generador.pararGenerador(estado, mensajeInformacion) ; 

        // Guardamos el generador en la base de datos
        this.generadorRepository.saveAndFlush(generador) ;

        // Devolvemos el generador
        return generador ;
    }

    /**
     * Método que arranca el generador
     * @throws SchoolManagerServerException
     */
    public void arrancarGenerador() throws SchoolManagerServerException
    {
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

        // Guardamos el generador en la base de datos
        this.generadorRepository.saveAndFlush(generador);

        // Iniciamos el proceso
        manejadorThreads.iniciarProceso() ;
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
            Optional<List<SesionBaseDto>> restriccionesBaseOptional = this.generadorSesionBaseRepository.buscarSesionesBasePorAsignaturaProfesor(impartir.getAsignatura(), impartir.getProfesor()) ;
            
            List<RestriccionHoraria> restriccionesHorarias = null ;

            // Si hay restricciones base, las añadimos a la lista
            if (restriccionesBaseOptional.isPresent())
            {
                // Creamos una lista de restricciones horarias
                restriccionesHorarias = new ArrayList<RestriccionHoraria>() ;

                // Obtenemos la lista de restricciones base
                List<SesionBaseDto> sesionBaseList = restriccionesBaseOptional.get() ;

                // Obtenemos el curso, etapa y grupo de la asignatura, y si es matutino o vespertino
                CursoEtapaGrupo cursoEtapaGrupo = impartir.getAsignatura().getIdAsignatura().getCursoEtapaGrupo() ;
                boolean tipoHorarioMatutino     = cursoEtapaGrupo.getHorarioMatutino() ;

                // Iteramos para cada restricción base
                for (SesionBaseDto restriccionBase : sesionBaseList)
                {
                    // Obtenemos el curso, etapa y grupo en formato String
                    String cursoEtapaGrupoString = cursoEtapaGrupo.getCursoEtapaGrupoString() ;

                    // Obtenemos el día y el tramo de la restricción base
                    int dia   = restriccionBase.getDia() ;
                    int tramo = restriccionBase.getTramo() ;

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
                                .setAlmacenadorHorarioService(this.almacenadorHorarioService)
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
