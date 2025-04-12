package es.iesjandula.reaktor.school_manager_server.generator.models;

import java.util.Objects;

import es.iesjandula.reaktor.school_manager_server.generator.models.enums.Conciliacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Profesor
{
	/** Email del profesor */
	private String email ;
	
	/** Nombre del profesor */
    private String nombre ;
    
    /** Apellidos del profesor */
    private String apellidos ;

    /** Conciliacion */
    private Conciliacion conciliacion ;

    @Override
    public String toString()
    {
    	return this.nombre + " " + this.apellidos ;
    }
    
    @Override
	public int hashCode()
    {
		return Objects.hash(this.email) ;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj == null)
		{
			return false;
		}
		else if (getClass() != obj.getClass())
		{
			return false;
		}
		
		Profesor other = (Profesor) obj ;
		
		return Objects.equals(this.email, other.email) ;
	}
}
