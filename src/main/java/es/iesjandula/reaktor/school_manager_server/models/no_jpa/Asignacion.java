package es.iesjandula.reaktor.school_manager_server.models.no_jpa;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Asignacion
{
	/** Lista de sesiones - Normalmente será un único item, pero si hay más es que es una optativa */
	private List<Sesion> listaSesiones ;
	
	/** True si la asignación es de optativas */
	private boolean optativas ;

	/**
	 * Constructor vacío
	 */
	public Asignacion()
	{
		this.listaSesiones = new ArrayList<Sesion>() ;
		this.optativas  = false ;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder() ;
		
		if (this.listaSesiones != null)
		{
			builder.append("[") ;
			
			for (int i = 0 ; i < this.listaSesiones.size() ; i++)
			{
				Sesion sesion = this.listaSesiones.get(i) ;
				
				builder.append(sesion) ;
				
				if (i + 1 < this.listaSesiones.size())
				{
					builder.append("-") ;
				}
			}
			
			builder.append("]") ;
		}
		
		return builder.toString() ;
	}
}
