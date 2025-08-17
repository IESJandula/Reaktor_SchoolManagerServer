package es.iesjandula.reaktor.school_manager_server.generator.sesiones.selector;

import java.util.Collections;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SelectorSesionesBase
{
	/** Matriz de asignaciones matutinas */
	private Asignacion[][] matrizAsignacionesMatutinas ;

	/** Matriz de asignaciones vespertinas */
	private Asignacion[][] matrizAsignacionesVespertinas ;

	/** Matriz de asignaciones */
	private Asignacion[][] matrizAsignaciones ;

	/**
	 * Constructor de la clase
	 * 
	 * @param matrizAsignacionesMatutinas matriz de asignaciones matutinas
	 * @param matrizAsignacionesVespertinas matriz de asignaciones vespertinas
	 */
	public SelectorSesionesBase(Asignacion[][] matrizAsignacionesMatutinas, Asignacion[][] matrizAsignacionesVespertinas)
	{
		this.matrizAsignacionesMatutinas   = matrizAsignacionesMatutinas ;
		this.matrizAsignacionesVespertinas = matrizAsignacionesVespertinas ;
	}

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @param indiceElemento índice del elemento a eliminar
	 * @return una sesión aleatoria
	 */
	protected SesionBase obtenerSesionBorrarYmezclar(List<SesionBase> listaDeSesiones, int indiceElemento)
	{
		// Ahora, eliminamos y obtenemos el primero que encontremos
		SesionBase sesion = listaDeSesiones.remove(indiceElemento) ;

		// Mezclamos aleatoriamente todas ellas
		Collections.shuffle(listaDeSesiones) ;

		return sesion ;
	}

	/**
     * Selecciona la matriz de asignaciones apropiada basada en el tipo de horario
     * @param sesion sesión
     */
    protected void seleccionarMatrizAsignaciones(SesionBase sesion)
    {
        // Si es matutino ...
        if (sesion.isTipoHorarioMatutino())
        {
        	// ... elegimos la matriz de asignaciones matutinas
            this.matrizAsignaciones = this.matrizAsignacionesMatutinas ;
        }
        else
        {
            // ... elegimos la matriz de asignaciones vespertinas
            this.matrizAsignaciones = this.matrizAsignacionesVespertinas ;
        }
    }

	/**
	 * @return matriz de asignaciones
	 */
	protected Asignacion[][] getMatrizAsignaciones()
	{
		return this.matrizAsignaciones ;
	}
}
