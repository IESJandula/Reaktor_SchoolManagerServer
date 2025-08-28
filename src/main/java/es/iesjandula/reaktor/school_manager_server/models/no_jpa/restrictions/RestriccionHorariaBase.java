package es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions;

import java.util.Iterator;
import java.util.List;

/**
 * Clase base para las restricciones horarias
 */
public class RestriccionHorariaBase
{
    /** Restricciones horarias no evitables */
    private final List<RestriccionHorariaItem> restriccionesHorariasNoEvitables ;

    /** Restricciones horarias evitables */
    private final List<RestriccionHorariaItem> restriccionesHorariasEvitables ;

    /**
     * Constructor
     * 
     * @param restriccionesHorariasNoEvitables restricciones horarias no evitables
     * @param restriccionesHorariasEvitables restricciones horarias evitables
     */
    protected RestriccionHorariaBase(final List<RestriccionHorariaItem> restriccionesHorariasNoEvitables,
                                     final List<RestriccionHorariaItem> restriccionesHorariasEvitables)
    {
        this.restriccionesHorariasNoEvitables = restriccionesHorariasNoEvitables ;
        this.restriccionesHorariasEvitables   = restriccionesHorariasEvitables ;
    }

    /**
     * @return restricciones horarias no evitables
     */
    public List<RestriccionHorariaItem> getRestriccionesHorariasNoEvitables()
    {
        return this.restriccionesHorariasNoEvitables ;
    }

    /**
     * @return restricciones horarias evitables
     */
    public List<RestriccionHorariaItem> getRestriccionesHorariasEvitables()
    {
        return this.restriccionesHorariasEvitables ;
    }

    /**
     * Este método se debe llamar cuando se configura el generador de horarios antes de lanzarlo
     * 
     * @param indiceCursoDia índice del curso y día
     * @param indiceTramoHorario índice del tramo horario
     */
    public void asignarUnDiaTramoConcreto(int indiceCursoDia, int indiceTramoHorario)
    {
        this.hacerCoincidirEnDiaHora(indiceCursoDia, indiceTramoHorario) ;
    }

    /**
     * Hace coincidir el tramo horario con el día
     * 
     * @param indiceCursoDia índice del curso y día
     * @param indiceTramoHorario índice del tramo horario
     */
    protected void hacerCoincidirEnDiaHora(int indiceCursoDia, int indiceTramoHorario)
    {
        // Primero eliminamos de las no evitables
        Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getIndiceDia() != indiceCursoDia || restriccionHorariaItem.getTramoHorario() != indiceTramoHorario)
            {
                // La eliminamos de la lista de no evitables
                iterator.remove() ;
            }
        }

        // Ahora eliminamos de las evitables
        iterator = this.restriccionesHorariasEvitables.iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getIndiceDia() != indiceCursoDia || restriccionHorariaItem.getTramoHorario() != indiceTramoHorario)
            {
                // La eliminamos de la lista de evitables
                iterator.remove() ;
            }
        }
    }
}
