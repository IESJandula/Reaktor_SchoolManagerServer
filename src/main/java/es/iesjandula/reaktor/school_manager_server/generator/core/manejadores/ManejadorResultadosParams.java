package es.iesjandula.reaktor.school_manager_server.generator.core.manejadores;

import es.iesjandula.reaktor.school_manager_server.services.GeneradorService;

public class ManejadorResultadosParams
{
    /** Umbral mínimo para considerar una solución válida */
    private int umbralMinimoSolucion ;

    /** Generador de horarios */
    private GeneradorService generadorService ;
    
    /**
     * Constructor privado para forzar el uso del Builder
     * 
     * @param builder builder interno
     */
    private ManejadorResultadosParams(Builder builder)
    {
        this.umbralMinimoSolucion = builder.umbralMinimoSolucion ;
        this.generadorService     = builder.generadorService ;
    }

    /**
     * @return umbral mínimo para considerar una solución válida
     */
    public int getUmbralMinimoSolucion()
    {
        return this.umbralMinimoSolucion ;
    }

    /**
     * @return generador de horarios
     */
    public GeneradorService getGeneradorService()
    {
        return this.generadorService ;
    }

    /**
     * Clase estática interna Builder
     */
    public static class Builder
    {
        /** Umbral mínimo para considerar una solución válida */
        private int umbralMinimoSolucion ;
        
        /** Generador de horarios */
        private GeneradorService generadorService ;

        /**
         * @param umbral mínimo para considerar una solución válida
         * @return builder
         */
        public Builder setUmbralMinimoSolucion(int umbralMinimoSolucion)
        {
            this.umbralMinimoSolucion = umbralMinimoSolucion ;
            return this ;
        }

        /**
         * @param generadorService generador de horarios
         * @return builder
         */
        public Builder setGeneradorService(GeneradorService generadorService)
        {
            this.generadorService = generadorService ;
            return this ;
        }

        /**
         * Método build que construye el objeto final
         * @return una instancia de ManejadorResultadosParams
         */
        public ManejadorResultadosParams build()
        {
            return new ManejadorResultadosParams(this) ;
        }
    }
}
