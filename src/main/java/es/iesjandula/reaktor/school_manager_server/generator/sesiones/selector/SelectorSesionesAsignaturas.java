package es.iesjandula.reaktor.school_manager_server.generator.sesiones.selector;

import java.util.Iterator;
import java.util.List;

import es.iesjandula.reaktor.school_manager_server.generator.sesiones.SesionesUtils;
import es.iesjandula.reaktor.school_manager_server.generator.threads.UltimaAsignacion;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.Asignacion;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.no_jpa.SesionBase;
import es.iesjandula.reaktor.school_manager_server.services.AsignaturaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;

public class SelectorSesionesAsignaturas extends SelectorSesionesBase
{
	/** Asignatura service */
	private AsignaturaService asignaturaService ;

	/** Ultima asignación */
	private UltimaAsignacion ultimaAsignacion ;

   	/**
	 * Constructor de la clase
	 * 
	 * @param asignaturaService asignatura service
	 * @param matrizAsignacionesMatutinas matriz de asignaciones matutinas
	 * @param matrizAsignacionesVespertinas matriz de asignaciones vespertinas
	 * @param ultimaAsignacion ultima asignación
	 */
	public SelectorSesionesAsignaturas(AsignaturaService asignaturaService,
                                       Asignacion[][] matrizAsignacionesMatutinas,
                                       Asignacion[][] matrizAsignacionesVespertinas,
                                       UltimaAsignacion ultimaAsignacion)
    {
        super(matrizAsignacionesMatutinas, matrizAsignacionesVespertinas) ;
        
        this.asignaturaService = asignaturaService ;
        this.ultimaAsignacion  = ultimaAsignacion ;
    } 

    /**
	 * @param listaDeSesiones lista de sesiones de asignaturas
	 * @return una sesión de FP asociada a la asignatura de la última asignación
	 */
	protected SesionBase obtenerSesionFPRelacionadaConUltimaAsignacion(List<SesionBase> listaDeSesiones)
	{
		SesionBase outcome = null ;

        // Primero vemos si hay una última asignación y si es una asignatura
        if (this.ultimaAsignacion != null && this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0) instanceof SesionAsignatura)
        {
			// Obtenemos la sesión de asignatura de la última asignación
			SesionBase ultimaAsignacionSesionBase = this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0) ;

