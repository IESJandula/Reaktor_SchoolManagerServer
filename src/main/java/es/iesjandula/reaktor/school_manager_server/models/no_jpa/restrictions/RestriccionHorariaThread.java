package es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RestriccionHorariaThread extends RestriccionHorariaBase
{
    /**
     * @param restriccionHorariaInit Restricción horaria inicial
     */
    public RestriccionHorariaThread(RestriccionHorariaInit restriccionHorariaInit)
    {
        super(new ArrayList<RestriccionHorariaItem>(restriccionHorariaInit.getRestriccionesHorariasNoEvitables()), new ArrayList<RestriccionHorariaItem>(restriccionHorariaInit.getRestriccionesHorariasEvitables())) ;
        
    }

    /**
     * Establece la restricción para hacer coincidir con una optativa existente del mismo bloque
     * 
     * @param indiceCursoDia índice del curso y día donde está la otra optativa
     * @param indiceTramoHorario índice del tramo horario donde está la otra optativa
     */
    public void hacerCoincidirConOptativaDelBloque(int indiceCursoDia, int indiceTramoHorario)
    {
        // Hacemos coincidir el tramo horario con el día
        this.hacerCoincidirEnDiaHora(indiceCursoDia, indiceTramoHorario) ;
    }

    /**
     * Establece la restricción para hacer coincidir con un módulo existente del antes o después de la que ya hay asignada para evitar sesiones aisladas
     * 
     * @param indiceCursoDia índice del curso y día donde está la otra optativa
     * @param indiceTramoHorario índice del tramo horario donde está la otra optativa
     */
    public void hacerCoincidirConModuloFp(int indiceCursoDia, int indiceTramoHorario)
    {
        // Hacemos coincidir el tramo horario con el día
        this.hacerCoincidirEnDiaHora(indiceCursoDia, indiceTramoHorario) ;
    }

    /**
     * Establece la restricción para asignar un día concreto
     * 
     * @param diaDeLaSemana día de la semana
     */
    public void asignarUnDiaConcreto(int diaDeLaSemana)
    {
        // Primero eliminamos de las no evitables
        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getIndiceDia() != diaDeLaSemana)
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

            if (restriccionHorariaItem.getIndiceDia() != diaDeLaSemana)
            {
                // La eliminamos de la lista de evitables
                iterator.remove() ;
            }
        }
    }

    /** 
     * Elimina un item de la restricción horaria
     * 
     * @param restriccionHorariaItem item a eliminar
     */
    public void eliminarRestriccionHorariaItem(RestriccionHorariaItem restriccionHorariaItem)
    {
        // Si el item está en las no evitables, lo eliminamos de las no evitables
        if (this.getRestriccionesHorariasNoEvitables().contains(restriccionHorariaItem))
        {
            this.getRestriccionesHorariasNoEvitables().remove(restriccionHorariaItem) ;
        }
        // Si el item está en las evitables, lo eliminamos de las evitables
        else if (this.getRestriccionesHorariasEvitables().contains(restriccionHorariaItem))
        {
            this.getRestriccionesHorariasEvitables().remove(restriccionHorariaItem) ;
        }
    }

    /**
     * Elimina un día concreto de la restricción horaria de las no evitables y las evitables
     * 
     * @param indiceCursoDia índice del curso y día
     */
    public void eliminarDiaConcreto(int indiceCursoDia)
    {
        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getIndiceDia() == indiceCursoDia)
            {
                // La eliminamos de la lista de no evitables
                iterator.remove() ;
            }
        }

        iterator = this.getRestriccionesHorariasEvitables().iterator() ;
        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getIndiceDia() == indiceCursoDia)
            {
                // La eliminamos de la lista de evitables
                iterator.remove() ;
            }
        }
    }

    /**
     * Busca una restricción horaria por día y tramo
     * 
     * @param indiceDia índice del día
     * @param indiceTramoHorario índice del tramo horario
     * @return la restricción horaria encontrada
     */
    public RestriccionHorariaItem buscarRestriccionHorariaPorDiaTramo(int indiceDia, int indiceTramoHorario)
    {
        RestriccionHorariaItem restriccionHorariaItem = null ;

        Iterator<RestriccionHorariaItem> iterator = this.getRestriccionesHorariasNoEvitables().iterator() ;
        while (iterator.hasNext() && restriccionHorariaItem == null)
        {
            RestriccionHorariaItem restriccionHorariaItemNoEvitable = iterator.next() ;

            if (restriccionHorariaItemNoEvitable.getIndiceDia() == indiceDia && restriccionHorariaItemNoEvitable.getTramoHorario() == indiceTramoHorario)
            {
                restriccionHorariaItem = restriccionHorariaItemNoEvitable ;
            }
        }

        iterator = this.getRestriccionesHorariasEvitables().iterator() ;
        while (iterator.hasNext() && restriccionHorariaItem == null)
        {
            RestriccionHorariaItem restriccionHorariaItemEvitable = iterator.next() ;

            if (restriccionHorariaItemEvitable.getIndiceDia() == indiceDia && restriccionHorariaItemEvitable.getTramoHorario() == indiceTramoHorario)
            {
                restriccionHorariaItem = restriccionHorariaItemEvitable ;
            }
        }

        return restriccionHorariaItem ;
    }

    /**
     * Método que comprueba si existe hueco para una sesión
     * 
     * @param sesionBase sesión base
     * @return true si existe hueco, false en caso contrario
     * @throws SchoolManagerServerException si no hay más intervalo para asignar la sesión  
     */
    public RestriccionHorariaItem obtenerRestriccionHorariaItem(SesionBase sesionBase) throws SchoolManagerServerException
    {
        if (this.getRestriccionesHorariasNoEvitables().isEmpty() && this.getRestriccionesHorariasEvitables().isEmpty())
        {
			// Logueamos y lanzamos una excepción para cortar esta generación de horario
			// ya que no hay más items de la semana para asignar la sesión
			
			String debugString = "\n Este horario es incorrecto ya que no se pueden obtener más items de la semana para asignar esta sesión: \n" +  sesionBase ;
			
			log.debug(debugString) ;
			throw new SchoolManagerServerException(Constants.ERR_CODE_HORARIO_NO_MAS_AMPLIABLE, debugString) ;		
        }

        RestriccionHorariaItem restriccionHorariaItem = null ;

        // Primero vemos si podemos obtener una restricción horaria no evitable
        if (!this.getRestriccionesHorariasNoEvitables().isEmpty())
        {
            // Mezclamos la lista para que tenga cierta aleatoriedad
            Collections.shuffle(this.getRestriccionesHorariasNoEvitables()) ;

            // Obtenemos el primer item de la lista
            restriccionHorariaItem = this.getRestriccionesHorariasNoEvitables().remove(0) ;
        }
        else
        {
            // Mezclamos la lista para que tenga cierta aleatoriedad
            Collections.shuffle(this.getRestriccionesHorariasEvitables()) ;

            // Obtenemos el primer item de la lista
            restriccionHorariaItem = this.getRestriccionesHorariasEvitables().remove(0) ;
        }

        // Eliminamos aleatoriamente un item de la restricción horaria
        return restriccionHorariaItem ;
    }
}
