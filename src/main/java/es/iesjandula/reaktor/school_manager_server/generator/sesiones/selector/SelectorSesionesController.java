package es.iesjandula.reaktor.school_manager_server.generator.sesiones.selector;

import java.util.Collections;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.services.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.generator.threads.UltimaAsignacion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectorSesionesController
{
    /** Sesiones pendientes */
    private List<List<SesionBase>> sesionesPendientes ;

	/** Selector de sesiones de asignaturas */
	private SelectorSesionesAsignaturas selectorSesionesAsignaturas ;

	/**
	 * Constructor de la clase
	 * 
	 * @param asignaturaService asignatura service
	 * @param sesionesPendientes sesiones pendientes
	 * @param matrizAsignacionesMatutinas matriz de asignaciones matutinas
	 * @param matrizAsignacionesVespertinas matriz de asignaciones vespertinas
	 * @param ultimaAsignacion ultima asignación
	 */
	public SelectorSesionesController(AsignaturaService asignaturaService,
									 List<List<SesionBase>> sesionesPendientes,
									 Asignacion[][] matrizAsignacionesMatutinas,
									 Asignacion[][] matrizAsignacionesVespertinas,
									 UltimaAsignacion ultimaAsignacion)
	{
		this.sesionesPendientes = sesionesPendientes ;

		this.selectorSesionesAsignaturas = new SelectorSesionesAsignaturas(asignaturaService,
																		   matrizAsignacionesMatutinas,
																		   matrizAsignacionesVespertinas,
																		   ultimaAsignacion) ;
	}

	/**
	 * @return matriz de asignaciones
	 */
	public Asignacion[][] getMatrizAsignaciones()
	{
		return this.selectorSesionesAsignaturas.getMatrizAsignaciones() ;
	}

	/**
     * @return una de las sesiones pendientes de asignar
     */
    public SesionBase obtenerSesionParaAsignar()
    {
		// Buscamos el último índice que tenga elementos en la lista de sesiones pendientes
		int ultimoIndiceConElementos = this.buscarUltimoIndiceConElementosEnListaDeSesionesPendientes() ;

		// Nos vamos a la lista que tenga más restricciones
		List<SesionBase> listaDeSesiones = this.sesionesPendientes.get(ultimoIndiceConElementos) ;
    	
		// Tratamos de obtener una sesión de asignaturas FP relacionada con la última asignación
		SesionBase outcome = this.selectorSesionesAsignaturas.obtenerSesionFPRelacionadaConUltimaAsignacion(listaDeSesiones) ;
		
		// Nos centramos en las restricciones de la lista en la que estemos
		if (outcome == null && ultimoIndiceConElementos == Constants.INDEX_CON_CONCILIACIONES)
		{
			// Tratamos de obtener una sesión para asignar relacionada con las conciliaciones
			outcome = this.obtenerSesionParaAsignarEnConciliaciones(listaDeSesiones) ;
		}
		
		// Nos centramos en las restricciones de la lista de optativas
		if (outcome == null && ultimoIndiceConElementos == Constants.INDEX_CON_OPTATIVAS)
		{
			outcome = this.selectorSesionesAsignaturas.obtenerSesionParaAsignarEnOptativas(listaDeSesiones) ;
		}

		// Llegados a este punto, no encontramos ninguna sesión válida ...
		if (outcome == null)
		{
			// ... elegimos una aleatoria
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, 0) ;
		}

		// Llegados a este punto, ya tenemos firmemente la matriz de sesiones
		this.selectorSesionesAsignaturas.seleccionarMatrizAsignaciones(outcome) ;
		
		// Si este elemento era el último ... 
		if (listaDeSesiones.size() == 0)
		{
			// Borramos la sublista
			this.sesionesPendientes.remove(ultimoIndiceConElementos) ;
		}
				
		return outcome ;
	}

    /**
     * @return el último índice que tenga elementos
     */
	private int buscarUltimoIndiceConElementosEnListaDeSesionesPendientes()
	{
		int ultimoIndiceConElementos = -1 ;
    	
    	int indice = this.sesionesPendientes.size() - 1 ;
    	while (indice > -1 && ultimoIndiceConElementos == -1)
    	{
    		if (this.sesionesPendientes.get(indice).size() > 0)
    		{
    			ultimoIndiceConElementos = indice ;
    		}
    		
    		indice -- ;
    	}
    	
		return ultimoIndiceConElementos ;
	}
	
	/**
	 * @param listaDeSesiones lista de sesiones
	 * @return una sesión con la conciliación de entrar a primera hora y sino, alguna otra
	 */
	private SesionBase obtenerSesionParaAsignarEnConciliaciones(List<SesionBase> listaDeSesiones)
	{
		// Me interesa asignar primero las conciliaciones de entrar a primera hora
		int i = 0 ;
		int indiceSesionEncontrada = -1 ;
		while (i < listaDeSesiones.size() && indiceSesionEncontrada == -1)
		{
			SesionBase temp = listaDeSesiones.get(i) ;

			ObservacionesAdicionales observacionesAdicionales = temp.getProfesor().getObservacionesAdicionales() ;

			if (observacionesAdicionales != null && observacionesAdicionales.getConciliacion() != null && observacionesAdicionales.getConciliacion())
			{
				indiceSesionEncontrada = i ;
			}

			i++ ;
		}	

		SesionBase outcome = null ;

		// Si encontramos alguna ...
		if (indiceSesionEncontrada != -1)
		{
			// ... obtenemos la sesión, la borramos y mezclamos la lista
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, indiceSesionEncontrada) ;
		}

		return outcome ;
	}

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @param indiceElemento índice del elemento a eliminar
	 * @return una sesión aleatoria
	 */
	private SesionBase obtenerSesionBorrarYmezclar(List<SesionBase> listaDeSesiones, int indiceElemento)
	{
		// Ahora, eliminamos y obtenemos el primero que encontremos
		SesionBase sesion = listaDeSesiones.remove(indiceElemento) ;

		// Mezclamos aleatoriamente todas ellas
		Collections.shuffle(listaDeSesiones) ;

		return sesion ;
	}
}
