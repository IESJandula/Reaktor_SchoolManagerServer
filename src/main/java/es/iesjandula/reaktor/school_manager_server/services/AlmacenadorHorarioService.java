package es.iesjandula.reaktor.school_manager_server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.generator.core.Horario;
import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.repositories.IGeneradorSesionAsignadaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import jakarta.annotation.PostConstruct;

@Service
public class AlmacenadorHorarioService
{
    @Autowired
    private DiasTramosTipoHorarioService diasTramosTipoHorarioService ;

    @Autowired
    private IGeneradorSesionAsignadaRepository generadorSesionAsignadaRepository ;

    @Autowired
    private GeneradorService generadorService ;

    /** Identificador del generador */
    private Integer idGenerador ;


    @PostConstruct
    public void init() throws SchoolManagerServerException  
    {
        this.idGenerador = this.generadorService.obtenerGeneradorEnCurso().getId() ;
    }
    
    /**
     * Método que guarda una sesión asignada
     * @param horario - Horario de la sesión asignada
     * @param puntuacionObtenida - Puntuación obtenida
     * @param mensajeInformacion - Mensaje de información
     */
    public void guardarHorario(Horario horario, int puntuacionObtenida, String mensajeInformacion) throws SchoolManagerServerException
    {
        // Actualizamos el estado del generador
        this.generadorService.actualizarGeneradorEnBBDD(mensajeInformacion, Constants.ESTADO_FINALIZADO) ;

        // Si hay horario matutino, recorremos la matriz de asignaciones matutinas para insertar las sesiones asignadas
        if (horario.getHorarioParams().getMatrizAsignacionesMatutinas() != null)
        {
            for (int i = 0; i < horario.getHorarioParams().getMatrizAsignacionesMatutinas().length; i++)
            {
                for (int j = 0; j < horario.getHorarioParams().getMatrizAsignacionesMatutinas()[i].length; j++)
                {
                    this.guardarHorarioInternal(horario, i, j, true) ;
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
                    this.guardarHorarioInternal(horario, i, j, false) ;
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
     */
    private void guardarHorarioInternal(Horario horario, int i, int j, boolean horarioMatutino) throws SchoolManagerServerException
    {
        // Obtenemos la asignación de la matriz de asignaciones matutinas
        Asignacion asignacion = horario.getHorarioParams().getMatrizAsignacionesMatutinas()[i][j] ;

        // Obtenemos el día y tramo de tipo horario
        DiasTramosTipoHorario diasTramosTipoHorario = this.diasTramosTipoHorarioService.obtenerDiasTramosHorario(i, j, horarioMatutino) ;

        // Iteramos por cada sesión de la asignación
        for (Sesion sesion : asignacion.getListaSesiones())
        {
            // Obtenemos el profesor y la asignatura de la sesión
            Profesor profesor = sesion.getProfesor() ;
            Asignatura asignatura = sesion.getAsignatura() ;

            // Creamos una instancia de IdGeneradorSesionAsignada
            IdGeneradorSesionAsignada idGeneradorSesionAsignada = new IdGeneradorSesionAsignada() ;

            // Asignamos los valores a la instancia
            idGeneradorSesionAsignada.setIdGeneracion(this.idGenerador) ;
            idGeneradorSesionAsignada.setProfesor(profesor) ;
            idGeneradorSesionAsignada.setAsignatura(asignatura) ;
            idGeneradorSesionAsignada.setDiasTramosTipoHorario(diasTramosTipoHorario) ;

            // Creamos una instancia de GeneradorSesionAsignada
            GeneradorSesionAsignada generadorSesionAsignada = new GeneradorSesionAsignada() ;
            generadorSesionAsignada.setIdGeneradorSesionAsignada(idGeneradorSesionAsignada) ;

            // Asignamos los valores a la instancia
            generadorSesionAsignada.setAsignatura(asignatura) ;
            generadorSesionAsignada.setProfesor(profesor) ;

            // Guardamos la instancia en la base de datos
            this.generadorSesionAsignadaRepository.saveAndFlush(generadorSesionAsignada) ;
        }
    }
}