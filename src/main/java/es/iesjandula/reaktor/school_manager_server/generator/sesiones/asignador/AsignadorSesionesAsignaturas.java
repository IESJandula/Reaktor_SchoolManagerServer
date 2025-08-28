package es.iesjandula.reaktor.school_manager_server.generator.sesiones.asignador;

import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.generator.sesiones.SesionesUtils;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHorariaItem;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.restrictions.RestriccionHorariaThread;
import es.iesjandula.reaktor.school_manager_server.services.manager.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;

public class AsignadorSesionesAsignaturas
{
	/** Asignatura service */
	private AsignaturaService asignaturaService ;

   	/**
	 * Constructor de la clase
	 * 
	 * @param asignaturaService asignatura service
	 */
	public AsignadorSesionesAsignaturas(AsignaturaService asignaturaService)
    {
		this.asignaturaService = asignaturaService ;
    }

	/**
	 * @return asignatura service
	 */
	protected AsignaturaService getAsignaturaService()
	{
		return this.asignaturaService ;
	}

	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param asignatura asignatura
	 * @param restriccionHorariaIteracion restriccion horaria iteracion
	 * @return true si la restricción es incompatible
	 */
	protected boolean obtenerRestriccionHorariaDeSesionPorDiaTramo(Asignacion[][] matrizAsignaciones,
		                                                           Asignatura asignatura,
																   RestriccionHorariaItem restriccionHorariaItem)
	{
		boolean restriccionIncompatible = false ;

		// Si la sesión no es optativa ...
		if (!asignatura.isOptativa())
		{
			// ... y hay algo asignado, entonces no es compatible
			restriccionIncompatible = matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()] != null ;
		}
		else
		{
			// Si es optativa ...
			
			// ... Obtenemos la asignación actual
			Asignacion asignacion = matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()] ;

