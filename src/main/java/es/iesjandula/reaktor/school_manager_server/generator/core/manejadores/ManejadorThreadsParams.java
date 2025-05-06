package es.iesjandula.reaktor.school_manager_server.generator.core.manejadores;

import java.util.Map;

import es.iesjandula.reaktor.school_manager_server.services.AsignaturaService;

public class ManejadorThreadsParams
{
	/** Número de cursos matutinos */
	private int numeroCursosMatutinos ;
	
	/** Número de cursos vespertinos */
	private int numeroCursosVespertinos ;
	
    /** Mapa que correlaciona los nombres de los cursos matutinos con el índice que ocupan en la matriz de sesiones */
    private Map<String, Integer> mapCorrelacionadorCursosMatutinos ;

    /** Mapa que correlaciona los nombres de los cursos vespertinos con el índice que ocupan en la matriz de sesiones */
    private Map<String, Integer> mapCorrelacionadorCursosVespertinos ;

    /** Tamaño del pool */
    private int poolSize ;

    /** Número de threads por iteración */
    private int numeroThreadPorIteracion ;
    
    /** Manejador de resultados */
    private ManejadorResultados manejadorResultados ;
    
    /** Factor de puntuación en función del número de sesiones insertadas */
    private int factorNumeroSesionesInsertadas ;

    /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor */
    private int factorSesionesConsecutivasProfesor ;

    /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina */
    private int factorSesionesConsecutivasProfesorMatVes ;

    /** Asignatura service */
    private AsignaturaService asignaturaService ;
    
    /**
     * Constructor privado para forzar el uso del Builder
     * 
     * @param builder builder interno
     */
    private ManejadorThreadsParams(Builder builder)
    {
    	this.numeroCursosMatutinos				      = builder.numeroCursosMatutinos ;
        this.numeroCursosVespertinos			      = builder.numeroCursosVespertinos ;
        this.mapCorrelacionadorCursosMatutinos 	      = builder.mapCorrelacionadorCursosMatutinos ;
        this.mapCorrelacionadorCursosVespertinos      = builder.mapCorrelacionadorCursosVespertinos ;
        this.poolSize                 			      = builder.poolSize ;
        this.numeroThreadPorIteracion 			      = builder.numeroThreadPorIteracion ;
        this.manejadorResultados	  			      = builder.manejadorResultados ;
        this.factorNumeroSesionesInsertadas 	      = builder.factorNumeroSesionesInsertadas ;
        this.factorSesionesConsecutivasProfesor       = builder.factorSesionesConsecutivasProfesor ;
        this.factorSesionesConsecutivasProfesorMatVes = builder.factorSesionesConsecutivasProfesorMatVes ;
        this.asignaturaService                        = builder.asignaturaService ;
    }
    
    /**
     * @return número de cursos matutinos
     */
    public int getNumeroCursosMatutinos()
    {
        return this.numeroCursosMatutinos ;
    }

    /**
     * @return número de cursos vespertinos
     */
    public int getNumeroCursosVespertinos()
    {
        return this.numeroCursosVespertinos ;
    }

    /**
     * @return mapa que correlaciona los nombres de los cursos matutinos con el índice que ocupan en la matriz de sesiones
     */
    public Map<String, Integer> getMapCorrelacionadorCursosMatutinos()
    {
        return this.mapCorrelacionadorCursosMatutinos ;
    }

    /**
     * @return mapa que correlaciona los nombres de los cursos vespertinos con el índice que ocupan en la matriz de sesiones
     */
    public Map<String, Integer> getMapCorrelacionadorCursosVespertinos()
    {
        return this.mapCorrelacionadorCursosVespertinos ;
    }

    /**
     * @return tamaño del pool
     */
    public int getPoolSize()
    {
        return this.poolSize ;
    }
    
    /**
     * @return número de threads por iteración
     */
    public int getNumeroThreadPorIteracion()
    {
        return this.numeroThreadPorIteracion ;
    }
    
    /**
     * @return manejador de resultados
     */    
    public ManejadorResultados getManejadorResultados()
    {
    	return this.manejadorResultados ;
    }
    
    /**
	 * @return the factorNumeroSesionesInsertadas
	 */
	public int getFactorNumeroSesionesInsertadas()
	{
		return this.factorNumeroSesionesInsertadas ;
	}

	/**
	 * @return the factorSesionesConsecutivasProfesor
	 */
	public int getFactorSesionesConsecutivasProfesor()
	{
		return this.factorSesionesConsecutivasProfesor ;
	}

    /**
     * @return the factorSesionesConsecutivasProfesorMatVes
     */
    public int getFactorSesionesConsecutivasProfesorMatVes()
    {
        return this.factorSesionesConsecutivasProfesorMatVes ;
    }

    /**
     * @return asignatura service
     */
    public AsignaturaService getAsignaturaService()
    {
        return this.asignaturaService ;
    }

