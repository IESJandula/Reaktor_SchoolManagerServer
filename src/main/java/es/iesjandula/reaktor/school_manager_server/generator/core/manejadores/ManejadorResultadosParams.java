package es.iesjandula.reaktor.school_manager_server.generator.core.manejadores;

import es.iesjandula.reaktor.school_manager_server.services.AlmacenadorHorarioService;

public class ManejadorResultadosParams
{
    /** Umbral mínimo para considerar una solución válida */
    private int umbralMinimoSolucion ;

    /** Umbral mínimo para considerar un error */
    private int umbralMinimoError ;

    /** Almacenador de horarios */
    private AlmacenadorHorarioService almacenadorHorarioService ;
    
    /**
     * Constructor privado para forzar el uso del Builder
     * 
     * @param builder builder interno
     */
    private ManejadorResultadosParams(Builder builder)
    {
        this.umbralMinimoSolucion      = builder.umbralMinimoSolucion ;
        this.umbralMinimoError         = builder.umbralMinimoError ;
        this.almacenadorHorarioService = builder.almacenadorHorarioService ;
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
     * @return almacenador de horarios
     */
    public AlmacenadorHorarioService getAlmacenadorHorarioService()
    {
        return this.almacenadorHorarioService ;
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

        /** Almacenador de horarios */
        private AlmacenadorHorarioService almacenadorHorarioService ;

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
         * @param almacenadorHorarioService almacenador de horarios
         * @return builder
         */
        public Builder setAlmacenadorHorarioService(AlmacenadorHorarioService almacenadorHorarioService)
        {
            this.almacenadorHorarioService = almacenadorHorarioService ;
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
