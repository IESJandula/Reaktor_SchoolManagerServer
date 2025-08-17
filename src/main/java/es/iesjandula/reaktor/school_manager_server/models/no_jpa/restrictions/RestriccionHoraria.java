package es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.PreferenciasHorariasProfesor;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestriccionHoraria
{
	/** Restricciones horarias no evitables */
	private final List<RestriccionHorariaItem> restriccionesHorariasNoEvitables ;

	/** Restricciones horarias evitables */
	private final List<RestriccionHorariaItem> restriccionesHorariasEvitables ;

    private RestriccionHoraria(Builder builder)
    {
        this.restriccionesHorariasNoEvitables = builder.restriccionesHorariasNoEvitables ;
        this.restriccionesHorariasEvitables   = builder.restriccionesHorariasEvitables ;
    }

    /**
     * Builder class para {@link RestriccionHoraria}
     */
    public static class Builder
    {
        /** Restricciones horarias no evitables */
        private List<RestriccionHorariaItem> restriccionesHorariasNoEvitables ;

        /** Restricciones horarias evitables */
        private List<RestriccionHorariaItem> restriccionesHorariasEvitables ;

        /**
         * @param indiceCursoDiaInicial Índice Día curso inicial
         */
        public Builder(int indiceCursoDiaInicial)
        {
            this.restriccionesHorariasNoEvitables = new ArrayList<RestriccionHorariaItem>() ;
            this.restriccionesHorariasEvitables   = new ArrayList<RestriccionHorariaItem>() ;

            // Por defecto, damos de alta todas las posibilidades en las no evitables
            for (int i = indiceCursoDiaInicial ; i < (indiceCursoDiaInicial + Constants.NUMERO_DIAS_SEMANA) ; i++)
            {
                for (int j = Constants.TRAMO_HORARIO_PRIMERA_HORA ; j < Constants.NUMERO_TRAMOS_HORARIOS ; j++)
                {
                    this.restriccionesHorariasNoEvitables.add(new RestriccionHorariaItem(i, j)) ;
                }
            }
        }

        /**
         * Este método se debe llamar cuando se quiera tratar de eliminar la primera hora de la restricción horaria
         * 
         * @return this
         */
        public Builder tratarEvitarClasePrimeraHora()
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;

            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_PRIMERA_HORA)
                {
                    // La eliminamos de la lista de no evitables
                    iterator.remove() ;

                    // Añadimos la restricción horaria a la lista de evitables
                    this.restriccionesHorariasEvitables.add(restriccionHorariaItem) ;
                }
            }

            return this ;
        }

        /**
         * Este método se debe llamar cuando se quiera tratar de eliminar la última hora de la restricción horaria
         * 
         * @return this
         */
        public Builder tratarEvitarClaseUltimaHora()
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;

            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_SEXTA_HORA)
                {
                    // La eliminamos de la lista de no evitables
                    iterator.remove() ;

                    // Añadimos la restricción horaria a la lista de evitables
                    this.restriccionesHorariasEvitables.add(restriccionHorariaItem) ;
                }
            }

            return this ;
        }

        /**
         * Este método se debe llamar cuando se quiera tratar de eliminar un tramo horario concreto de la restricción horaria
         * 
         * @param preferenciasHorariasProfesores Preferencias horarias de los profesores
         * @return this
         */
        public Builder tratarEvitarClaseTramoHorario(List<PreferenciasHorariasProfesor> preferenciasHorariasProfesores)
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;

            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                // Si se ha encontrado, se establece que se desea evitar
                if (this.buscarPreferenciaHorariaProfesor(restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario(), preferenciasHorariasProfesores))
                {
                    // La eliminamos de la lista de no evitables
                    iterator.remove() ;

                    // Añadimos la restricción horaria a la lista de evitables
                    this.restriccionesHorariasEvitables.add(restriccionHorariaItem) ;
                }
            }

            return this ;
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
         * Eliminamos la primera hora (tramo 0) de todos los items de la lista
         * 
         * @return this
         */
        public Builder sinClasePrimeraHora()
        {
            // Primero eliminamos de las no evitables
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;
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
            iterator = this.restriccionesHorariasEvitables.iterator() ;
            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_PRIMERA_HORA)
                {
                    // La eliminamos de la lista de evitables
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
        public Builder sinClaseUltimaHora()
        {
            // Primero eliminamos de las no evitables
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;
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
            iterator = this.restriccionesHorariasEvitables.iterator() ;
            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getTramoHorario() == Constants.TRAMO_HORARIO_SEXTA_HORA)
                {
                    // La eliminamos de la lista de evitables
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
            // Primero eliminamos de las no evitables
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;
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
            iterator = this.restriccionesHorariasEvitables.iterator() ;
            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getIndiceDia() != diaDeLaSemana)
                {
                    // La eliminamos de la lista de evitables
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

        /** 
         * Elimina un item de la restricción horaria
         * 
         * @param restriccionHorariaItem item a eliminar
         * @return this
         */
        public Builder eliminarRestriccionHorariaItem(RestriccionHorariaItem restriccionHorariaItem)
        {
            // Si el item está en las no evitables, lo eliminamos de las no evitables
            if (this.restriccionesHorariasNoEvitables.contains(restriccionHorariaItem))
            {
                this.restriccionesHorariasNoEvitables.remove(restriccionHorariaItem) ;
            }
            // Si el item está en las evitables, lo eliminamos de las evitables
            else if (this.restriccionesHorariasEvitables.contains(restriccionHorariaItem))
            {
                this.restriccionesHorariasEvitables.remove(restriccionHorariaItem) ;
            }

            return this ;
        }

        /**
         * Elimina un día concreto de la restricción horaria de las no evitables y las evitables
         * 
         * @param indiceCursoDia índice del curso y día
         * @return this
         */
        public Builder eliminarDiaConcreto(int indiceCursoDia)
        {
            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;
            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getIndiceDia() == indiceCursoDia)
                {
                    // La eliminamos de la lista de no evitables
                    iterator.remove() ;
                }
            }

            iterator = this.restriccionesHorariasEvitables.iterator() ;
            while (iterator.hasNext())
            {
                RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

                if (restriccionHorariaItem.getIndiceDia() == indiceCursoDia)
                {
                    // La eliminamos de la lista de evitables
                    iterator.remove() ;
                }
            }

            return this ;
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

            Iterator<RestriccionHorariaItem> iterator = this.restriccionesHorariasNoEvitables.iterator() ;
            while (iterator.hasNext() && restriccionHorariaItem == null)
            {
                RestriccionHorariaItem restriccionHorariaItemNoEvitable = iterator.next() ;

                if (restriccionHorariaItemNoEvitable.getIndiceDia() == indiceDia && restriccionHorariaItemNoEvitable.getTramoHorario() == indiceTramoHorario)
                {
                    restriccionHorariaItem = restriccionHorariaItemNoEvitable ;
                }
            }

            iterator = this.restriccionesHorariasEvitables.iterator() ;
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
         * @return the restriccionesHorarias
         */
        public List<RestriccionHorariaItem> getRestriccionesHorariasNoEvitables()
        {
            return this.restriccionesHorariasNoEvitables ;
        }

        /**
         * @return the restriccionesHorarias
         */
        public List<RestriccionHorariaItem> getRestriccionesHorariasEvitables()
        {
            return this.restriccionesHorariasEvitables ;
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
     * @param sesionBase sesión base
     * @return true si existe hueco, false en caso contrario
     * @throws SchoolManagerServerException si no hay más intervalo para asignar la sesión  
     */
    public RestriccionHorariaItem obtenerRestriccionHorariaItem(SesionBase sesionBase) throws SchoolManagerServerException
    {
        if (this.restriccionesHorariasNoEvitables.isEmpty() && this.restriccionesHorariasEvitables.isEmpty())
        {
			// Logueamos y lanzamos una excepción para cortar esta generación de horario
			// ya que no hay más items de la semana para asignar la sesión
			
			String warningString = "\n Este horario es incorrecto ya que no se pueden obtener más items de la semana para asignar esta sesión: \n" +  sesionBase ;
			
			log.warn(warningString) ;
			throw new SchoolManagerServerException(Constants.ERR_CODE_HORARIO_NO_MAS_AMPLIABLE, warningString) ;		
        }

        RestriccionHorariaItem restriccionHorariaItem = null ;

        // Primero vemos si podemos obtener una restricción horaria no evitable
        if (!this.restriccionesHorariasNoEvitables.isEmpty())
        {
            // Mezclamos la lista para que tenga cierta aleatoriedad
            Collections.shuffle(this.restriccionesHorariasNoEvitables) ;

            // Obtenemos el primer item de la lista
            restriccionHorariaItem = this.restriccionesHorariasNoEvitables.remove(0) ;
        }
        else
        {
            // Mezclamos la lista para que tenga cierta aleatoriedad
            Collections.shuffle(this.restriccionesHorariasEvitables) ;

            // Obtenemos el primer item de la lista
            restriccionHorariaItem = this.restriccionesHorariasEvitables.remove(0) ;
        }

        // Eliminamos aleatoriamente un item de la restricción horaria
        return restriccionHorariaItem ;
    }
}
