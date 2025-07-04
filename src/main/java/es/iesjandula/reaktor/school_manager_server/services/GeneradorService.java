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
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionBase;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.repositories.IConstantesRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorInstanciaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorSesionAsignadaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorSesionBaseRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import jakarta.annotation.PostConstruct;

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
    private IConstantesRepository constantesRepository ;

    @Autowired
    private AsignaturaService asignaturaService ;

    @Autowired
    private DiaTramoTipoHorarioService diaTramoTipoHorarioService ;


    /**
     * Método que realiza una serie de validaciones previas
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public void validarNoHayGeneradorEnCurso() throws SchoolManagerServerException
    {
        // Validamos si ya hay un generador en curso
        Optional<Generador> generadorEnCurso = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_GENERADOR_EN_CURSO) ;

        if (generadorEnCurso.isPresent())
        {
            String mensajeError = "Hay un generador en curso que fue lanzado el " + generadorEnCurso.get().getFechaInicio() ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.ERROR_CODE_GENERADOR_EN_CURSO, mensajeError) ;
        }
    }

    /**
     * Método que obtiene el generador en curso
     * @return Generador - Generador en curso
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public Generador obtenerGeneradorEnCurso() throws SchoolManagerServerException
    {
        // Buscamos el generador
        Optional<Generador> generador = this.generadorRepository.buscarGeneradorPorEstado(Constants.ESTADO_GENERADOR_EN_CURSO) ;

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
     * Método que arranca el generador
     * @throws SchoolManagerServerException
     */
    public void configurarYarrancarGenerador() throws SchoolManagerServerException
    {
        // Obtenemos el generador en curso
        Generador generador = this.obtenerGeneradorEnCurso() ;

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
        // Obtenemos el mayor de los umbrales mínimos tanto para los errores como para las soluciones de BBDD
        int umbralMinimoError    = this.obtenerUmbralMinimoError() ;
        int umbralMinimoSolucion = this.obtenerUmbralMinimoSolucion() ;

        ManejadorResultadosParams manejadorResultadosParams =  
            new ManejadorResultadosParams.Builder()
                                         .setUmbralMinimoSolucion(umbralMinimoSolucion)
                                         .setUmbralMinimoError(umbralMinimoError)
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
                                  .setFactorNumeroSesionesInsertadas(Constants.FACTOR_NUMERO_SESIONES_INSERTADAS) // Factor de puntuación por número de sesiones insertadas
                                  .setFactorSesionesConsecutivasProfesor(Constants.FACTOR_SESIONES_CONSECUTIVAS_PROFESOR) // Factor de puntuación por sesiones consecutivas de profesor
                                  .setFactorSesionesConsecutivasProfesorMatVes(Constants.FACTOR_SESIONES_CONSECUTIVAS_PROFESOR_MAT_VES) // Factor de puntuación por sesiones consecutivas de profesor en la primera hora vespertina
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

    private int obtenerUmbralMinimoError()
    {
        int umbralMinimoError = 0 ;

        // Tratamos de obtener el umbral mínimo de errores de la tabla de constantes
        Optional<Constantes> optionalUmbralMinimoError = this.constantesRepository.findByClave(Constants.TABLA_CONST_UMBRAL_MINIMO_ERROR) ;

        if (optionalUmbralMinimoError.isPresent() && optionalUmbralMinimoError.get().getValor() != null)
        {
            umbralMinimoError = Integer.parseInt(optionalUmbralMinimoError.get().getValor()) ;
        }

        // Obtenemos el umbral mínimo de errores de BBDD
        Optional<Integer> optionalMaximaPuntuacionError = this.generadorInstanciaRepository.buscarMaximaPuntuacionError() ;

        // Si hay umbral mínimo de errores, lo asignamos
        if (optionalMaximaPuntuacionError.isPresent() && optionalMaximaPuntuacionError.get() > umbralMinimoError)
        {
            umbralMinimoError = optionalMaximaPuntuacionError.get() ;
        }
    
        return umbralMinimoError ;
    }

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
     * Método que guarda una sesión asignada
     * @param generadorInstancia - Generador instancia
     * @param horario - Horario de la sesión asignada
     * @param puntuacionObtenida - Puntuación obtenida
     * @param mensajeInformacion - Mensaje de información
     */
    public void guardarHorario(GeneradorInstancia generadorInstancia, Horario horario, int puntuacionObtenida, String mensajeInformacion) throws SchoolManagerServerException
    {
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
    public void eliminarGeneradorInstancia(GeneradorInstancia generadorInstancia) throws SchoolManagerServerException
    {
        // Eliminamos el GeneradorInstancia
        this.generadorInstanciaRepository.delete(generadorInstancia) ;
    }
}
