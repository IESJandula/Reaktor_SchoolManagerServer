package es.iesjandula.reaktor.school_manager_server.generator.core;

import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;

public class HorarioParams
{
	/** Número de cursos matutinos */
	private int numeroCursosMatutinos ;
	
	/** Número de cursos vespertinos */
	private int numeroCursosVespertinos ;
	
    /** Factor de puntuación en función del número de sesiones insertadas */
    private int factorNumeroSesionesInsertadas ;

    /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor */
    private int factorSesionesConsecutivasProfesor ;

    /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina */
    private int factorSesionesConsecutivasProfesorMatVes ;

    /** Matriz de asignaciones matutinas */
    private Asignacion[][] matrizAsignacionesMatutinas ;

    /** Matriz de asignaciones vespertinas */
    private Asignacion[][] matrizAsignacionesVespertinas ;

    /**
     * Constructor privado para forzar el uso del Builder
     * 
     * @param builder builder interno
     */
    private HorarioParams(Builder builder)
    {
    	this.numeroCursosMatutinos				      = builder.numeroCursosMatutinos ;
    	this.numeroCursosVespertinos			      = builder.numeroCursosVespertinos ;
        this.factorNumeroSesionesInsertadas           = builder.factorNumeroSesionesInsertadas ;
        this.factorSesionesConsecutivasProfesor       = builder.factorSesionesConsecutivasProfesor ;
        this.factorSesionesConsecutivasProfesorMatVes = builder.factorSesionesConsecutivasProfesorMatVes ;
        this.matrizAsignacionesMatutinas              = builder.matrizAsignacionesMatutinas ;
        this.matrizAsignacionesVespertinas            = builder.matrizAsignacionesVespertinas ;
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
     * @return factor de puntuación en función del número de sesiones insertadas
     */
    public int getFactorNumeroSesionesInsertadas()
    {
        return this.factorNumeroSesionesInsertadas ;
    }

    /**
     * @return factor de puntuación en función del número de sesiones consecutivas que tenga un profesor
     */
    public int getFactorSesionesConsecutivasProfesor()
    {
        return this.factorSesionesConsecutivasProfesor ;
    }

    /**
     * @return factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina
     */
    public int getFactorSesionesConsecutivasProfesorMatVes()
    {
        return this.factorSesionesConsecutivasProfesorMatVes ;
    }

    /**
     * @return matriz de asignaciones matutinas
     */
    public Asignacion[][] getMatrizAsignacionesMatutinas()
    {
        return this.matrizAsignacionesMatutinas ;
    }

    /**
     * @return matriz de asignaciones vespertinas
     */
    public Asignacion[][] getMatrizAsignacionesVespertinas()
    {
        return this.matrizAsignacionesVespertinas ;
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
    	
        /** Factor de puntuación en función del número de sesiones insertadas */
        private int factorNumeroSesionesInsertadas ;

        /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor */
        private int factorSesionesConsecutivasProfesor ;

        /** Factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina */
        private int factorSesionesConsecutivasProfesorMatVes ;

        /** Matriz de asignaciones matutinas */
        private Asignacion[][] matrizAsignacionesMatutinas ;

        /** Matriz de asignaciones vespertinas */
        private Asignacion[][] matrizAsignacionesVespertinas ;
        
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
         * @param factorNumeroSesionesInsertadas factor de puntuación en función del número de sesiones insertadas
         * @return builder
         */
        public Builder setFactorNumeroSesionesInsertadas(int factorNumeroSesionesInsertadas)
        {
            this.factorNumeroSesionesInsertadas = factorNumeroSesionesInsertadas ;
            
            return this ;
        }

        /**
         * @param factorSesionesConsecutivasProfesor factor de puntuación en función del número de sesiones consecutivas que tenga un profesor
         * @return builder
         */
        public Builder setFactorSesionesConsecutivasProfesor(int factorSesionesConsecutivasProfesor)
        {
            this.factorSesionesConsecutivasProfesor = factorSesionesConsecutivasProfesor ;
            
            return this ;
        }

        /**
         * @param factorSesionesConsecutivasProfesorMatVes factor de puntuación en función del número de sesiones consecutivas que tenga un profesor en la primera hora vespertina
         * @return builder
         */
        public Builder setFactorSesionesConsecutivasProfesorMatVes(int factorSesionesConsecutivasProfesorMatVes)
        {
            this.factorSesionesConsecutivasProfesorMatVes = factorSesionesConsecutivasProfesorMatVes ;
            
            return this ;
        }

        /**
         * @param matrizAsignacionesMatutinas matriz de asignaciones matutinas
         * @return builder
         */
        public Builder setMatrizAsignacionesMatutinas(Asignacion[][] matrizAsignacionesMatutinas)
        {
            this.matrizAsignacionesMatutinas = matrizAsignacionesMatutinas ;
            
            return this ;
        }

        /**
         * @param matrizAsignacionesVespertinas matriz de asignaciones vespertinas
         * @return builder
         */
        public Builder setMatrizAsignacionesVespertinas(Asignacion[][] matrizAsignacionesVespertinas)
        {
            this.matrizAsignacionesVespertinas = matrizAsignacionesVespertinas ;
            
            return this ;
        }

        /**
         * Método build que construye el objeto final
         * @return una instancia de HorarioParams
         */
        public HorarioParams build()
        {
            return new HorarioParams(this) ;
        }
    }
}
