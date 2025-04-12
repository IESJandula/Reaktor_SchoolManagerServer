package es.iesjandula.reaktor.school_manager_server.generator.core.manejadores;

public class ManejadorResultadosParams
{
    /** Umbral mínimo para considerar una solución válida */
    private int umbralMinimoSolucion ;

    /** Umbral mínimo para considerar un error */
    private int umbralMinimoError ;
    
    /**
     * Constructor privado para forzar el uso del Builder
     * 
     * @param builder builder interno
     */
    private ManejadorResultadosParams(Builder builder)
    {
        this.umbralMinimoSolucion = builder.umbralMinimoSolucion ;
        this.umbralMinimoError    = builder.umbralMinimoError ;
    }

    /**
     * @return umbral mínimo para considerar una solución válida
     */
    public int getUmbralMinimoSolucion()
    {
        return this.umbralMinimoSolucion ;
    }

    /**
     * @return umbral mínimo para considerar un error
     */
    public int getUmbralMinimoError()
    {
        return this.umbralMinimoError ;
    }

    /**
     * Clase estática interna Builder
     */
    public static class Builder
    {
        /** Umbral mínimo para considerar una solución válida */
        private int umbralMinimoSolucion ;

        /** Umbral mínimo para considerar un error */
        private int umbralMinimoError ;

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
         * @param umbral mínimo para considerar un error
         * @return builder
         */
        public Builder setUmbralMinimoError(int umbralMinimoError)
        {
            this.umbralMinimoError = umbralMinimoError ;
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
