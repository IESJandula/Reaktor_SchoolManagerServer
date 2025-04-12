package es.iesjandula.reaktor.school_manager_server.generator.models;

import java.util.List;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Data
public class Asignatura
{
	/** Nombre de la asignatura */
	private String nombre ;
	
	/** Curso-Etapa-Grupo, por ejemplo, 1ESOA */
	private String cursoEtapaGrupo ;
	
    /** Si la sesion se imparte en ESO o BACH */
    private boolean esoBachillerato ;
    
    /** Lista de asignaturas que pertenecen al mismo bloque */
    private List<Asignatura> bloqueOptativas ;
    
    /** True si esta asignatura es una optativa */
    private boolean optativa ;
    
	/** Horas a la semana */
	private int horasSemana ;

    @Override
    public String toString()
    {
    	return this.nombre ;
    }

    /**
     * @param nombre nombre de la asignatura
     * @param cursoEtapaGrupo curso etapa y grupo
     * @param esoBachillerato true si es ESO o BACH
	 * @param horasSemana horas a la semana
     */
	public Asignatura(String nombre, String cursoEtapaGrupo, boolean esoBachillerato, int horasSemana)
	{
		this.nombre 		 = nombre ;
		this.cursoEtapaGrupo = cursoEtapaGrupo ;
		this.esoBachillerato = esoBachillerato ;
		this.optativa   	 = false ;
		this.horasSemana	 = horasSemana ;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true ;
		}
		else if (obj == null)
		{
			return false ;
		}
		else if (getClass() != obj.getClass())
		{
			return false ;
		}
		
		Asignatura other = (Asignatura) obj ;
		
		return Objects.equals(this.nombre, other.nombre)				   &&
			   Objects.equals(this.cursoEtapaGrupo, other.cursoEtapaGrupo) ;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.nombre, this.cursoEtapaGrupo, this.esoBachillerato, this.horasSemana) ;
	}
	
	/**
	 * @param bloqueOptativas bloque de optativas
	 */
	public void setBloqueOptativas(List<Asignatura> bloqueOptativas)
	{
		this.bloqueOptativas = bloqueOptativas ;
		
		this.optativa 		 = this.bloqueOptativas != null && this.bloqueOptativas.size() > 0 ;
	}
}
