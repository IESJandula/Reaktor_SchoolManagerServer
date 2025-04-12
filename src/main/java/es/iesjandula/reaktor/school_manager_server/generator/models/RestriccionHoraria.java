package es.iesjandula.reaktor.school_manager_server.generator.models;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestriccionHoraria
{
	/** Índice Día curso inicial */
	private final int indiceCursoDiaInicial ;
	
	/** Índice Día curso final */
	private final int indiceCursoDiaFinal ;
	
	/** Índice Tramo horario inicial */
	private int indiceTramoHorarioInicial ;
	
	/** Índice Tramo horario final */
	private int indiceTramoHorarioFinal ;

    private RestriccionHoraria(Builder builder)
    {
        this.indiceCursoDiaInicial     = builder.indiceCursoDiaInicial ;
        this.indiceCursoDiaFinal       = builder.indiceCursoDiaFinal ;
        this.indiceTramoHorarioInicial = builder.indiceTramoHorarioInicial ;
        this.indiceTramoHorarioFinal   = builder.indiceTramoHorarioFinal ;
    }

    /**
     * @return the indiceCursoDiaInicial
     */
    public int getIndiceCursoDiaInicial() 
    {
        return this.indiceCursoDiaInicial ;
    }

    /**
     * @return the indiceCursoDiaFinal
     */
    public int getIndiceCursoDiaFinal() 
    {
        return this.indiceCursoDiaFinal ;
    }

    /**
     * @return the indiceTramoHorarioInicial
     */
    public int getIndiceTramoHorarioInicial() 
    {
        return this.indiceTramoHorarioInicial ;
    }

    /**
     * @return the indiceTramoHorarioFinal
     */
    public int getIndiceTramoHorarioFinal() 
    {
        return this.indiceTramoHorarioFinal ;
    }

    /**
     * Builder class para {@link RestriccionHoraria}
     */
    public static class Builder
    {
        /** Índice Día curso inicial */
        private int indiceCursoDiaInicial ;

        /** Índice Día curso final */
        private int indiceCursoDiaFinal ;

        /** Índice Tramo horario inicial */
        private int indiceTramoHorarioInicial ;

        /** Índice Tramo horario final */
        private int indiceTramoHorarioFinal ;

        /**
         * @param indiceCursoDiaInicial Índice Día curso inicial
         */
        public Builder(int indiceCursoDiaInicial)
        {
            this.indiceCursoDiaInicial     = indiceCursoDiaInicial ;
            this.indiceCursoDiaFinal       = indiceCursoDiaInicial + Constants.NUMERO_DIAS_SEMANA ;

            this.indiceTramoHorarioInicial = 0 ;
            this.indiceTramoHorarioFinal   = Constants.NUMERO_TRAMOS_HORARIOS ;
        }

        /**
         * @param indiceTramoHorarioInicial Índice Tramo horario inicial
         * @return this
         */
        public Builder docenteEntraDespuesSegundaHora()
        {
            this.indiceTramoHorarioInicial = Constants.TRAMO_HORARIO_SEGUNDA_HORA ;
            return this ;
        }

        /**
         * @param indiceTramoHorarioInicial Índice Tramo horario inicial
         * @return this
         */
        public Builder docenteSaleAntesQuintaHora()
        {
            this.indiceTramoHorarioFinal = Constants.TRAMO_HORARIO_QUINTA_HORA ;
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
            // Asignamos justo la curso-dia necesario para asignarlo
            this.indiceCursoDiaInicial     = indiceCursoDia ;
            this.indiceCursoDiaFinal       = indiceCursoDia + 1 ;

            // Asignamos justo la hora necesaria para asignarlo
            this.indiceTramoHorarioInicial = indiceTramoHorario ;
            this.indiceTramoHorarioFinal   = indiceTramoHorario + 1 ;

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
            // Asignamos justo la curso-dia necesario para asignarlo
            this.indiceCursoDiaInicial     = indiceCursoDia ;
            this.indiceCursoDiaFinal       = indiceCursoDia + 1 ;

            // Asignamos justo la hora necesaria para asignarlo
            this.indiceTramoHorarioInicial = indiceTramoHorario ;
            this.indiceTramoHorarioFinal   = indiceTramoHorario + 1 ;

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
            this.indiceCursoDiaInicial = this.indiceCursoDiaInicial + diaDeLaSemana ;
            this.indiceCursoDiaFinal   = this.indiceCursoDiaInicial + 1 ;

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
            this.indiceCursoDiaInicial     = this.indiceCursoDiaInicial + diaDeLaSemana ;
            this.indiceCursoDiaFinal       = this.indiceCursoDiaInicial + 1 ;

            this.indiceTramoHorarioInicial = numerotramo ;
            this.indiceTramoHorarioFinal   = numerotramo + 1 ;

            return this ;
        }

        /**
         * Incrementamos un día ya que el anterior no es válido
         * 
         * @return this
         */
        public Builder incrementarUnDia()
        {
            // Incrementamos
            this.indiceCursoDiaInicial ++ ;

            return this ;
        }

        /**
         * @return the indiceCursoDiaInicial
         */
        public int getIndiceCursoDiaInicial() 
        {
            return this.indiceCursoDiaInicial ;
        }

        /**
         * @return the indiceCursoDiaFinal
         */
        public int getIndiceCursoDiaFinal() 
        {
            return this.indiceCursoDiaFinal ;
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
	 * Método que amplia el intervalo en caso de que no se encuentre hueco para una sesión
	 * 
	 * @param sesion sesión a asignar
	 * @throws SchoolManagerServerException con un error
	 */
	public void ampliarIntervalo(Sesion sesion) throws SchoolManagerServerException
	{
		if (this.indiceTramoHorarioInicial <= 0 || this.indiceTramoHorarioFinal >= (Constants.NUMERO_TRAMOS_HORARIOS - 1))
		{
			// Logueamos y lanzamos una excepción para cortar esta generación de horario
			// ya que no hay más intervalo para asignar la sesión
			
			String errorString = "\n Este horario es incorrecto ya que no se pueden ampliar más los intervalos de tramo " +
								 "horario para asignar esta sesión: \n" + 
								 sesion ;
			
			log.error(errorString) ;
			throw new SchoolManagerServerException(Constants.ERR_CODE_HORARIO_NO_MAS_AMPLIABLE, errorString) ;			
		}
		
		// Como tenemos que ampliar por el índice inicial y el final, lo haremos de forma equitativa
		
		int cercaniaAlLimiteIndiceInicial = this.indiceTramoHorarioInicial ;
		int cercaniaAlLimiteIndiceFinal   = (Constants.NUMERO_TRAMOS_HORARIOS - 1) - this.indiceTramoHorarioFinal ;
		
		// Si la cercania al límite del índice inicial es mayor, lo quito del inicial
		if (cercaniaAlLimiteIndiceInicial > cercaniaAlLimiteIndiceFinal)
		{
			this.indiceTramoHorarioInicial -- ;
		}
		else
		{
			// Sino, lo quito del final
			this.indiceTramoHorarioFinal ++ ;
		}
	}
}
