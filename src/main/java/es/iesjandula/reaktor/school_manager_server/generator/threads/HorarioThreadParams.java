package es.iesjandula.reaktor.school_manager_server.generator.threads;

import java.util.Map;

import es.iesjandula.reaktor.school_manager_server.services.manager.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.services.timetable.GeneradorService;

public class HorarioThreadParams
{
    /** Mapa que correlaciona los nombres de los cursos matutinos con el índice que ocupan en la matriz de sesiones */
    private Map<String, Integer> mapCorrelacionadorCursosMatutinos ;

    /** Mapa que correlaciona los nombres de los cursos vespertinos con el índice que ocupan en la matriz de sesiones */
    private Map<String, Integer> mapCorrelacionadorCursosVespertinos ;

    /** Asignatura service */
    private AsignaturaService asignaturaService ;

    /** Generador service */
    private GeneradorService generadorService ;

    /** Umbral mínimo para considerar una solución válida */
    private int umbralMinimoSolucion ;

    /**
     * Constructor privado para forzar el uso del Builder
     * 
     * @param builder builder interno
     */
    private HorarioThreadParams(Builder builder)
    {
        this.mapCorrelacionadorCursosMatutinos   = builder.mapCorrelacionadorCursosMatutinos ;
        this.mapCorrelacionadorCursosVespertinos = builder.mapCorrelacionadorCursosVespertinos ;
        this.asignaturaService                   = builder.asignaturaService ;
        this.generadorService                    = builder.generadorService ;
        this.umbralMinimoSolucion                = builder.umbralMinimoSolucion ;
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
     * @return asignatura service
     */
    public AsignaturaService getAsignaturaService()
    {
        return this.asignaturaService ;
    }

    /**
     * @return generador service
     */
    public GeneradorService getGeneradorService()
    {
        return this.generadorService ;
    }

    /**
     * @return umbral mínimo para considerar una solución válida
     */
    public int getUmbralMinimoSolucion()
    {
        return this.umbralMinimoSolucion ;
    }

    /**
     * Clase estática interna Builder
     */
    public static class Builder
    {
        /** Mapa que correlaciona los nombres de los cursos matutinos con el índice que ocupan en la matriz de sesiones */
        private Map<String, Integer> mapCorrelacionadorCursosMatutinos;

        /** Mapa que correlaciona los nombres de los cursos vespertinos con el índice que ocupan en la matriz de sesiones */
        private Map<String, Integer> mapCorrelacionadorCursosVespertinos;

        /** Asignatura service */
        private AsignaturaService asignaturaService ;

        /** Generador service */
        private GeneradorService generadorService ;

        /** Umbral mínimo para considerar una solución válida */
        private int umbralMinimoSolucion ;

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
         * @param asignaturaService asignatura service
         * @return builder
         */
        public Builder setAsignaturaService(AsignaturaService asignaturaService)
        {
            this.asignaturaService = asignaturaService ;
            
            return this ;
        }

        /**
         * @param generadorService generador service
         * @return builder
         */
        public Builder setGeneradorService(GeneradorService generadorService)
        {
            this.generadorService = generadorService ;
            
            return this ;
        }

        /**
         * @param umbralMinimoSolucion umbral mínimo para considerar una solución válida
         * @return builder
         */
        public Builder setUmbralMinimoSolucion(int umbralMinimoSolucion)
        {
            this.umbralMinimoSolucion = umbralMinimoSolucion ;

            return this ;
        }

        /**
         * Método build que construye el objeto final
         * @return una instancia de HorarioThreadParams
         */
        public HorarioThreadParams build()
        {
            return new HorarioThreadParams(this) ;
        }
    }
}
