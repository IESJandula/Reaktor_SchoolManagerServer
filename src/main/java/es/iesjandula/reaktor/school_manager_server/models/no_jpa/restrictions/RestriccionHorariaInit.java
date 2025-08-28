package es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.PreferenciasHorariasProfesor;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RestriccionHorariaInit extends RestriccionHorariaBase
{
    /**
     * @param indiceCursoDiaInicial Índice Día curso inicial
     */
    public RestriccionHorariaInit(int indiceCursoDiaInicial)
    {
        super(new ArrayList<RestriccionHorariaItem>(), new ArrayList<RestriccionHorariaItem>()) ;

        // Por defecto, damos de alta todas las posibilidades en las no evitables
        for (int i = indiceCursoDiaInicial ; i < (indiceCursoDiaInicial + Constants.NUMERO_DIAS_SEMANA) ; i++)
        {
            for (int j = Constants.TRAMO_HORARIO_PRIMERA_HORA ; j < Constants.NUMERO_TRAMOS_HORARIOS ; j++)
            {
                this.getRestriccionesHorariasNoEvitables().add(new RestriccionHorariaItem(i, j)) ;
            }
        }
    }

    /**
     * Este es un método que se llama cuando se configura el generador de horarios antes de lanzarlo
     */
    public void tratarEvitarClasePrimeraHora()
    {
        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;

        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_PRIMERA_HORA)
            {
                // La eliminamos de la lista de no evitables
                iterator.remove() ;

                // Añadimos la restricción horaria a la lista de evitables
                this.getRestriccionesHorariasEvitables().add(restriccionHorariaItem) ;
            }
        }
    }

    /**
     * Este es un método que se llama cuando se configura el generador de horarios antes de lanzarlo
     */
    public void tratarEvitarClaseUltimaHora()
    {
        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;

        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_SEXTA_HORA)
            {
                // La eliminamos de la lista de no evitables
                iterator.remove() ;

                // Añadimos la restricción horaria a la lista de evitables
                this.getRestriccionesHorariasEvitables().add(restriccionHorariaItem) ;
            }
        }
    }

    /**
     * Este método se debe llamar cuando se configura el generador de horarios antes de lanzarlo
     * 
     * @param preferenciasHorariasProfesores Preferencias horarias de los profesores
     */
    public void tratarEvitarClaseTramoHorario(List<PreferenciasHorariasProfesor> preferenciasHorariasProfesores)
    {
        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;

        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            // Si se ha encontrado, se establece que se desea evitar
            if (this.buscarPreferenciaHorariaProfesor(restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario(), preferenciasHorariasProfesores))
            {
                // La eliminamos de la lista de no evitables
                iterator.remove() ;

                // Añadimos la restricción horaria a la lista de evitables
                this.getRestriccionesHorariasEvitables().add(restriccionHorariaItem) ;
            }
        }
    }

    /**
     * Busca la preferencia horaria del profesor
     * 
     * @param indiceCursoDia Índice del curso y día
     * @param tramoHorario Tramo horario
     * @param preferenciasHorariasProfesores Preferencias horarias de los profesores
     * @return true si se desea evitar, false en caso contrario
     */ 
    private boolean buscarPreferenciaHorariaProfesor(int indiceCursoDia, int tramoHorario, List<PreferenciasHorariasProfesor> preferenciasHorariasProfesores)
    {
        boolean encontrado = false ;
        
        // Obtenemos el día de la semana
        int diaDeLaSemana = indiceCursoDia % Constants.NUMERO_DIAS_SEMANA ;

        // Buscamos la preferencia horaria del profesor
        Iterator<PreferenciasHorariasProfesor> iterator = preferenciasHorariasProfesores.iterator() ;
        while (iterator.hasNext() && !encontrado)
        {
            // Obtenemos la preferencia horaria del profesor
            PreferenciasHorariasProfesor preferenciaHorariaProfesor = iterator.next() ;

            // Si la preferencia horaria del profesor coincide con el día y tramo horario, se establece que se desea evitar
            encontrado = preferenciaHorariaProfesor.getDiaTramoTipoHorario().getDia() == diaDeLaSemana && 
                            preferenciaHorariaProfesor.getDiaTramoTipoHorario().getTramo() == tramoHorario ;
        }

        return encontrado ;
    }

    /**
     * Este método se debe llamar cuando se configura el generador de horarios antes de lanzarlo
     */
    public void sinClasePrimeraHora()
    {
        // Primero eliminamos de las no evitables
        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_PRIMERA_HORA)
            {
                // La eliminamos de la lista de no evitables
                iterator.remove() ;
            }
        }

        // Ahora eliminamos de las evitables
        iterator = this.getRestriccionesHorariasEvitables().iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_PRIMERA_HORA)
            {
                // La eliminamos de la lista de evitables
                iterator.remove() ;
            }
        }
    }

    /**
     * Este es un método que se llama cuando se configura el generador de horarios antes de lanzarlo
     */
    public void sinClaseUltimaHora()
    {
        // Primero eliminamos de las no evitables
        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_SEXTA_HORA)
            {
                // La eliminamos de la lista de no evitables
                iterator.remove() ;
            }
        }

        // Ahora eliminamos de las evitables
        iterator = this.getRestriccionesHorariasEvitables().iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_SEXTA_HORA)
            {
                // La eliminamos de la lista de evitables
                iterator.remove() ;
            }
        }
    }
}
