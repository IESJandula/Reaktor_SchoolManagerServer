package es.iesjandula.reaktor.school_manager_server.generator.sesiones.creador;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionReduccion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHoraria;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CreadorSesiones
{
    /** Sesiones con restricciones horarias */
    private List<SesionBase> sesionesConRestriccionesHorarias ;

    /** Sesiones con conciliaciones */
    private List<SesionBase> sesionesConConciliaciones ;

    /** Sesiones con optativas */
    private List<SesionBase> sesionesConOptativas ;

    /** Sesiones sin restricciones */
    private List<SesionBase> sesionesSinRestricciones ;

    /**
     * Constructor vacío
     */
    public CreadorSesiones()
    {
        this.sesionesConRestriccionesHorarias = new ArrayList<SesionBase>() ;   
        this.sesionesConConciliaciones        = new ArrayList<SesionBase>() ;
        this.sesionesConOptativas             = new ArrayList<SesionBase>() ;
        this.sesionesSinRestricciones         = new ArrayList<SesionBase>() ;
    }

    /**
     * Añade a una de las listas de sesiones la asignatura y profesor
     * 
     * @param cursoEtapaGrupo curso etapa grupo
     * @param asignatura asignatura
     * @param profesor profesor
     * @param tipoHorarioMatutino true si el tipo de horario es matutino, false en caso contrario
     * @param restriccionHoraria restricción horaria
     */
    public void crearSesion(CursoEtapaGrupo cursoEtapaGrupo, Asignatura asignatura, Profesor profesor,
                            boolean tipoHorarioMatutino, RestriccionHoraria restriccionHoraria)
    {
        // Por defecto, la lista de sesiones elegida es la de sin restricciones
        List<SesionBase> listaSesionesElegida = this.sesionesSinRestricciones ;

        // Si la asignatura tiene restricciones horarias, la lista elegida es la de sesiones con restricciones horarias
        if (restriccionHoraria != null)
        {
            listaSesionesElegida = this.sesionesConRestriccionesHorarias ;
        }
        else if (profesor.getObservacionesAdicionales().getConciliacion() != null && profesor.getObservacionesAdicionales().getConciliacion())
        {
            listaSesionesElegida = this.sesionesConConciliaciones ;
        }
        else if (asignatura.isOptativa())
        {
            // Si es optativa, la lista elegida es la de sesiones con optativas
            listaSesionesElegida = this.sesionesConOptativas ;
        }

        // Añadimos la sesión a la lista elegida
        listaSesionesElegida.add(new SesionAsignatura(cursoEtapaGrupo, asignatura, profesor, tipoHorarioMatutino, restriccionHoraria)) ;
    }

    /**
     * Añade a una de las listas de sesiones la reduccion y profesor
     * 
     * @param cursoEtapaGrupo curso etapa grupo
     * @param reduccion reduccion
     * @param profesor profesor
     * @param tipoHorarioMatutino true si el tipo de horario es matutino, false en caso contrario
     * @param restriccionHoraria restricción horaria
     */
    public void crearSesion(CursoEtapaGrupo cursoEtapaGrupo,
        Reduccion reduccion, Profesor profesor, boolean tipoHorarioMatutino, RestriccionHoraria restriccionHoraria)
    {
        // Por defecto, la lista de sesiones elegida es la de sin restricciones
        List<SesionBase> listaSesionesElegida = this.sesionesSinRestricciones ;

        // Si la reduccion tiene restricciones horarias, la lista elegida es la de sesiones con restricciones horarias
        if (restriccionHoraria != null)
        {
            listaSesionesElegida = this.sesionesConRestriccionesHorarias ;
        }
        else if (profesor.getObservacionesAdicionales().getConciliacion() != null && profesor.getObservacionesAdicionales().getConciliacion())
        {
            listaSesionesElegida = this.sesionesConConciliaciones ;
        }

        // Añadimos la sesión a la lista elegida
        listaSesionesElegida.add(new SesionReduccion(cursoEtapaGrupo, reduccion, profesor, tipoHorarioMatutino, restriccionHoraria)) ;
    }

    /**
     * @return La lista de listas de sesiones
     */
    public List<List<SesionBase>> getListaDeListaSesiones()
    {
        List<List<SesionBase>> listaDeListas = new ArrayList<List<SesionBase>>() ;

        listaDeListas.add(this.sesionesSinRestricciones) ;
        listaDeListas.add(this.sesionesConOptativas) ;
        listaDeListas.add(this.sesionesConConciliaciones) ;
        listaDeListas.add(this.sesionesConRestriccionesHorarias) ;

        return listaDeListas ;
    }
}