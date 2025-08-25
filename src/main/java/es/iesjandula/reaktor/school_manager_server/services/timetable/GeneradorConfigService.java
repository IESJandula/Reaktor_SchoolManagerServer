package es.iesjandula.reaktor.school_manager_server.services.timetable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorReduccionConRestriccionesDto;
import es.iesjandula.reaktor.school_manager_server.generator.sesiones.creador.CreadorSesiones;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorRestriccionesImpartir;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.generador.IGeneradorRestriccionesImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GeneradorConfigService
{
    @Autowired
    private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository ;

    @Autowired
    private IProfesorReduccionRepository iProfesorReduccionRepository ;

    @Autowired
    private IImpartirRepository iImpartirRepository ;

    @Autowired
    private IGeneradorRestriccionesImpartirRepository generadorRestriccionesImpartirRepository ;

    /** Mapa de correlacionador de cursos matutinos */
    private Map<String, Integer> mapCorrelacionadorCursosMatutinos ;

    /** Mapa de correlacionador de cursos vespertinos */
    private Map<String, Integer> mapCorrelacionadorCursosVespertinos ;

    /** Creador de sesiones */
    private CreadorSesiones creadorSesiones ;

    /**
     * Método que obtiene el mapa de correlacionador de cursos matutinos
     * @return Map<String, Integer> - Mapa de correlacionador de cursos matutinos
     */
    public Map<String, Integer> getMapCorrelacionadorCursosMatutinos()
    {
        return this.mapCorrelacionadorCursosMatutinos ;
    }

    /**
     * Método que obtiene el mapa de correlacionador de cursos vespertinos
     * @return Map<String, Integer> - Mapa de correlacionador de cursos vespertinos
     */
    public Map<String, Integer> getMapCorrelacionadorCursosVespertinos()
    {
        return this.mapCorrelacionadorCursosVespertinos ;
    }

    /**
     * Método que obtiene el creador de sesiones
     * @return CreadorSesiones - Creador de sesiones
     */
    public CreadorSesiones getCreadorSesiones()
    {
        return this.creadorSesiones ;
    }

    /**
     * Método que configura el generador
     * @param recargarDatos - Recargar datos
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public void configurarGenerador(boolean recargarDatos) throws SchoolManagerServerException
    {
        // Si se recargan los datos, creamos los mapas de correlacionador de cursos y las sesiones
        if (recargarDatos)
        {
            // Obtengo todos los cursos, etapas y grupos de BBDD
            List<CursoEtapaGrupo> cursos = this.cursoEtapaGrupoRepository.buscarTodosLosCursosEtapasGruposSinOptativas() ;
                
            // Creamos dos mapas de correlacionador de cursos
            this.mapCorrelacionadorCursosMatutinos   = new HashMap<String, Integer>() ;
            this.mapCorrelacionadorCursosVespertinos = new HashMap<String, Integer>() ;
            
            // Creamos los mapas de correlacionador de cursos
            this.crearMapasGruposMatutinosVespertinos(cursos) ;
            
            // Creamos las sesiones
            this.creadorSesiones = this.crearSesiones() ;
        }
    }

    /**
     * Método que crea los mapas de correlacionador de cursos matutinos y vespertinos
     * @param cursos - Lista de cursos
     */
    private void crearMapasGruposMatutinosVespertinos(List<CursoEtapaGrupo> cursos)
    {
        // Creamos dos índices para los mapas que irán incrementandose de 5 en 5
        int indiceMatutino = 0 ;
        int indiceVespertino = 0 ;

        // Realizo un bucle para distinguir entre matutinos y vespertinos
        for (CursoEtapaGrupo curso : cursos)
        {
            if (curso.getHorarioMatutino())
            {
                this.mapCorrelacionadorCursosMatutinos.put(curso.getCursoEtapaGrupoString(), indiceMatutino) ;
                indiceMatutino = indiceMatutino + 5 ;
            }
            else
            {
                this.mapCorrelacionadorCursosVespertinos.put(curso.getCursoEtapaGrupoString(), indiceVespertino) ;
                indiceVespertino = indiceVespertino + 5 ;
            }
        }
    }
    
    /**
     * Método que crea las sesiones
     * @return CreadorSesiones - Creador de sesiones
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    private CreadorSesiones crearSesiones() throws SchoolManagerServerException
    {
        // Creamos una instancia de CreadorSesiones para añadir las asignatura y profesor a la sesión específica
        CreadorSesiones creadorSesiones = new CreadorSesiones() ;

        // Creamos las sesiones asociadas a impartir
        this.crearSesionesAsociadasAImpartir(creadorSesiones) ;

        // Creamos las sesiones asociadas a reducciones
        this.crearSesionesAsociadasAReducciones(creadorSesiones) ;

        return creadorSesiones ;
    }

    /**
     * Método que crea las sesiones asociadas a impartir
     * @param creadorSesiones - Creador de sesiones
     * @throws SchoolManagerServerException 
     */
    private void crearSesionesAsociadasAImpartir(CreadorSesiones creadorSesiones) throws SchoolManagerServerException
    {
        // Obtenemos todas las filas de la tabla Impartir con las preferencias horarias del profesor cargadas eager
        Optional<List<Impartir>> impartirOptional = this.iImpartirRepository.findAllWithPreferenciasHorarias() ;

        // Si hay filas en la tabla Impartir
        if (impartirOptional.isPresent())
        {
            // Creamos una lista de Impartir
            List<Impartir> impartirList = impartirOptional.get() ;

            // Iteramos para cada Impartir
            for (Impartir impartir : impartirList)
            {
                // Vemos si es horario matutino o vespertino
                boolean tipoHorarioMatutino = impartir.isHorarioMatutino() ;

                // Si la asignatura es optativa ...
                if (impartir.isOptativa())
                {
                    // Primero recorreremos todos los cursos para crear tantas sesiones como grupos tenga la asignatura
                    this.crearSesionesAsociadasAImpartirOptativa(creadorSesiones, impartir, tipoHorarioMatutino) ;
                }
                else
                {
                    // Asignatura no optativa
                    this.crearSesionesAImpartir(creadorSesiones, impartir, tipoHorarioMatutino, impartir.getCursoEtapaGrupo()) ;
                }
            }
        }
    }

    /**
     * Método que crea las sesiones asociadas a la asignatura optativa
     * @param creadorSesiones - Creador de sesiones
     * @param impartir - Impartir
     * @param tipoHorarioMatutino - Tipo de horario
     */
    private void crearSesionesAsociadasAImpartirOptativa(CreadorSesiones creadorSesiones, Impartir impartir, boolean tipoHorarioMatutino)
    {
        // ... obtenemos el curso y la etapa
        int curso    = impartir.getCurso() ;
        String etapa = impartir.getEtapa() ;
        
        // ... obtenemos todos los grupos asociados a este curso y etapa
        List<CursoEtapaGrupo> cursosEtapaGrupoDto = 
            this.cursoEtapaGrupoRepository.buscarTodosLosCursosEtapasGruposSinOptativas(curso, etapa) ;
        
        // ... creamos tantas sesiones como grupos tenga la asignatura
        for (int i = 0 ; i < cursosEtapaGrupoDto.size() ; i++)
        {
            // ... obtenemos el curso, etapa y grupo
            CursoEtapaGrupo cursoEtapaGrupo = cursosEtapaGrupoDto.get(i) ;

            // Creamos las sesiones a impartir
            this.crearSesionesAImpartir(creadorSesiones, impartir, tipoHorarioMatutino, cursoEtapaGrupo) ;
        }
    }

    /**
     * Método que crea las sesiones asociadas a la asignatura no optativa
     * @param creadorSesiones - Creador de sesiones
     * @param impartir - Impartir
     * @param tipoHorarioMatutino - Tipo de horario
     * @param cursoEtapaGrupo - Curso etapa grupo
     */
    public void crearSesionesAImpartir(CreadorSesiones creadorSesiones, Impartir impartir, boolean tipoHorarioMatutino, CursoEtapaGrupo cursoEtapaGrupo)
    {
        RestriccionHoraria restriccionHoraria = null ;

        // Iteramos por cada hora de la asignatura
        for (int i = 0 ; i < impartir.getHorasTotalesAsignatura() ; i++)
        {
            // Buscamos la restricción de tipo de horario por número de sesión, profesor y asignatura
            Optional<GeneradorRestriccionesImpartir> generadorRestriccionesImpartirOptional = 
                this.generadorRestriccionesImpartirRepository.buscarRestriccionesPorNumeroRestriccionImpartir(i + 1, impartir) ;

            // Si existe, obtenemos el día y el tramo de la restricción
            if (generadorRestriccionesImpartirOptional.isPresent())
            {
                // Obtenemos el día y el tramo de la restricción
                int dia   = generadorRestriccionesImpartirOptional.get().getDiaTramoTipoHorario().getDia() ;
                int tramo = generadorRestriccionesImpartirOptional.get().getDiaTramoTipoHorario().getTramo() ;

                // ... obtenemos el curso, etapa y grupo en formato String
                String cursoEtapaGrupoString = cursoEtapaGrupo.getCursoEtapaGrupoString() ;

                // ... calculamos las restricciones asociadas
                restriccionHoraria = this.crearSesionesAsociadasAImpartirIRestriccionesHorarias(dia, tramo, cursoEtapaGrupoString, tipoHorarioMatutino) ;
            }

            // Creamos la sesión asociada a la asignatura y profesor en este grupo concreto
            creadorSesiones.crearSesion(cursoEtapaGrupo, impartir.getAsignatura(), impartir.getProfesor(), tipoHorarioMatutino, restriccionHoraria) ;
        }
    }

    /**
     * Método que crea las sesiones asociadas a la asignatura optativa
     * @param diaRestriccion - Día de la restricción
     * @param tramoRestriccion - Tramo de la restricción
     * @param cursoEtapaGrupoString - Curso, etapa y grupo en formato String
     * @param tipoHorarioMatutino - Tipo de horario
     * @return RestriccionHoraria - Restricción horaria
     */
    private RestriccionHoraria crearSesionesAsociadasAImpartirIRestriccionesHorarias(int diaRestriccion,
                                                                                     int tramoRestriccion,
                                                                                     String cursoEtapaGrupoString,
                                                                                     boolean tipoHorarioMatutino)
    {
        RestriccionHoraria.Builder restriccionHorariaBuilder = null ;

        // Vemos si el tipo de horario es matutino o vespertino
        if (tipoHorarioMatutino)
        {
            // Añadimos la restricción horaria a la lista
            restriccionHorariaBuilder = new RestriccionHoraria.Builder(this.mapCorrelacionadorCursosMatutinos.get(cursoEtapaGrupoString))
                                                              .asignarUnDiaTramoConcreto(diaRestriccion, tramoRestriccion) ;
        }
        else
        {
            // Añadimos la restricción horaria a la lista
            restriccionHorariaBuilder = new RestriccionHoraria.Builder(this.mapCorrelacionadorCursosVespertinos.get(cursoEtapaGrupoString))
                                                              .asignarUnDiaTramoConcreto(diaRestriccion, tramoRestriccion) ;
        }

        return restriccionHorariaBuilder.build() ;
    }

    /**
     * Método que crea las sesiones asociadas a reducciones
     * @param creadorSesiones - Creador de sesiones
     * @throws SchoolManagerServerException con un error
     */
    private void crearSesionesAsociadasAReducciones(CreadorSesiones creadorSesiones) throws SchoolManagerServerException
    {
        // Obtenemos todas las restricciones de impartir
        Optional<List<GeneradorReduccionConRestriccionesDto>> generadorReduccionConRestriccionesDtoOptional = 
            this.iProfesorReduccionRepository.obtenerReduccionesConRestricciones() ;

        // Si hay restricciones, las añadimos a la lista
        if (generadorReduccionConRestriccionesDtoOptional.isPresent())
        {
            // Creamos una lista de restricciones horarias
            List<GeneradorReduccionConRestriccionesDto> generadorReduccionConRestriccionesDtoList = generadorReduccionConRestriccionesDtoOptional.get() ;

            // Iteramos para cada restricción
            for (GeneradorReduccionConRestriccionesDto generadorReduccionConRestriccionesDto : generadorReduccionConRestriccionesDtoList)
            {
                // Obtenemos el curso, etapa y grupo de la asignatura, y si es matutino o vespertino
                CursoEtapaGrupo cursoEtapaGrupo = generadorReduccionConRestriccionesDto.getCursoEtapaGrupo() ;
                boolean tipoHorarioMatutino     = cursoEtapaGrupo.getHorarioMatutino() ;

                // Obtenemos el curso, etapa y grupo en formato String
                String cursoEtapaGrupoString = cursoEtapaGrupo.getCursoEtapaGrupoString() ;

                // Obtenemos el día y el tramo de la restricción
                int dia   = generadorReduccionConRestriccionesDto.getDiaTramoTipoHorario().getDia() ;
                int tramo = generadorReduccionConRestriccionesDto.getDiaTramoTipoHorario().getTramo() ;

                RestriccionHoraria restriccionHoraria = null ;

                // Vemos si el tipo de horario es matutino o vespertino
                if (tipoHorarioMatutino)
                {
                    // Añadimos la restricción horaria a la lista
                    restriccionHoraria = new RestriccionHoraria.Builder(this.mapCorrelacionadorCursosMatutinos.get(cursoEtapaGrupoString))
                                                               .asignarUnDiaTramoConcreto(dia, tramo)
                                                               .build() ;
                }
                else
                {
                    // Añadimos la restricción horaria a la lista
                    restriccionHoraria = new RestriccionHoraria.Builder(this.mapCorrelacionadorCursosVespertinos.get(cursoEtapaGrupoString))
                                                               .asignarUnDiaTramoConcreto(dia, tramo)
                                                               .build() ;
                }

                // Creamos el conjunto de sesiones asociadas a la asignatura y profesor
                creadorSesiones.crearSesion(cursoEtapaGrupo,
                                            generadorReduccionConRestriccionesDto.getReduccion(),
                                            generadorReduccionConRestriccionesDto.getProfesor(),
                                            tipoHorarioMatutino,
                                            restriccionHoraria) ;
            }
        }
    }
}
