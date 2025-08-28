package es.iesjandula.reaktor.school_manager_server.generator.sesiones.selector;

import java.util.Collections;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.services.manager.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.generator.threads.UltimaAsignacion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectorSesionesController
{
	/** Selector de sesiones de asignaturas */
	private SelectorSesionesAsignaturas selectorSesionesAsignaturas ;

	/**
	 * Constructor de la clase
	 * 
	 * @param asignaturaService asignatura service
	 */
	public SelectorSesionesController(AsignaturaService asignaturaService)
	{
		this.selectorSesionesAsignaturas = new SelectorSesionesAsignaturas(asignaturaService) ;
	}

	/**
	 * @param sesionesPendientes sesiones pendientes
	 * @param matrizAsignacionesMatutinas matriz de asignaciones matutinas
	 * @param matrizAsignacionesVespertinas matriz de asignaciones vespertinas
	 * @param ultimaAsignacion ultima asignación
     * @return una de las sesiones pendientes de asignar
     */
    public SesionBase obtenerSesionParaAsignar(List<List<SesionBase>> sesionesPendientes,
										       Asignacion[][] matrizAsignacionesMatutinas,
											   Asignacion[][] matrizAsignacionesVespertinas,
											   UltimaAsignacion ultimaAsignacion)
    {
		// Buscamos el último índice que tenga elementos en la lista de sesiones pendientes
		int ultimoIndiceConElementos = this.buscarUltimoIndiceConElementosEnListaDeSesionesPendientes(sesionesPendientes) ;

		// Nos vamos a la lista que tenga más restricciones
		List<SesionBase> listaDeSesiones = sesionesPendientes.get(ultimoIndiceConElementos) ;
    	
		// Tratamos de obtener una sesión de asignaturas relacionadas con la última asignación
		SesionBase outcome = this.selectorSesionesAsignaturas.obtenerSesionRelacionadaConUltimaAsignacion(listaDeSesiones,
																									      matrizAsignacionesMatutinas,
																									      matrizAsignacionesVespertinas,
																									      ultimaAsignacion) ;

		// Llegados a este punto, no encontramos ninguna sesión válida ...
		if (outcome == null)
		{
			// ... elegimos una aleatoria
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, 0) ;
		}
		
		// Si este elemento era el último ... 
		if (listaDeSesiones.size() == 0)
		{
			// Borramos la sublista
			sesionesPendientes.remove(ultimoIndiceConElementos) ;
		}
				
		return outcome ;
	}

    /**
	 * @param sesionesPendientes sesiones pendientes
     * @return el último índice que tenga elementos
     */
	private int buscarUltimoIndiceConElementosEnListaDeSesionesPendientes(List<List<SesionBase>> sesionesPendientes)
	{
		int ultimoIndiceConElementos = -1 ;
    	
    	int indice = sesionesPendientes.size() - 1 ;
    	while (indice > -1 && ultimoIndiceConElementos == -1)
    	{
    		if (sesionesPendientes.get(indice).size() > 0)
    		{
    			ultimoIndiceConElementos = indice ;
    		}
    		
    		indice -- ;
    	}
    	
		return ultimoIndiceConElementos ;
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