	/**
     * Clase estática interna Builder
     */
    public static class Builder
    {
    	/** Número de cursos matutinos */
    	private int numeroCursosMatutinos ;

    	/** Número de cursos vespertinos */
    	private int numeroCursosVespertinos ;
    	
        /** Mapa que correlaciona los nombres de los cursos matutinos con el índice que ocupan en la matriz de sesiones */
        private Map<String, Integer> mapCorrelacionadorCursosMatutinos ;

        /** Mapa que correlaciona los nombres de los cursos vespertinos con el índice que ocupan en la matriz de sesiones */
        private Map<String, Integer> mapCorrelacionadorCursosVespertinos ;

        /** Tamaño del pool */
        private int poolSize ;
        
        /** Número de threads por iteración */
        private int numeroThreadPorIteracion ;
        
        /** Manejador de resultados */
        private ManejadorResultados manejadorResultados ;
        
        /** Factor de puntuación en función del número de sesiones insertadas */
        private int factorNumeroSesionesInsertadas ;

        /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor */
        private int factorSesionesConsecutivasProfesor ;

        /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina */
        private int factorSesionesConsecutivasProfesorMatVes ;

        /** Asignatura service */
        private AsignaturaService asignaturaService ;

        /**
         * @param numeroCursosMatutinos numero de cursos matutinos
         * @return builder
         */
        public Builder setNumeroCursosMatutinos(int numeroCursosMatutinos)
        {
            this.numeroCursosMatutinos = numeroCursosMatutinos ;
            
            return this ;
        }

        /**
         * @param numeroCursosVespertinos numero de cursos vespertinos
         * @return builder
         */
        public Builder setNumeroCursosVespertinos(int numeroCursosVespertinos)
        {
            this.numeroCursosVespertinos = numeroCursosVespertinos ;
            return this ;
        }
        
        /**
         * @param mapa que correlaciona los nombres de los cursos matutinos con el índice que ocupan en la matriz de sesiones
         * @return builder
         */
        public Builder setMapCorrelacionadorCursosMatutinos(Map<String, Integer> mapCorrelacionadorCursosMatutinos)
        {
            this.mapCorrelacionadorCursosMatutinos = mapCorrelacionadorCursosMatutinos ;
            return this ;
        }

        /**
         * @param mapa que correlaciona los nombres de los cursos vespertinos con el índice que ocupan en la matriz de sesiones
         * @return builder
         */
        public Builder setMapCorrelacionadorCursosVespertinos(Map<String, Integer> mapCorrelacionadorCursosVespertinos)
        {
            this.mapCorrelacionadorCursosVespertinos = mapCorrelacionadorCursosVespertinos ;
            return this ;
        }

        /**
         * @param tamaño del pool
         * @return builder
         */
        public Builder setPoolSize(int poolSize)
        {
            this.poolSize = poolSize ;
            return this ;
        }
        
        /**
         * @param número de threads por iteración
         * @return builder
         */
        public Builder setNumeroThreadPorIteracion(int numeroThreadPorIteracion)
        {
            this.numeroThreadPorIteracion = numeroThreadPorIteracion ;
            return this ;
        }
        
        /**
         * @param manejador de resultados
         * @return builder
         */
        public Builder setManejadorResultados(ManejadorResultados manejadorResultados)
        {
            this.manejadorResultados = manejadorResultados ;
            return this ;
        }
        
        /**
         * @param factor número sesiones insertadas
         * @return builder
         */
        public Builder setFactorNumeroSesionesInsertadas(int factorNumeroSesionesInsertadas)
        {
            this.factorNumeroSesionesInsertadas = factorNumeroSesionesInsertadas ;
            return this ;
        }
        
        /**
         * @param factor sesiones consecutivas profesor
         * @return builder
         */
        public Builder setFactorSesionesConsecutivasProfesor(int factorSesionesConsecutivasProfesor)
        {
            this.factorSesionesConsecutivasProfesor = factorSesionesConsecutivasProfesor ;
            return this ;
        }

        /**
         * @param factor sesiones consecutivas profesor en la primera hora vespertina
         * @return builder
         */
        public Builder setFactorSesionesConsecutivasProfesorMatVes(int factorSesionesConsecutivasProfesorMatVes)
        {
            this.factorSesionesConsecutivasProfesorMatVes = factorSesionesConsecutivasProfesorMatVes ;
            return this ;
        }

        /** 
         * @param asignaturaService asignatura service
         * @return builder
         */
        public Builder setAsignaturaService(AsignaturaService asignaturaService)
        {
            this.asignaturaService = asignaturaService ;
            
            return this ;
        }

        /**
         * Método build que construye el objeto final
         * @return una instancia de ManejadorThreadsParams
         */
        public ManejadorThreadsParams build()
        {
            return new ManejadorThreadsParams(this) ;
        }
    }
}