			// Obtenemos la asignatura de la última asignación
            SesionAsignatura ultimaAsignacionSesionAsignatura = (SesionAsignatura) this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0) ;

			// Vemos si es FP
            boolean esFP = !ultimaAsignacionSesionAsignatura.getAsignatura().isEsoBachillerato() ;

            // Si es FP ...
            if (esFP)
            {
                // Obtenemos la asignatura de la última asignación
                Asignatura ultimaAsignacionAsignatura = ultimaAsignacionSesionAsignatura.getAsignatura() ;

                // Obtenemos el índice del curso-día de la última asignación
                int indiceCursoDia = this.ultimaAsignacion.getIndicesAsignacionSesion().getIndiceCursoDia() ;

                // Asignamos previamente la matriz de sesiones ya que nos basamos en la asignación anterior
                this.seleccionarMatrizAsignaciones(ultimaAsignacionSesionAsignatura) ;

                // Validamos si el día de la sesión es correcto
                if (SesionesUtils.sesionSinMasXOcurrenciasElMismoDia(this.getMatrizAsignaciones(), indiceCursoDia, ultimaAsignacionSesionBase))
                {
                    // Si es válido, tratamos de obtener la sesión
                    outcome = this.obtenerAsignaturaFPInternal(listaDeSesiones, ultimaAsignacionAsignatura) ;
                }
            }
        }

		return outcome ;
	}

	/**
     * Tratamos de obtener una sesión de FP asociada a la asignatura de la última asignación
	 * @param listaDeSesiones lista de sesiones
	 * @param ultimaAsignacionAsignatura asignatura de la última asignación
	 * @return una sesión de FP asociada a la asignatura de la última asignación
	 */
	private SesionBase obtenerAsignaturaFPInternal(List<SesionBase> listaDeSesiones, Asignatura ultimaAsignacionAsignatura)
	{
		SesionBase outcome = null ;

		int i = 0 ;
		int indiceSesionEncontrada = -1 ;

		// Iteramos en la lista de sesiones pendientes
		while (i < listaDeSesiones.size() && indiceSesionEncontrada == -1)
		{
			SesionBase tempSesionBase = listaDeSesiones.get(i) ;

            // Si la sesión base es una sesión de asignatura ...
            if (tempSesionBase instanceof SesionAsignatura)
            {
                SesionAsignatura tempSesionAsignatura = (SesionAsignatura) tempSesionBase ;

                // Si la asignatura de la sesión de asignatura es la misma que la asignatura pasada por parámetro ...
                if (tempSesionAsignatura.getAsignatura().equals(ultimaAsignacionAsignatura))
                {
                    // ... encontramos la sesión
                    indiceSesionEncontrada = i ;
                }
            }

			i++ ;
		}	

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
	 * @return una sesión de optativas
	 */
	protected SesionBase obtenerSesionParaAsignarEnOptativas(List<SesionBase> listaDeSesiones)
	{
		SesionBase outcome = null ;

        // Primero vemos si hay una última asignación y si es una asignatura
        if (this.ultimaAsignacion != null && this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0) instanceof SesionAsignatura)
        {
            SesionAsignatura ultimaAsignacionSesionAsignatura = (SesionAsignatura) this.ultimaAsignacion.getAsignacion().getListaSesiones().get(0) ;

            // Si la asignatura de la última asignación es una optativa ...
            if (ultimaAsignacionSesionAsignatura.getAsignatura().isOptativa())
            {
                // Obtenemos la asignatura de la última asignación
                Asignatura ultimaAsignacionAsignatura = ultimaAsignacionSesionAsignatura.getAsignatura() ;

                // ... tratamos de buscar la asignatura asociada a esta
                outcome = this.obtenerSesionParaAsignarEnOptativasInternal(listaDeSesiones, ultimaAsignacionAsignatura) ;
            }
        }

		return outcome ;
	}

	/**
	 * @param listaDeSesiones lista de sesiones
	 * @param ultimaAsignacionAsignatura asignatura de la última asignación
	 * @return una sesión de optativas
	 */
	private SesionBase obtenerSesionParaAsignarEnOptativasInternal(List<SesionBase> listaDeSesiones, Asignatura ultimaAsignacionAsignatura)
	{
		SesionBase outcome = null ;
		
		// Obtenemos la/s optativa/s de la última asignación
		List<Asignatura> bloqueOptativas = this.asignaturaService.buscaOptativasRelacionadas(ultimaAsignacionAsignatura) ;
		
		// Iteramos en la lista de sesiones pendientes
		int i = 0 ;
		int indiceSesionEncontrada = -1 ;
		while (i < listaDeSesiones.size() && indiceSesionEncontrada == -1)
		{
			SesionBase tempSesionBase = listaDeSesiones.get(i) ;

            // Si la sesión base es una sesión de asignatura ...
            if (tempSesionBase instanceof SesionAsignatura)
            {
                // Obtenemos la asignatura de la sesión de asignatura
                Asignatura tempAsignatura = ((SesionAsignatura) tempSesionBase).getAsignatura() ;

                // Si la asignatura de la sesión de asignatura es una de las optativas que no es la de la última asignación ...
                if (bloqueOptativas.contains(tempAsignatura) && !ultimaAsignacionAsignatura.equals(tempAsignatura))
                {
                    // ... encontramos la sesión
                    indiceSesionEncontrada = i ;
                }
            }

			i++ ;
		}

		// Si encontramos alguna ...
		if (indiceSesionEncontrada != -1)
		{
			// ... obtenemos la sesión, la borramos y mezclamos la lista
			outcome = this.obtenerSesionBorrarYmezclar(listaDeSesiones, indiceSesionEncontrada) ;
		}
		
		return outcome ;
	}
}