			// ... y vemos que la misma asignatura no esté ya asignada
			restriccionIncompatible = asignacion != null && 
										this.asignaturaEnBloqueDeOptativas(this.asignaturaService.buscaOptativasRelacionadas(asignatura), 
																			asignacion.getListaSesiones()) ;						
		}					  

		return restriccionIncompatible ;
	}

    /**
     * @param bloqueOptativas bloque de optativas
     * @param listaSesiones lista de sesiones a verificar si alguna de las asignaturas pertenece al bloque de optativas
     * @return true si alguna de las asignaturas pertenece al bloque de optativas
     */
	private boolean asignaturaEnBloqueDeOptativas(List<Asignatura> bloqueOptativas, List<SesionBase> listaSesiones)
	{
		boolean outcome = false ;
		
		// Vemos si la lista de sesiones tiene elementos de tipo SesionAsignatura
		if (listaSesiones.size() > 0 && listaSesiones.get(0) instanceof SesionAsignatura)
		{
			// Iteramos sobre todas las sesiones de esta asignación para ver si son del bloque de optativas
			int k = 0 ;
			while (k < listaSesiones.size() && !outcome)
			{
				// Obtenemos la sesión de esta asignación
				SesionAsignatura sesionAsignatura = (SesionAsignatura) listaSesiones.get(k) ;
				
				// Verificamos si pertenece la asignatura al bloque de optativas que estamos buscando
				outcome = bloqueOptativas.contains(sesionAsignatura.getAsignatura()) ;

				k++ ;
			}
		}

		return outcome ;
	}

    /**
	 * @param matrizAsignaciones matriz de asignaciones
     * @param asignatura asignatura
	 * @param restriccionHorariaThread restriccion horaria thread
	 * @param restriccionesHorarias restricciones horarias
     * @return true si se ha encontrado la misma asignatura en el índice
     */
	protected boolean obtenerRestriccionHorariaDeSesionPorOptativas(Asignacion[][] matrizAsignaciones,
		                                                            Asignatura asignatura,
																	RestriccionHorariaThread restriccionHorariaThread,
																	List<RestriccionHorariaItem> restriccionesHorarias)
	{
		boolean encontrado = false ;

		// Iteramos y obtenemos las sesiones actualmente asociadas a cada día
		Iterator<RestriccionHorariaItem> iterator = restriccionesHorarias.iterator() ;
		while (iterator.hasNext() && !encontrado)
		{
			// Obtenemos el siguiente item de la restricción horaria
			RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

			// Si:
				// justo en este día y tramo horario hay alguna asignación y
				// la lista de sesiones es mayor que cero y
				// es una asignatura y
				// es optativa y 
				// no es la misma que la que estamos buscando ...
			encontrado = matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()] != null &&
						 matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()].getListaSesiones().size() > 0 &&
						 matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()].getListaSesiones().get(0) instanceof SesionAsignatura &&
						 matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()].isOptativas() &&
						!SesionesUtils.buscarAsignaturaEnAsignacion(matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()], asignatura) ;

			// ... y si, además:
				
			// si la asignatura es del bloque de optativas
			if (encontrado)
			{
				// Obtenemos la sesión de asignatura
				SesionAsignatura sesionAsignatura = (SesionAsignatura) matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()].getListaSesiones().get(0) ;

				// ... y si la asignatura es del bloque de optativas ...
				encontrado = sesionAsignatura.getAsignatura().getBloqueId().getId() == asignatura.getBloqueId().getId() ;
				if (encontrado)
				{
					// ... entonces la hacemos coincidir con la optativa del bloque
					restriccionHorariaThread.hacerCoincidirConOptativaDelBloque(restriccionHorariaItem.getIndiceDia(), restriccionHorariaItem.getTramoHorario()) ;
				}
			}
		}

		return encontrado ;
	}

    /**
	 * @param matrizAsignaciones matriz de asignaciones
     * @param asignatura asignatura
     * @param restriccionHorariaThread restriccion horaria thread
     * @param restriccionesHorarias restricciones horarias
     * @return true si se ha encontrado la misma asignatura en el índice
     */
    protected boolean obtenerRestriccionHorariaDeSesionPorModuloFp(Asignacion[][] matrizAsignaciones,
		                                                           Asignatura asignatura,
																   RestriccionHorariaThread restriccionHorariaThread,
																   List<RestriccionHorariaItem> restriccionesHorarias)
    {
		boolean encontrado = false ;

		// Iteramos y obtenemos las sesiones actualmente asociadas a cada día
		Iterator<RestriccionHorariaItem> iterator = restriccionesHorarias.iterator() ;
		while (iterator.hasNext() && !encontrado)
		{
			// Obtenemos el siguiente item de la restricción horaria
			RestriccionHorariaItem restriccionHorariaItem = iterator.next() ;

			// Si justo en este día y tramo horario hay una asignación, la borramos de las restricciones horarias
			if (matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()] != null)
			{
				restriccionHorariaThread.eliminarRestriccionHorariaItem(restriccionHorariaItem) ;
			}
			else
			{
				// Verificamos si hay un índice donde se pueda realizar la asignación de la asignatura
				encontrado = this.obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndice(matrizAsignaciones, asignatura, restriccionHorariaThread, restriccionHorariaItem) ;
			}
		}

		return encontrado ;
    }

	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param asignatura asignatura
	 * @param restriccionHorariaThread restriccion horaria thread
	 * @param restriccionHorariaItem restriccion horaria item
	 * @return true si se ha encontrado el índice
	 */
	private boolean obtenerRestriccionHorariaDeSesionPorModuloFpEncontrarIndice(Asignacion[][] matrizAsignaciones,
		                                                                        Asignatura asignatura,
																				RestriccionHorariaThread restriccionHorariaThread,
																				RestriccionHorariaItem restriccionHorariaItem)
	{
		boolean outcome = false ;

		// Obtenemos el índice del curso y día
		int indiceCursoDia = restriccionHorariaItem.getIndiceDia() ;

		// Buscamos la asignatura en el día concreto
		int indiceTramoAsignaturaEncontrado = this.buscarIndiceTramoAsignaturaEnDiaConcreto(matrizAsignaciones, indiceCursoDia, asignatura) ;

		// Si no se ha encontrado, es porque en este día no está la asignatura aún en este día
		// Por ello, verificamos esta restricción horaria por si se puede incluir como asignación
		if (indiceTramoAsignaturaEncontrado == -1)
		{
			outcome = this.asignarRestriccionHorariaItemSiAunNoAsignadoAlDia(matrizAsignaciones, restriccionHorariaItem, restriccionHorariaThread) ;
		}
		else
		{
			// Si llegamos aquí es porque la asignatura ya se asignó previamente y ahora tenemos que ver si hay algún hueco
			outcome = this.asignarRestriccionHorariaItemSiAsignadoAlDia(matrizAsignaciones, restriccionHorariaItem, restriccionHorariaThread, indiceTramoAsignaturaEncontrado) ;
		}

		return outcome ;
	}

	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param indiceCursoDia índice curso día
	 * @param asignatura asignatura
	 * @return índice del tramo de la asignatura en el día
	 */
	private int buscarIndiceTramoAsignaturaEnDiaConcreto(Asignacion[][] matrizAsignaciones, int indiceCursoDia, Asignatura asignatura)
	{
		int indiceTramoAsignaturaEncontrado = -1 ;
		int i = 0 ;
		// Iteramos este día sobre la matriz de asignaciones y buscamos aquella que posea la misma asignatura
		while (i < matrizAsignaciones[indiceCursoDia].length && indiceTramoAsignaturaEncontrado == -1)
		{
			// Si hay una asignación y es la misma asignatura de esta sesion, la hemos encontrado
			if (matrizAsignaciones[indiceCursoDia][i] != null && SesionesUtils.buscarAsignaturaEnAsignacion(matrizAsignaciones[indiceCursoDia][i], asignatura))
			{
				indiceTramoAsignaturaEncontrado = i ;
			}

			i++ ;
		}

		return indiceTramoAsignaturaEncontrado ;
	}

	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param restriccionHorariaItem restriccion horaria item
	 * @param restriccionHorariaThread restriccion horaria thread
	 * @return true si se ha asignado la restricción horaria item
	 */
	private boolean asignarRestriccionHorariaItemSiAunNoAsignadoAlDia(Asignacion[][] matrizAsignaciones,
																     RestriccionHorariaItem restriccionHorariaItem, 
																	 RestriccionHorariaThread restriccionHorariaThread)
	{
		// Vemos si la matriz está vacía en el día y tramo de la restricción horaria que viene como parámetro 
		boolean outcome = matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][restriccionHorariaItem.getTramoHorario()] == null ;
		
		if (outcome)
		{
			// Si está vacía, se asigna a este día concreto, cualquiera de los tramos que tenga
			restriccionHorariaThread.asignarUnDiaConcreto(restriccionHorariaItem.getIndiceDia()) ;
		}
		else
		{
			// Si no está vacía, se elimina la restricción horaria del día y tramo
			restriccionHorariaThread.eliminarRestriccionHorariaItem(restriccionHorariaItem) ;
		}

		return outcome ;
	}


	/**
	 * @param matrizAsignaciones matriz de asignaciones
	 * @param restriccionHorariaItem restriccion horaria item
	 * @param restriccionHorariaThread restriccion horaria thread
	 * @param indiceTramoAsignaturaEncontrado indice del tramo de la asignatura
	 * @return true si se ha asignado la restricción horaria item
	 */
	private boolean asignarRestriccionHorariaItemSiAsignadoAlDia(Asignacion[][] matrizAsignaciones,
															     RestriccionHorariaItem restriccionHorariaItem,
																 RestriccionHorariaThread restriccionHorariaThread,
															     int indiceTramoAsignaturaEncontrado)
	{
		// Este parámetro nos ayudará a saber la restricción horaria compatible que está en las restricciones horarias
		RestriccionHorariaItem restriccionHorariaItemEncontrada = null ;
		
		// Verificamos si es un índice intermedio
		boolean indiceIntermedio = indiceTramoAsignaturaEncontrado > 0 && indiceTramoAsignaturaEncontrado < Constants.NUMERO_TRAMOS_HORARIOS - 1 ;

		// Si es un índice intermedio y ...
		if (indiceIntermedio)
		{
			// ... y la posición previa está vacía, se probará aquí si hay restricción horaria
			if (matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][indiceTramoAsignaturaEncontrado - 1] == null)
			{
				restriccionHorariaItemEncontrada = restriccionHorariaThread.buscarRestriccionHorariaPorDiaTramo(restriccionHorariaItem.getIndiceDia(), indiceTramoAsignaturaEncontrado - 1) ;
			}
			
			// Si en el anterior if no se encontró y la siguiente posición está vacía, se probará aquí si hay restricción horaria
			if (restriccionHorariaItemEncontrada == null && matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][indiceTramoAsignaturaEncontrado + 1] == null)
			{
				restriccionHorariaItemEncontrada = restriccionHorariaThread.buscarRestriccionHorariaPorDiaTramo(restriccionHorariaItem.getIndiceDia(), indiceTramoAsignaturaEncontrado + 1) ;
			}
		}
		// Si es el primer índice y el segundo está vacío, se puede considerar como posible índice
		else if (indiceTramoAsignaturaEncontrado == 0 &&
				 matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][1] == null)
		{
			restriccionHorariaItemEncontrada = restriccionHorariaThread.buscarRestriccionHorariaPorDiaTramo(restriccionHorariaItem.getIndiceDia(), 1) ;
		}
		// Si es el último índice y el penúltimo está vacío, se puede considerar como posible índice
		else if (indiceTramoAsignaturaEncontrado == Constants.NUMERO_TRAMOS_HORARIOS - 1 &&
				 matrizAsignaciones[restriccionHorariaItem.getIndiceDia()][Constants.NUMERO_TRAMOS_HORARIOS - 2] == null)
		{
			restriccionHorariaItemEncontrada = restriccionHorariaThread.buscarRestriccionHorariaPorDiaTramo(restriccionHorariaItem.getIndiceDia(), Constants.NUMERO_TRAMOS_HORARIOS - 2) ;
		}

		// Si no está en las restricciones horarias, no se puede asignar
		if (restriccionHorariaItemEncontrada == null)
		{
			// ... se elimina el día completo
			restriccionHorariaThread.eliminarDiaConcreto(restriccionHorariaItem.getIndiceDia()) ;
		}
		else
		{
			// Se realiza la asignación concreta
			restriccionHorariaThread.asignarUnDiaTramoConcreto(restriccionHorariaItemEncontrada.getIndiceDia(), restriccionHorariaItemEncontrada.getTramoHorario()) ;
		}

		return restriccionHorariaItemEncontrada != null ;
	}
}
