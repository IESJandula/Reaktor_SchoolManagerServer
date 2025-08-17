package es.iesjandula.reaktor.school_manager_server.utils;

import java.util.ArrayList;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;

public class CopiaEstructuras
{
	/**
	 * @param listaOriginal lista original de sesiones
	 * @return una copia de la lista original de sesiones
	 */
	public static List<List<SesionBase>> copiarListaDeSesiones(List<List<SesionBase>> listaOriginal)
	{
        List<List<SesionBase>> copia = new ArrayList<>() ;

        synchronized(listaOriginal)
        {
            for (List<SesionBase> sublista : listaOriginal)
            {
                // Creamos una nueva sublista para cada sublista original
                List<SesionBase> copiaSublista = new ArrayList<SesionBase>(sublista) ;
                
                // Añadimos la copia de la sublista a la nueva lista
                copia.add(copiaSublista) ;
            }
        }

        return copia ;
	}

	
    /**
     * @param matrizAsignaciones matriz con las asignaciones
     * @return una copia de la matriz de las asignaciones
     */
    public static Asignacion[][] copiarMatriz(Asignacion[][] matrizAsignaciones)
    {
        Asignacion[][] copia = new Asignacion[matrizAsignaciones.length][matrizAsignaciones[0].length] ;

        synchronized(matrizAsignaciones)
        {
            for (int i = 0 ; i < matrizAsignaciones.length ; i++)
            {
                for (int j = 0 ; j < matrizAsignaciones[i].length ; j++)
                {
                    copia[i][j] = matrizAsignaciones[i][j] ;
                }
            }
        }
        
        return copia ;
    }
}
