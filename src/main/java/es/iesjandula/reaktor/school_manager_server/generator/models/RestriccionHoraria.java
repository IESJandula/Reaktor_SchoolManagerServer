package es.iesjandula.reaktor.school_manager_server.generator.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestriccionHoraria
{
	/** Restricciones horarias */
	private final List<RestriccionHorariaItem> restriccionesHorarias ;

    private RestriccionHoraria(Builder builder)
    {
        this.restriccionesHorarias = builder.restriccionesHorarias ;
    }

    /**
     * @return the restriccionesHorarias
     */
    public List<RestriccionHorariaItem> getRestriccionesHorarias() 
    {
        return this.restriccionesHorarias ;
    }

    /**
     * Builder class para {@link RestriccionHoraria}
     */
    public static class Builder
    {
        /** Restricciones horarias */
        private List<RestriccionHorariaItem> restriccionesHorarias ;

        /**
         * @param indiceCursoDiaInicial Índice Día curso inicial
         */
        public Builder(int indiceCursoDiaInicial)
        {
            // Por defecto, damos de alta todas las posibilidades
            this.restriccionesHorarias = new ArrayList<>() ;

            for (int i = indiceCursoDiaInicial ; i < (indiceCursoDiaInicial + Constants.NUMERO_DIAS_SEMANA) ; i++)
            {
                for (int j = Constants.TRAMO_HORARIO_PRIMERA_HORA ; j < Constants.NUMERO_TRAMOS_HORARIOS ; j++)
                {
                    this.restriccionesHorarias.add(new RestriccionHorariaItem(i, j)) ;
                }
            }
        }

        /**
         * Eliminamos la primera hora (tramo 0) de todos los items de la lista
         * 
         * @return this
         */
        public Builder sinClasePrimeraHora()
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorarias.iterator() ;

            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_PRIMERA_HORA)
                {
                    iterator.remove() ;
                }
            }

            return this ;
        }

        /**
         * Eliminamos la última hora (tramo 5) de todos los items de la lista
         * 
         * @return this
         */
        public Builder conClasePrimerHora()
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorarias.iterator() ;

            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_SEXTA_HORA)
                {
                    iterator.remove() ;
                }
            }

            return this ;
        }

        /**
         * Establece la restricción para hacer coincidir con una optativa existente del mismo bloque
         * 
         * @param indiceCursoDia índice del curso y día donde está la otra optativa
         * @param indiceTramoHorario índice del tramo horario donde está la otra optativa
         * @return this
         */
        public Builder hacerCoincidirConOptativaDelBloque(int indiceCursoDia, int indiceTramoHorario)
        {
            // Hacemos coincidir el tramo horario con el día
            this.hacerCoincidirEnDiaHora(indiceCursoDia, indiceTramoHorario) ;

            return this ;
        }

        /**
         * Establece la restricción para hacer coincidir con un módulo existente del antes o después de la que ya hay asignada para evitar sesiones aisladas
         * 
         * @param indiceCursoDia índice del curso y día donde está la otra optativa
         * @param indiceTramoHorario índice del tramo horario donde está la otra optativa
         * @return this
         */
        public Builder hacerCoincidirConModuloFp(int indiceCursoDia, int indiceTramoHorario)
        {
            // Hacemos coincidir el tramo horario con el día
            this.hacerCoincidirEnDiaHora(indiceCursoDia, indiceTramoHorario) ;

            return this ;
        }

        /**
         * Establece la restricción para asignar un día concreto
         * 
         * @param diaDeLaSemana día de la semana
         * @return this
         */
        public Builder asignarUnDiaConcreto(int diaDeLaSemana)
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorarias.iterator() ;

            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getIndiceDia() != diaDeLaSemana)
                {
                    iterator.remove() ;
                }
            }

            return this ;
        }

        /**
         * Establece la restricción para asignar un día y tramo horario     concreto
         * 
         * @param diaDeLaSemana día de la semana
         * @param numeroTramoHorario número del tramo horario
         * @return this
         */
        public Builder asignarUnDiaTramoConcreto(int diaDeLaSemana, int numerotramo)
        {
            this.hacerCoincidirEnDiaHora(diaDeLaSemana, numerotramo) ;

            return this ;
        }

        /**
         * Hace coincidir el tramo horario con el día
         * 
         * @param indiceCursoDia índice del curso y día donde está la otra optativa
         * @param indiceTramoHorario índice del tramo horario donde está la otra optativa
         */
        private void hacerCoincidirEnDiaHora(int indiceCursoDia, int indiceTramoHorario)
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorarias.iterator() ;

            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getIndiceDia() != indiceCursoDia || restriccionHorariaItem.getTramoHorario() != indiceTramoHorario)
                {
                    iterator.remove() ;
                }
            }
        }

        /** 
         * Elimina un item de la restricción horaria
         * 
         * @param restriccionHorariaItem item a eliminar
         * @return this
         */
        public Builder eliminarRestriccionHorariaItem(RestriccionHorariaItem restriccionHorariaItem)
        {
            this.restriccionesHorarias.remove(restriccionHorariaItem) ;

            return this ;
        }

        /**
         * @return the restriccionesHorarias
         */
        public List<RestriccionHorariaItem> getRestriccionesHorarias()
        {
            return this.restriccionesHorarias ;
        }

        /** Builder para {@link RestriccionHoraria}
         * @return una nueva instancia de {@link RestriccionHoraria} 
         */
        public RestriccionHoraria build()
        {
            return new RestriccionHoraria(this) ;
        }
    }

    /**
     * Método que comprueba si existe hueco para una sesión
     * 
     * @return true si existe hueco, false en caso contrario
     * @throws SchoolManagerServerException si no hay más intervalo para asignar la sesión  
     */
    public RestriccionHorariaItem obtenerRestriccionHorariaItem(Sesion sesion) throws SchoolManagerServerException
    {
        if (this.restriccionesHorarias.isEmpty())
        {
			// Logueamos y lanzamos una excepción para cortar esta generación de horario
			// ya que no hay más items de la semana para asignar la sesión
			
			String errorString = "\n Este horario es incorrecto ya que no se pueden obtener más items de la semana para asignar esta sesión: \n" +  sesion ;
			
			log.error(errorString) ;
			throw new SchoolManagerServerException(Constants.ERR_CODE_HORARIO_NO_MAS_AMPLIABLE, errorString) ;		
        }

        // Mezclamos la lista para que tenga cierta aleatoriedad
        Collections.shuffle(this.restriccionesHorarias) ;

        // Eliminamos aleatoriamente un item de la restricción horaria
        return this.restriccionesHorarias.remove(0) ;
    }


    /**
     * Método que elimina un día concreto de la restricción horaria
     * 
     * @param dia día a eliminar
     */
    public void eliminarDiaConcreto(int dia)
    {
        Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorarias.iterator() ;

        while (iterator.hasNext())
        {
            RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

            if (restriccionHorariaItem.getIndiceDia() == dia)
            {
                iterator.remove() ;
            }
        }
    }
}
