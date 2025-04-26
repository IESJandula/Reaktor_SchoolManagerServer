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
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.generator.models.enums.TipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionesBase;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorRestriccionesBase;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pasoXGenerador")
public class PasoXGenerador
{
    @Autowired
    private IGeneradorRepository generadorRepository ;

    @Autowired
    private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository ;

    @Autowired
    private IProfesorRepository profesorRepository ;

    @Autowired
    private IAsignaturaRepository asignaturaRepository ;    

    @Autowired
    private IImpartirRepository impartirRepository ;

    @Autowired
    private IGeneradorRestriccionesBase generadorRestriccionesBase ;

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/arrancarGenerador")
    public ResponseEntity<?> arrancarGenerador()
    {
        try
        {
            // Creamos una instancia de CreadorSesiones para añadir las asignatura y profesor a la sesión específica
            CreadorSesiones creadorSesiones = new CreadorSesiones() ;

            // Obtengo todos los cursos, etapas y grupos de BBDD
            List<CursoEtapaGrupo> cursos = this.cursoEtapaGrupoRepository.findAll() ;
            
            // Creamos dos mapas de correlacionador de cursos
            Map<String, Integer> mapCorrelacionadorCursosMatutinos   = new HashMap<String, Integer>() ;
            Map<String, Integer> mapCorrelacionadorCursosVespertinos = new HashMap<String, Integer>() ;

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

                    // Iteramos para cada restricción base
                    for (GeneradorSesionesBase restriccionBase : restriccionesBaseList)
                    {
                        // Añadimos la restricción horaria a la lista
                        restriccionesHorarias.add(new RestriccionHoraria.Builder(mapCorrelacionadorCursosMatutinos.get(impartir.getIdImpartir().getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getCursoEtapaGrupoString()))
                                                                        .asignarUnDiaTramoConcreto(restriccionBase.getIdGeneradorSesionesBase().getDiasTramosTipoHorario().getDia(), restriccionBase.getIdGeneradorSesionesBase().getDiasTramosTipoHorario().getTramo())
                                                                        .build()) ;
                    }

                    // Obtenemos el tipo de horario en base al curso
                    TipoHorario tipoHorario = TipoHorario.getTipoHorario(impartir.getAsignatura().getIdAsignatura().getCursoEtapaGrupo().getCursoEtapaGrupoString()) ;

                    // Creamos el conjunto de sesiones asociadas a la asignatura y profesor
                    //creadorSesiones.crearSesiones(impartir.getAsignatura(), impartir.getProfesor(), tipoHorario, restriccionesHorarias) ;
                }
            }

            // Creamos una nueva generación
            Generador generador = new Generador();
            generador.setFechaInicio(new Date());

            generadorRepository.save(generador);

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
}
