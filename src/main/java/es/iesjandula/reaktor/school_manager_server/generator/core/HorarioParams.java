package es.iesjandula.reaktor.school_manager_server.generator.core;

import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.services.GeneradorService;

public class HorarioParams
{
	/** Número de cursos matutinos */
	private int numeroCursosMatutinos ;
	
	/** Número de cursos vespertinos */
	private int numeroCursosVespertinos ;

    /** Matriz de asignaciones matutinas */
    private Asignacion[][] matrizAsignacionesMatutinas ;

    /** Matriz de asignaciones vespertinas */
    private Asignacion[][] matrizAsignacionesVespertinas ;

    /** GeneradorService */
    private GeneradorService generadorService ;

    /** GeneradorInstancia */
    private GeneradorInstancia generadorInstancia ;

    /**
     * Constructor privado para forzar el uso del Builder
     * 
     * @param builder builder interno
     */
    private HorarioParams(Builder builder)
    {
    	this.numeroCursosMatutinos				      = builder.numeroCursosMatutinos ;
    	this.numeroCursosVespertinos			      = builder.numeroCursosVespertinos ;
        this.matrizAsignacionesMatutinas              = builder.matrizAsignacionesMatutinas ;
        this.matrizAsignacionesVespertinas            = builder.matrizAsignacionesVespertinas ;
        this.generadorService                         = builder.generadorService ;
        this.generadorInstancia                       = builder.generadorInstancia ;
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
     * @return generadorService
     */
    public GeneradorService getGeneradorService()
    {
        return this.generadorService ;
    }

    /**
     * @return generadorInstancia
     */
    public GeneradorInstancia getGeneradorInstancia()
    {
        return this.generadorInstancia ;
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
    	
        /** Matriz de asignaciones matutinas */
        private Asignacion[][] matrizAsignacionesMatutinas ;

        /** Matriz de asignaciones vespertinas */
        private Asignacion[][] matrizAsignacionesVespertinas ;
        
        /** GeneradorService */
        private GeneradorService generadorService ;

        /** GeneradorInstancia */
        private GeneradorInstancia generadorInstancia ;

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
         * @param generadorService generadorService
         * @return builder
         */
        public Builder setGeneradorService(GeneradorService generadorService)
        {
            this.generadorService = generadorService ;
            
            return this ;
        }

        /**
         * @param generadorInstancia generadorInstancia
         * @return builder
         */
        public Builder setGeneradorInstancia(GeneradorInstancia generadorInstancia)
        {
            this.generadorInstancia = generadorInstancia ;
            
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
