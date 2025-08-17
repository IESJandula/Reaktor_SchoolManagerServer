package es.iesjandula.reaktor.school_manager_server.generator.sesiones;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionReduccion;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import java.util.Iterator;

public class SesionesUtils
{
    /**
     * @param matrizAsignaciones matriz de asignaciones
     * @param indiceCursoDia curso asociado a esta clase
     * @param sesionBase sesión base
     * @return true si la sesión no se repite el mismo día
     */
    public static boolean sesionSinMasXOcurrenciasElMismoDia(Asignacion[][] matrizAsignaciones, int indiceCursoDia, SesionBase sesionBase)
    {
        // Por defecto, el número máximo de ocurrencias por día es el de FP
        int numeroMaximoOcurrenciasPorDia =  Constants.NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_FP ;
        if (sesionBase.isEsoBachillerato())
        {
            // Si es una asignatura de ESO o BACH, el número máximo de ocurrencias por día es el de ESO o BACH
            numeroMaximoOcurrenciasPorDia = Constants.NUMERO_MAXIMO_OCURRENCIAS_POR_DIA_ESO_BACH ;
        }

        // Si es una asignatura, buscamos la asignatura en la asignación
        return SesionesUtils.sesionSinMasXOcurrenciasElMismoDia(matrizAsignaciones, indiceCursoDia, sesionBase, numeroMaximoOcurrenciasPorDia) ;
    }

    /**
     * @param matrizAsignaciones matriz de asignaciones
     * @param indiceCursoDia índice del curso-día
     * @param sesionBase sesión base
     * @param numeroMaximoOcurrenciasPorDia número máximo de ocurrencias por día
     * @return true si la asignatura cumple con las ocurrencias, false en caso contrario
     */
    private static boolean sesionSinMasXOcurrenciasElMismoDia(Asignacion[][] matrizAsignaciones, int indiceCursoDia, SesionBase sesionBase, int numeroMaximoOcurrenciasPorDia)
    {
        boolean outcome = true ;

		int vecesImpartida = 0 ;
        
        int i = 0 ;
        while (i < matrizAsignaciones[indiceCursoDia].length && outcome)
        {
			// Obtenemos la asignación
			Asignacion asignacion = matrizAsignaciones[indiceCursoDia][i] ;

			// Si hay asignaturas en la asignación ...
            if (asignacion != null && asignacion.getListaSesiones() != null && asignacion.getListaSesiones().size() > 0)
            {
                // ... y la sesión es de tipo asignatura y encontramos la asignatura en la asignación
                if (asignacion.getListaSesiones().get(0) instanceof SesionAsignatura && SesionesUtils.buscarAsignaturaEnAsignacion(asignacion, ((SesionAsignatura) sesionBase).getAsignatura()))
                {
                    vecesImpartida ++ ;

                    outcome = vecesImpartida < numeroMaximoOcurrenciasPorDia ;
                }
                else
                {
                    // ... y la sesión es de tipo reducción y encontramos la reducción en la asignación
                    if (asignacion.getListaSesiones().get(0) instanceof SesionReduccion && SesionesUtils.buscarReduccionEnAsignacion(asignacion, ((SesionReduccion) sesionBase).getReduccion()))
                    {
                        vecesImpartida ++ ;

                        outcome = vecesImpartida < numeroMaximoOcurrenciasPorDia ;
                    }
                }
            }

            i++ ;
        }
        
        return outcome ;
    }

    /**
	 * @param asignacion asignacion
	 * @param asignatura asignatura
	 * @return true si se ha encontrado la misma asignatura
	 */
	public static boolean buscarAsignaturaEnAsignacion(Asignacion asignacion, Asignatura asignatura)
	{
        // Llegados a este punto, sabemos que los elementos de la asignación son SesionesAsignatura
		boolean encontrado = false ;
		
		// Si hay una asignación, verificamos
		if (asignacion != null && asignacion.getListaSesiones() != null && asignacion.getListaSesiones().size() > 0)
		{
			// Iteramos y verificamos si alguna sesión corresponde a la misma asignatura
			Iterator<SesionBase> iterator = asignacion.getListaSesiones().iterator() ;
			while (iterator.hasNext() && !encontrado)
			{
                // Obtenemos la sesión de asignatura
				SesionAsignatura sesionAsignaturaTemp = (SesionAsignatura) iterator.next() ;
				
				encontrado = sesionAsignaturaTemp.getAsignatura().equals(asignatura) ;
			}
		}

		return encontrado ;
	}

    /**
     * @param asignacion asignacion
     * @param reduccion reduccion
     * @return true si se ha encontrado la misma reducción
     */
    private static boolean buscarReduccionEnAsignacion(Asignacion asignacion, Reduccion reduccion)
    {
        // Llegados a este punto, sabemos que los elementos de la asignación son SesionesReduccion
        boolean encontrado = false ;

        // Si hay una asignación, verificamos
        if (asignacion != null && asignacion.getListaSesiones() != null && asignacion.getListaSesiones().size() > 0)
        {
            // Iteramos y verificamos si alguna sesión corresponde a la misma reducción
            Iterator<SesionBase> iterator = asignacion.getListaSesiones().iterator() ;
            while (iterator.hasNext() && !encontrado)
            {
                // Obtenemos la sesión de reducción
                SesionReduccion sesionReduccionTemp = (SesionReduccion) iterator.next() ;
            }

            return encontrado ;
        }

        return false ;
    }
}
