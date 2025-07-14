package es.iesjandula.reaktor.school_manager_server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorInstanciaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorInstanciaSolucionInfoGeneralDto;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorInstanciaSolucionInfoProfesorDto;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorInfoDto;
import es.iesjandula.reaktor.school_manager_server.generator.core.CreadorSesiones;
import es.iesjandula.reaktor.school_manager_server.generator.core.Horario;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultados;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultadosParams;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreads;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreadsParams;
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Constantes;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstanciaSolucionInfoGeneral;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstanciaSolucionInfoProfesor;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionBase;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorInstanciaSolucionInfoGeneral;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorInstanciaSolucionInfoProfesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.repositories.IConstantesRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorInstanciaSolucionInfoGeneral;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorInstanciaSolucionInfoProfesor;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorInstanciaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorSesionAsignadaRepository;
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
    private IGeneradorSesionAsignadaRepository generadorSesionAsignadaRepository ;

    @Autowired
    private IGeneradorInstanciaRepository generadorInstanciaRepository ;

    @Autowired
    private IGeneradorInstanciaSolucionInfoGeneral generadorInstanciaSolucionInfoGeneralRepository ;

    @Autowired
    private IGeneradorInstanciaSolucionInfoProfesor generadorInstanciaSolucionInfoProfesorRepository ;

    @Autowired
    private IProfesorRepository profesorRepository ;

    @Autowired
    private IConstantesRepository constantesRepository ;

    @Autowired
    private AsignaturaService asignaturaService ;

    @Autowired
    private DiaTramoTipoHorarioService diaTramoTipoHorarioService ;

    /**
     * Método que arranca el generador
     * @throws SchoolManagerServerException
     */
    public void configurarYarrancarGenerador() throws SchoolManagerServerException
    {
        // Obtenemos el generador en curso
        Optional<Generador> optionalGenerador = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_GENERADOR_EN_CURSO) ;

        // Si no hay generador en curso, lanzamos una excepción
        if (!optionalGenerador.isPresent())
        {
            String mensajeError = "ERROR - No hay generador en curso" ; 

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, null);
        }

        // Obtenemos el generador
        Generador generador = optionalGenerador.get() ;

        // Obtengo todos los cursos, etapas y grupos de BBDD
        List<CursoEtapaGrupo> cursos = this.cursoEtapaGrupoRepository.buscarTodosLosCursosEtapasGrupos() ;
                    
        // Creamos dos mapas de correlacionador de cursos
        Map<String, Integer> mapCorrelacionadorCursosMatutinos   = new HashMap<String, Integer>() ;
        Map<String, Integer> mapCorrelacionadorCursosVespertinos = new HashMap<String, Integer>() ;
        
        // Creamos los mapas de correlacionador de cursos
        this.crearMapasGruposMatutinosVespertinos(cursos, mapCorrelacionadorCursosMatutinos, mapCorrelacionadorCursosVespertinos) ;

        // Obtenemos el número de cursos matutinos que hay
        int numeroCursosMatutinos = mapCorrelacionadorCursosMatutinos.size() ;
        
        // Creamos las sesiones
        CreadorSesiones creadorSesiones = this.crearSesiones(mapCorrelacionadorCursosMatutinos, mapCorrelacionadorCursosVespertinos) ;

        // Creamos una nueva instancia de GeneradorInstancia
        GeneradorInstancia generadorInstancia = new GeneradorInstancia() ;
        generadorInstancia.setGenerador(generador) ;

        // Guardamos la instancia en la base de datos
        this.generadorInstanciaRepository.saveAndFlush(generadorInstancia) ;

        // Creamos los manejadores y threads
        ManejadorThreads manejadorThreads = this.crearManejadorThreads(mapCorrelacionadorCursosMatutinos,
                                                                       mapCorrelacionadorCursosVespertinos,
                                                                       numeroCursosMatutinos,
                                                                       creadorSesiones,
                                                                       generadorInstancia) ;

        // Creamos las matrices de sesiones vacía, donde cada columna representa un curso en un día
    	Asignacion[][] asignacionesInicialesMatutinas   = null ;
		if (numeroCursosMatutinos > 0)
		{
			asignacionesInicialesMatutinas = new Asignacion[numeroCursosMatutinos * Constants.NUMERO_DIAS_SEMANA]
														   [Constants.NUMERO_TRAMOS_HORARIOS] ;

		}

		// Obtenemos el número de cursos vespertinos que hay
    	int numeroCursosVespertinos = mapCorrelacionadorCursosVespertinos.size() ;

    	Asignacion[][] asignacionesInicialesVespertinas = null ;
		if (numeroCursosVespertinos > 0)
		{
			asignacionesInicialesVespertinas = new Asignacion[numeroCursosVespertinos * Constants.NUMERO_DIAS_SEMANA]
														     [Constants.NUMERO_TRAMOS_HORARIOS] ;

		}
    	
    	// Lanzamos nuevos threads para procesar la siguiente clase
        manejadorThreads.lanzarNuevosThreads(creadorSesiones.getListaDeListaSesiones(), asignacionesInicialesMatutinas, asignacionesInicialesVespertinas, null) ;
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
            Optional<List<GeneradorSesionBase>> generadorRestriccionesBaseOptional = 
                    this.generadorSesionBaseRepository.buscarSesionesBasePorAsignaturaProfesor(impartir.getAsignatura(), impartir.getProfesor()) ;
            
            List<RestriccionHoraria> restriccionesHorarias = null ;

            // Si hay restricciones base, las añadimos a la lista
            if (generadorRestriccionesBaseOptional.isPresent())
            {
                // Creamos una lista de restricciones horarias
                restriccionesHorarias = new ArrayList<RestriccionHoraria>() ;

                // Obtenemos la lista de restricciones base
                List<GeneradorSesionBase> generadorSesionBaseList = generadorRestriccionesBaseOptional.get() ;

                // Obtenemos el curso, etapa y grupo de la asignatura, y si es matutino o vespertino
                CursoEtapaGrupo cursoEtapaGrupo = impartir.getAsignatura().getIdAsignatura().getCursoEtapaGrupo() ;
                boolean tipoHorarioMatutino     = cursoEtapaGrupo.getHorarioMatutino() ;

                // Iteramos para cada restricción base
                for (GeneradorSesionBase generadorSesionBase : generadorSesionBaseList)
                {
                    // Obtenemos el curso, etapa y grupo en formato String
                    String cursoEtapaGrupoString = cursoEtapaGrupo.getCursoEtapaGrupoString() ;

                    // Obtenemos el día y el tramo de la restricción base
                    int dia   = generadorSesionBase.getIdGeneradorSesionBase().getDiaTramoTipoHorario().getDia() ;
                    int tramo = generadorSesionBase.getIdGeneradorSesionBase().getDiaTramoTipoHorario().getTramo() ;

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
     * @param numeroCursosMatutinos - Número de cursos matutinos
     * @param creadorSesiones - Creador de sesiones
     * @return ManejadorThreads - Manejador de threads
     */
    private ManejadorThreads crearManejadorThreads(Map<String, Integer> mapCorrelacionadorCursosMatutinos,
                                                   Map<String, Integer> mapCorrelacionadorCursosVespertinos,
                                                   int numeroCursosMatutinos,
                                                   CreadorSesiones creadorSesiones,
                                                   GeneradorInstancia generadorInstancia)
    {
        // Obtenemos el mayor de los umbrales mínimos de las soluciones de BBDD
        int umbralMinimoSolucion = this.obtenerUmbralMinimoSolucion() ;

        ManejadorResultadosParams manejadorResultadosParams =  
            new ManejadorResultadosParams.Builder()
                                         .setUmbralMinimoSolucion(umbralMinimoSolucion)
                                         .setGeneradorService(this)
                                         .build();

        // Crear el manejador de resultados con los umbrales definidos en Constants
        ManejadorResultados manejadorResultados = new ManejadorResultados(manejadorResultadosParams) ;

        // Obtenemos el número de cursos vespertinos que tenemos en el instituto
        int numeroCursosVespertinos		  		= mapCorrelacionadorCursosVespertinos.size() ;

        ManejadorThreadsParams manejadorThreadsParams = 
        new ManejadorThreadsParams.Builder()
                                  .setNumeroCursosMatutinos(numeroCursosMatutinos)
                                  .setNumeroCursosVespertinos(numeroCursosVespertinos)
                                  .setFactorSesionesConsecutivasProfesor(this.obtenerFactorSesionesConsecutivas())
                                  .setMapCorrelacionadorCursosMatutinos(mapCorrelacionadorCursosMatutinos) // Mapa de correlacionador de cursos (debe ser rellenado con los datos reales)
                                  .setMapCorrelacionadorCursosVespertinos(mapCorrelacionadorCursosVespertinos) // Mapa de correlacionador de cursos (debe ser rellenado con los datos reales)
                                  .setPoolSize(Constants.THREAD_POOL_SIZE)                     // Tamaño del pool
                                  .setNumeroThreadPorIteracion(Constants.THREAD_POR_ITERACION) // Número de threads por iteración
                                  .setManejadorResultados(manejadorResultados)
                                  .setAsignaturaService(this.asignaturaService)
                                  .setGeneradorService(this)
                                  .setGeneradorInstancia(generadorInstancia)
                                  .build() ;

        return new ManejadorThreads(manejadorThreadsParams) ;
    }

    /**
     * Método que obtiene el umbral mínimo de soluciones
     * @return Umbral mínimo de soluciones
     */
    private int obtenerUmbralMinimoSolucion()
    {
        int umbralMinimoSolucion = 0 ;

        // Tratamos de obtener el umbral mínimo de soluciones de la tabla de constantes
        Optional<Constantes> optionalUmbralMinimoSolucion = this.constantesRepository.findByClave(Constants.TABLA_CONST_UMBRAL_MINIMO_SOLUCION) ;

        if (optionalUmbralMinimoSolucion.isPresent() && optionalUmbralMinimoSolucion.get().getValor() != null)
        {
            umbralMinimoSolucion = Integer.parseInt(optionalUmbralMinimoSolucion.get().getValor()) ;
        }

        // Obtenemos el umbral mínimo de soluciones de BBDD
        Optional<Integer> optionalMaximaPuntuacionSolucion = this.generadorInstanciaRepository.buscarMaximaPuntuacionSolucion() ;

        // Si hay umbral mínimo de soluciones, lo asignamos
        if (optionalMaximaPuntuacionSolucion.isPresent() && optionalMaximaPuntuacionSolucion.get() > umbralMinimoSolucion)
        {
            umbralMinimoSolucion = optionalMaximaPuntuacionSolucion.get() ;
        }

        return umbralMinimoSolucion ;
    }

    /**
     * Método que obtiene el factor de puntuación por número de sesiones consecutivas
     * @return Factor de puntuación por número de sesiones consecutivas
     */
    private int obtenerFactorSesionesConsecutivas()
    {
        int factorSesionesConsecutivas = 1 ;

        // Obtenemos el factor de puntuación por número de sesiones insertadas de la tabla de constantes
        Optional<Constantes> optionalFactorSesionesConsecutivas = this.constantesRepository.findByClave(Constants.TABLA_CONST_FACTOR_SESIONES_CONSECUTIVAS) ;

        if (optionalFactorSesionesConsecutivas.isPresent() && optionalFactorSesionesConsecutivas.get().getValor() != null)
        {
            factorSesionesConsecutivas = Integer.parseInt(optionalFactorSesionesConsecutivas.get().getValor()) ;
        }

        return factorSesionesConsecutivas ;
    }

    /**
     * Método que guarda una sesión asignada
     * @param generadorInstancia - Generador instancia
     * @param horario - Horario de la sesión asignada
     * @param puntuacionObtenida - Puntuación obtenida
     * @param mensajeInformacion - Mensaje de información
     */
    public void guardarHorario(GeneradorInstancia generadorInstancia, Horario horario, int puntuacionObtenida, String mensajeInformacion) throws SchoolManagerServerException
    {
        // Obtenemos los profesores de BBDD
        List<Profesor> profesores = this.profesorRepository.findAll() ;
        
        // Para cada profesor, actualizamos la tabla de información del profesor con los porcentajes de sesiones consecutivas
        for (Profesor profesor : profesores)
        {
            this.generadorInstanciaSolucionInfoProfesorRepository.actualizarPorcentajesSesionesInsertadasYConsecutivas(generadorInstancia.getId(), profesor.getEmail()) ;
        }

        // Actualizamos el GeneradorInstancia y el Generador en BBDD
        this.actualizarGeneradorYgeneradorInstancia(generadorInstancia, mensajeInformacion, puntuacionObtenida) ;

        // Actualizamos la tabla de asignación de sesiones
        this.actualizarGeneradorSesionAsignada(generadorInstancia, horario) ;
    }

    /**
     * Método que actualiza el GeneradorInstancia y el Generador en BBDD
     * @param generadorInstancia - Generador instancia
     * @param mensajeInformacion - Mensaje de información
     * @param puntuacionObtenida - Puntuación obtenida
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private void actualizarGeneradorYgeneradorInstancia(GeneradorInstancia generadorInstancia, String mensajeInformacion, int puntuacionObtenida) throws SchoolManagerServerException
    {
        if (Constants.MENSAJE_SOLUCION_ENCONTRADA.equals(mensajeInformacion))
        {
            // Actualizamos el GeneradorInstancia con el estado, puntuación y mensaje de información
            generadorInstancia.pararGeneradorInstancia(Constants.ESTADO_GENERADOR_FINALIZADO, puntuacionObtenida, mensajeInformacion) ;
            this.generadorInstanciaRepository.saveAndFlush(generadorInstancia) ;

            // Seleccionamos la solución
            this.seleccionarSolucionInternal(generadorInstancia) ;

            // Actualizamos el Generador con el estado a finalizado
            Generador generador = generadorInstancia.getGenerador() ;
            generador.pararGenerador(Constants.ESTADO_GENERADOR_FINALIZADO) ;
            this.generadorRepository.saveAndFlush(generador) ;
        }
        else
        {
            // Actualizamos el GeneradorInstancia con el estado a error
            generadorInstancia.pararGeneradorInstancia(Constants.ESTADO_GENERADOR_ERROR, puntuacionObtenida, mensajeInformacion) ;
            this.generadorInstanciaRepository.saveAndFlush(generadorInstancia) ;
        }
    }

    /**
     * Método que actualiza la tabla de asignación de sesiones
     * @param generadorInstancia - Generador instancia
     * @param horario - Horario
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private void actualizarGeneradorSesionAsignada(GeneradorInstancia generadorInstancia, Horario horario) throws SchoolManagerServerException  
    {
        // Si hay horario matutino, recorremos la matriz de asignaciones matutinas para insertar las sesiones asignadas
        if (horario.getHorarioParams().getMatrizAsignacionesMatutinas() != null)
        {
            for (int i = 0; i < horario.getHorarioParams().getMatrizAsignacionesMatutinas().length; i++)
            {
                for (int j = 0; j < horario.getHorarioParams().getMatrizAsignacionesMatutinas()[i].length; j++)
                {
                    if (horario.getHorarioParams().getMatrizAsignacionesMatutinas()[i][j] != null)
                    {
                        this.actualizarGeneradorSesionAsignadaInternal(horario, i, j, true, generadorInstancia) ;
                    }
                }
            }   
        }

        // Si hay horario vespertino, recorremos la matriz de asignaciones vespertinas para insertar las sesiones asignadas
        if (horario.getHorarioParams().getMatrizAsignacionesVespertinas() != null)
        {
            for (int i = 0; i < horario.getHorarioParams().getMatrizAsignacionesVespertinas().length; i++)
            {
                for (int j = 0; j < horario.getHorarioParams().getMatrizAsignacionesVespertinas()[i].length; j++)
                {
                    if (horario.getHorarioParams().getMatrizAsignacionesVespertinas()[i][j] != null)
                    {
                        this.actualizarGeneradorSesionAsignadaInternal(horario, i, j, false, generadorInstancia) ;
                    }
                }
            }
        }
    }

    /**
     * Método que guarda un horario
     * @param horario - Horario
     * @param i - Día
     * @param j - Tramo
     * @param horarioMatutino - True si es horario matutino, false si es horario vespertino
     * @param generadorInstancia - Generador instancia
     */
    private void actualizarGeneradorSesionAsignadaInternal(Horario horario, int i, int j, boolean horarioMatutino, GeneradorInstancia generadorInstancia) throws SchoolManagerServerException
    {
        // Obtenemos la asignación de la matriz de asignaciones matutinas
        Asignacion asignacion = horario.getHorarioParams().getMatrizAsignacionesMatutinas()[i][j] ;

        // Aplicamos el módulo 5 al día
        int dia = i % Constants.NUMERO_DIAS_SEMANA ;

        // Obtenemos el día y tramo de tipo horario
        DiaTramoTipoHorario diaTramoTipoHorario = this.diaTramoTipoHorarioService.obtenerDiaTramoTipoHorario(dia, j, horarioMatutino) ;

        // Iteramos por cada sesión de la asignación
        for (Sesion sesion : asignacion.getListaSesiones())
        {
            // Obtenemos el profesor y la asignatura de la sesión
            Profesor profesor = sesion.getProfesor() ;
            Asignatura asignatura = sesion.getAsignatura() ;

            // Creamos una instancia de IdGeneradorSesionAsignada
            IdGeneradorSesionAsignada idGeneradorSesionAsignada = new IdGeneradorSesionAsignada() ;

            // Asignamos los valores a la instancia
            idGeneradorSesionAsignada.setIdGeneradorInstancia(generadorInstancia.getId()) ;
            idGeneradorSesionAsignada.setProfesor(profesor) ;
            idGeneradorSesionAsignada.setAsignatura(asignatura) ;
            idGeneradorSesionAsignada.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

            // Creamos una instancia de GeneradorSesionAsignada
            GeneradorSesionAsignada generadorSesionAsignada = new GeneradorSesionAsignada() ;
            generadorSesionAsignada.setIdGeneradorSesionAsignada(idGeneradorSesionAsignada) ;

            // Asignamos los valores a la instancia
            generadorSesionAsignada.setGeneradorInstancia(generadorInstancia) ;
            generadorSesionAsignada.setAsignatura(asignatura) ;
            generadorSesionAsignada.setProfesor(profesor) ;
            generadorSesionAsignada.setDiaTramoTipoHorario(diaTramoTipoHorario) ;

            // Guardamos la instancia en la base de datos
            this.generadorSesionAsignadaRepository.saveAndFlush(generadorSesionAsignada) ;
        }
    }

    /**
     * Método que elimina un generador instancia
     * @param generadorInstancia - Generador instancia
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    @Transactional
    public void eliminarGeneradorInstancia(GeneradorInstancia generadorInstancia) throws SchoolManagerServerException
    {
        // Borramos por primera vez todas las soluciones generales de esta instancia
        this.generadorInstanciaSolucionInfoGeneralRepository.borrarPorIdGeneradorInstancia(generadorInstancia.getId()) ;

        // Borramos por primera vez todas las soluciones de profesores de esta instancia
        this.generadorInstanciaSolucionInfoProfesorRepository.borrarPorIdGeneradorInstancia(generadorInstancia.getId()) ;
        
        // Borramos por primera vez todas las sesiones asignadas de esta instancia
        this.generadorSesionAsignadaRepository.borrarPorIdGeneradorInstancia(generadorInstancia.getId()) ;

        // Eliminamos el GeneradorInstancia
        this.generadorInstanciaRepository.delete(generadorInstancia) ;
    }

    /**
     * Método que obtiene el estado del generador
     * @return GeneradorInfoDto - DTO con el estado del generador
     */
    public GeneradorInfoDto obtenerEstadoGenerador()
    {
        // Obtenemos el estado del generador y sus detalles generales
        GeneradorInfoDto generadorInfoDto = this.obtenerEstadoGeneradorDetallesGenerales() ;

        // Ahora obtenemos la información de las soluciones (si existieran)
        this.obtenerEstadoGeneradorInfoSoluciones(generadorInfoDto) ;

       return generadorInfoDto ;
    }

    /**
     * Método que obtiene el estado del generador y sus detalles generales
     * @return GeneradorInfoDto - DTO con el estado del generador y sus detalles generales
     */
    private GeneradorInfoDto obtenerEstadoGeneradorDetallesGenerales()
    {
        GeneradorInfoDto generadorInfoDto = new GeneradorInfoDto() ;

        // Verificamos si hay un generador en curso
        Optional<Generador> optionalGeneradorEnCurso = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_GENERADOR_EN_CURSO) ;

        // Si hay un generador en curso, seteamos el estado y la fecha de inicio
        if (optionalGeneradorEnCurso.isPresent())
        {
            // Obtenemos el generador en curso
            Generador generadorEnCurso = optionalGeneradorEnCurso.get() ;

            // Seteamos el estado del generador
            generadorInfoDto.setEstado(generadorEnCurso.getEstado()) ;

            // Seteamos la fecha de inicio del generador
            generadorInfoDto.setFechaInicio(generadorEnCurso.getFechaInicio()) ;
        }
        else // Si no hay un generador en curso, buscamos el último generador que se lanzó mediante la fecha de inicio
        {
            // Buscamos el último generador que se lanzó mediante la fecha de inicio
            Optional<Generador> optionalGeneradorDetenido = this.generadorRepository.buscarUltimoGeneradorLanzadoUsandoFechaInicio() ;

            if (optionalGeneradorDetenido.isPresent())
            {
                // Obtenemos el generador
                Generador generadorDetenido = optionalGeneradorDetenido.get() ;

                // Seteamos el estado del generador
                generadorInfoDto.setEstado(generadorDetenido.getEstado()) ;

                // Seteamos la fecha de inicio del generador
                generadorInfoDto.setFechaInicio(generadorDetenido.getFechaInicio()) ;

                // Seteamos la fecha de fin del generador
                generadorInfoDto.setFechaFin(generadorDetenido.getFechaFin()) ;
            }
            else
            {
                // Seteamos el estado del generador
                generadorInfoDto.setEstado(Constants.ESTADO_GENERADOR_DETENIDO) ;

                // Seteamos la fecha de inicio del generador
                generadorInfoDto.setFechaInicio(null) ;

                // Seteamos la fecha de fin del generador
                generadorInfoDto.setFechaFin(null) ;
            }
        }

        return generadorInfoDto ;
    }

    /**
     * Método que obtiene la información de las soluciones
     * @param generadorInfoDto - DTO con el estado del generador y sus detalles generales
     */
    private void obtenerEstadoGeneradorInfoSoluciones(GeneradorInfoDto generadorInfoDto)
    {
        // Buscamos todas aquellas instancias del generador que tengan una solución
        Optional<List<GeneradorInstancia>> optionalGeneradorInstancias = this.generadorInstanciaRepository.obtenerTodasLasPosiblesSoluciones() ;

        // Creamos una lista de soluciones
        List<GeneradorInstanciaDto> soluciones = new ArrayList<GeneradorInstanciaDto>() ;

        // Si hay soluciones, seteamos la información de las soluciones
        if (optionalGeneradorInstancias.isPresent())
        {
            // Obtenemos las soluciones
            List<GeneradorInstancia> generadorInstancias = optionalGeneradorInstancias.get() ;

            // Iteramos por cada solución
            for (GeneradorInstancia generadorInstancia : generadorInstancias)
            {
                this.obtenerEstadoGeneradorInfoSolucionesInternal(soluciones, generadorInstancia) ;                
            }
        }

        // Seteamos la lista de soluciones
        generadorInfoDto.setSoluciones(soluciones) ;
    }

    /**
     * Método que obtiene la información de una solución
     * @param soluciones - Lista de soluciones
     * @param generadorInstancia - Generador instancia
     */
    private void obtenerEstadoGeneradorInfoSolucionesInternal(List<GeneradorInstanciaDto> soluciones, GeneradorInstancia generadorInstancia)
    {
        // Creamos una instancia de GeneradorInstanciaDto
        GeneradorInstanciaDto generadorInstanciaDto = new GeneradorInstanciaDto() ;

        // Seteamos los valores de la instancia
        generadorInstanciaDto.setIdGeneradorInstancia(generadorInstancia.getId()) ;
        generadorInstanciaDto.setPuntuacion(generadorInstancia.getPuntuacion()) ;
        generadorInstanciaDto.setSolucionElegida(generadorInstancia.getSolucionElegida()) ;

        // Obtenemos la información de las puntuaciones generales
        this.obtenerEstadoGeneradorInfoSolucionesInternalGeneral(generadorInstanciaDto, generadorInstancia) ;

        // Obtenemos la información de las puntuaciones de profesores
        this.obtenerEstadoGeneradorInfoSolucionesInternalProfesores(generadorInstanciaDto, generadorInstancia) ;

        // Añadimos la instancia a la lista de soluciones
        soluciones.add(generadorInstanciaDto) ;
    }

    /**
     * Método que obtiene la información de las puntuaciones generales
     * @param generadorInstanciaDto - Generador instancia DTO
     * @param generadorInstancia - Generador instancia
     */
    private void obtenerEstadoGeneradorInfoSolucionesInternalGeneral(GeneradorInstanciaDto generadorInstanciaDto, GeneradorInstancia generadorInstancia)
    {
        // Buscamos para esta instancia todos los tipos de puntuaciones generales
        Optional<List<GeneradorInstanciaSolucionInfoGeneral>> optionalGeneradorInstanciaSolucionInfoGenerals = 
        this.generadorInstanciaSolucionInfoGeneralRepository.buscarPorGeneradorInstancia(generadorInstancia.getId()) ;

        // Si hay puntuaciones generales, seteamos la información de las puntuaciones generales
        if (optionalGeneradorInstanciaSolucionInfoGenerals.isPresent())
        {
            // Obtenemos las puntuaciones generales
            List<GeneradorInstanciaSolucionInfoGeneral> generadorInstanciaSolucionInfoGenerals = 
                                    optionalGeneradorInstanciaSolucionInfoGenerals.get() ;

            // Iteramos por cada puntuación general
            for (GeneradorInstanciaSolucionInfoGeneral generadorInstanciaSolucionInfoGeneral : generadorInstanciaSolucionInfoGenerals)
            {
                // Creamos una instancia de GeneradorInstanciaSolucionInfoGeneralDto
                GeneradorInstanciaSolucionInfoGeneralDto generadorInstanciaSolucionInfoGeneralDto = 
                                    new GeneradorInstanciaSolucionInfoGeneralDto() ;

                // Seteamos los valores de la instancia
                generadorInstanciaSolucionInfoGeneralDto.setTipo(generadorInstanciaSolucionInfoGeneral.getIdGeneradorInstanciaSolucionInfoGeneral().getTipo()) ;
                generadorInstanciaSolucionInfoGeneralDto.setPuntuacion(generadorInstanciaSolucionInfoGeneral.getPuntuacion()) ;
                generadorInstanciaSolucionInfoGeneralDto.setPorcentaje(generadorInstanciaSolucionInfoGeneral.getPorcentaje()) ;

                // Añadimos la puntuación general a la instancia
                generadorInstanciaDto.getPuntuacionesDesglosadas().add(generadorInstanciaSolucionInfoGeneralDto) ;
            }
        }
    }

    /**
     * Método que obtiene la información de las puntuaciones de profesores
     * @param generadorInstanciaDto - Generador instancia DTO
     * @param generadorInstancia - Generador instancia
     */
    private void obtenerEstadoGeneradorInfoSolucionesInternalProfesores(GeneradorInstanciaDto generadorInstanciaDto, GeneradorInstancia generadorInstancia)
    {
        // Buscamos para esta instancia todos los tipos de puntuaciones de profesores
        Optional<List<GeneradorInstanciaSolucionInfoProfesor>> optionalGeneradorInstanciaSolucionInfoProfesors = 
        this.generadorInstanciaSolucionInfoProfesorRepository.buscarPorGeneradorInstancia(generadorInstancia.getId()) ;

        // Si hay puntuaciones de profesores, seteamos la información de las puntuaciones de profesores
        if (optionalGeneradorInstanciaSolucionInfoProfesors.isPresent())
        {
            // Obtenemos las puntuaciones de profesores
            List<GeneradorInstanciaSolucionInfoProfesor> generadorInstanciaSolucionInfoProfesors = 
                            optionalGeneradorInstanciaSolucionInfoProfesors.get() ;

            // Iteramos por cada puntuación de profesor
            for (GeneradorInstanciaSolucionInfoProfesor generadorInstanciaSolucionInfoProfesor : generadorInstanciaSolucionInfoProfesors)
            {
                // Creamos una instancia de GeneradorInstanciaSolucionInfoProfesorDto
                GeneradorInstanciaSolucionInfoProfesorDto generadorInstanciaSolucionInfoProfesorDto =
                            new GeneradorInstanciaSolucionInfoProfesorDto() ;

                // Seteamos los valores de la instancia
                generadorInstanciaSolucionInfoProfesorDto.setEmailProfesor(generadorInstanciaSolucionInfoProfesor.getIdGeneradorInstanciaSolucionInfoProfesor().getProfesor().getEmail()) ;
                generadorInstanciaSolucionInfoProfesorDto.setTipo(generadorInstanciaSolucionInfoProfesor.getIdGeneradorInstanciaSolucionInfoProfesor().getTipo()) ;
                generadorInstanciaSolucionInfoProfesorDto.setPuntuacion(generadorInstanciaSolucionInfoProfesor.getPuntuacion()) ;
                generadorInstanciaSolucionInfoProfesorDto.setPorcentaje(generadorInstanciaSolucionInfoProfesor.getPorcentaje()) ;

                // Añadimos la puntuación de profesor a la instancia
                generadorInstanciaDto.getPuntuacionesDesglosadas().add(generadorInstanciaSolucionInfoProfesorDto) ;
            }
        }
    }

    /**
     * Método que guarda la información de la solución general
     * @param generadorInstancia - Generador instancia
     * @param tipo - Tipo de información de la solución
     * @param puntuacion - Puntuación de la solución
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public void guardarGeneradorInstanciaSolucionInfoGeneral(GeneradorInstancia generadorInstancia, String tipo, int puntuacion, double porcentaje) throws SchoolManagerServerException
    {        
        // Creamos una instancia de GeneradorInstanciaSolucionInfoGeneral
        GeneradorInstanciaSolucionInfoGeneral generadorInstanciaSolucionInfoGeneral = new GeneradorInstanciaSolucionInfoGeneral() ;

        // Creamos una instancia de IdGeneradorInstanciaSolucionInfoGeneral
        IdGeneradorInstanciaSolucionInfoGeneral idGeneradorInstanciaSolucionInfoGeneral = new IdGeneradorInstanciaSolucionInfoGeneral() ;
        
        // Seteamos los valores del ID compuesto
        idGeneradorInstanciaSolucionInfoGeneral.setGeneradorInstancia(generadorInstancia) ;
        idGeneradorInstanciaSolucionInfoGeneral.setTipo(tipo) ;

        // Seteamos los valores de la entidad
        generadorInstanciaSolucionInfoGeneral.setIdGeneradorInstanciaSolucionInfoGeneral(idGeneradorInstanciaSolucionInfoGeneral) ;
        generadorInstanciaSolucionInfoGeneral.setPuntuacion(puntuacion) ;
        generadorInstanciaSolucionInfoGeneral.setPorcentaje(porcentaje) ;

        // Guardamos la instancia en la base de datos
        this.generadorInstanciaSolucionInfoGeneralRepository.saveAndFlush(generadorInstanciaSolucionInfoGeneral) ;
    }

    /**
     * Método que guarda la información de la solución de un profesor
     * @param generadorInstancia - Generador instancia
     * @param profesor - Profesor
     * @param tipo - Tipo de información de la solución
     * @param puntuacion - Puntuación de la solución
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public void guardarGeneradorInstanciaSolucionInfoProfesor(GeneradorInstancia generadorInstancia, Profesor profesor, String tipo, int puntuacion) throws SchoolManagerServerException
    {
        // Creamos una instancia de GeneradorInstanciaSolucionInfoProfesor
        GeneradorInstanciaSolucionInfoProfesor generadorInstanciaSolucionInfoProfesor = new GeneradorInstanciaSolucionInfoProfesor() ;

        // Creamos una instancia de IdGeneradorInstanciaSolucionInfoProfesor
        IdGeneradorInstanciaSolucionInfoProfesor idGeneradorInstanciaSolucionInfoProfesor = new IdGeneradorInstanciaSolucionInfoProfesor() ;
        
        // Seteamos los valores del ID compuesto
        idGeneradorInstanciaSolucionInfoProfesor.setGeneradorInstancia(generadorInstancia) ;
        idGeneradorInstanciaSolucionInfoProfesor.setProfesor(profesor) ;
        idGeneradorInstanciaSolucionInfoProfesor.setTipo(tipo) ;

        // Buscamos si ya existe el la instancia de GeneradorInstanciaSolucionInfoProfesor
        Optional<GeneradorInstanciaSolucionInfoProfesor> optionalGeneradorInstanciaSolucionInfoProfesor = 
                this.generadorInstanciaSolucionInfoProfesorRepository.findById(idGeneradorInstanciaSolucionInfoProfesor) ;
        
        // Si ya existe, incrementamos la puntuación con el valor de la nueva puntuación
        if (optionalGeneradorInstanciaSolucionInfoProfesor.isPresent())
        {
            // Obtenemos la instancia
            generadorInstanciaSolucionInfoProfesor = optionalGeneradorInstanciaSolucionInfoProfesor.get() ;

            // Actualizamos la puntuación
            generadorInstanciaSolucionInfoProfesor.setPuntuacion(generadorInstanciaSolucionInfoProfesor.getPuntuacion() + puntuacion) ;
        }
        else // Si no existe, creamos una nueva instancia
        {
            // Seteamos los valores de la entidad
            generadorInstanciaSolucionInfoProfesor.setIdGeneradorInstanciaSolucionInfoProfesor(idGeneradorInstanciaSolucionInfoProfesor) ;
            generadorInstanciaSolucionInfoProfesor.setPuntuacion(puntuacion) ;
        }

        // Guardamos la instancia en la base de datos
        this.generadorInstanciaSolucionInfoProfesorRepository.saveAndFlush(generadorInstanciaSolucionInfoProfesor) ;
    }

    /**
     * Método que selecciona una solución
     * @param idGeneradorInstancia - ID de la instancia del generador
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    @Transactional
    public void seleccionarSolucion(Integer idGeneradorInstancia) throws SchoolManagerServerException
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

        // Actualizamos la instancia del generador
        GeneradorInstancia generadorInstancia = generadorInstanciaOptional.get() ;

        // Seleccionamos la solución
        this.seleccionarSolucionInternal(generadorInstancia) ;
    }

    private void seleccionarSolucionInternal(GeneradorInstancia generadorInstancia) throws SchoolManagerServerException
    {
        // Cualquier solución que haya sido elegida, la deseleccionamos
        this.generadorInstanciaRepository.deseleccionarSoluciones() ;

        // Actualizamos la instancia del generador
        generadorInstancia.setSolucionElegida(true) ;

        // Guardamos la instancia en la base de datos
        this.generadorInstanciaRepository.saveAndFlush(generadorInstancia) ;
    }   
}
