package es.iesjandula.reaktor.school_manager_server.generator.core;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CreadorSesiones
{
    /** Sesiones con restricciones horarias */
    private List<Sesion> sesionesConRestriccionesHorarias ;

    /** Sesiones con conciliaciones */
    private List<Sesion> sesionesConConciliaciones ;

    /** Sesiones con optativas */
    private List<Sesion> sesionesConOptativas ;

    /** Sesiones sin restricciones */
    private List<Sesion> sesionesSinRestricciones ;

    /**
     * Constructor vacío
     */
    public CreadorSesiones()
    {
        this.sesionesConRestriccionesHorarias = new ArrayList<Sesion>() ;   
        this.sesionesConConciliaciones        = new ArrayList<Sesion>() ;
        this.sesionesConOptativas             = new ArrayList<Sesion>() ;
        this.sesionesSinRestricciones         = new ArrayList<Sesion>() ;
    }

    /**
     * Añade a una de las listas de sesiones la asignatura y profesor
     * 
     * @param asignatura asignatura
     * @param profesor profesor
     * @param tipoHorarioMatutino true si el tipo de horario es matutino, false en caso contrario
     * @param restriccionesHorarias restricciones horarias
     * @throws SchoolManagerServerException con un error
     */ 
    public void crearSesiones(Asignatura asignatura, Profesor profesor, 
                              boolean tipoHorarioMatutino, List<RestriccionHoraria> restriccionesHorarias) throws SchoolManagerServerException
    {
        // Validamos las restricciones horarias para que no supere el límite de horas de la asignatura
        this.validarRestriccionesHorarias(asignatura, restriccionesHorarias) ;

        // Iteramos sobre las horas de la semana de la asignatura
        // Para cada una, creamos una nueva sesión y la añadimos a la lista elegida
        for (int i=0 ; i < asignatura.getHoras() ; i++)
        {
            RestriccionHoraria restriccionHoraria = null ;

            // Si hay restricciones horarias, y el número de restricciones horarias es mayor que el índice, se coge la restricción horaria
            if (restriccionesHorarias != null && restriccionesHorarias.size() > i)
            {
                restriccionHoraria = restriccionesHorarias.get(i) ;
            }

            // Creamos la sesión
            this.crearSesion(asignatura, profesor, tipoHorarioMatutino, restriccionHoraria) ;
        }
    }

    /**
     * Valida que el número de restricciones horarias no supere el límite de horas de la asignatura
     * 
     * @param asignatura asignatura
     * @param restriccionesHorarias restricciones horarias
     * @throws SchoolManagerServerException con un error
     */
    public void validarRestriccionesHorarias(Asignatura asignatura, List<RestriccionHoraria> restriccionesHorarias) throws SchoolManagerServerException
    {
        if (restriccionesHorarias != null && restriccionesHorarias.size() > asignatura.getHoras())
        {
            String errorString = "Superado el límite de restricciones que se pueden asignar a esta asignatura por sus horas";
            
            log.error(errorString);
            throw new SchoolManagerServerException(Constants.ERR_CODE_SUPERADO_LIMITE_RESTRICCIONES, errorString);
        }
    }

    /**
     * Añade a una de las listas de sesiones la asignatura y profesor
     * 
     * @param asignatura asignatura
     * @param profesor profesor
     * @param tipoHorarioMatutino true si el tipo de horario es matutino, false en caso contrario
     * @param restriccionHoraria restricción horaria
     */
    public void crearSesion(Asignatura asignatura, Profesor profesor, boolean tipoHorarioMatutino, RestriccionHoraria restriccionHoraria)
    {
        // Por defecto, la lista de sesiones elegida es la de sin restricciones
        List<Sesion> listaSesionesElegida = this.sesionesSinRestricciones ;

        // Si la asignatura tiene restricciones horarias, la lista elegida es la de sesiones con restricciones horarias
        if (restriccionHoraria != null)
        {
            listaSesionesElegida = this.sesionesConRestriccionesHorarias ;
        }
        else if (profesor.getConciliacion() != null)
        {
            listaSesionesElegida = this.sesionesConConciliaciones ;
        }
        else if (asignatura.isOptativa())
        {
            // Si es optativa, la lista elegida es la de sesiones con optativas
            listaSesionesElegida = this.sesionesConOptativas ;
        }

        // Añadimos la sesión a la lista elegida
        listaSesionesElegida.add(new Sesion(asignatura, profesor, tipoHorarioMatutino, restriccionHoraria)) ;
    }

    /**
     * @return La lista de listas de sesiones
     */
    public List<List<Sesion>> getListaDeListaSesiones()
    {
        List<List<Sesion>> listaDeListas = new ArrayList<List<Sesion>>() ;

        listaDeListas.add(this.sesionesSinRestricciones) ;
        listaDeListas.add(this.sesionesConOptativas) ;
        listaDeListas.add(this.sesionesConConciliaciones) ;
        listaDeListas.add(this.sesionesConRestriccionesHorarias) ;

        return listaDeListas ;
    }
}