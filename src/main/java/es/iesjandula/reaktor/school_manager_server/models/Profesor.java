package es.iesjandula.reaktor.school_manager_server.models;

import java.util.List;
import java.util.Objects;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdProfesor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Entidad - Profesor
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase representa a un profesor en el sistema. Incluye la información del profesor, como su correo electrónico,
 * nombre, apellidos y su relación con el departamento al que pertenece. Además, mantiene las relaciones con las asignaturas
 * que imparte y las reducciones de horas que tiene.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Profesor")
@IdClass(IdProfesor.class)
@Slf4j
public class Profesor 
{
	/**
	 * Curso académico al que pertenece el profesor. Forma parte de la clave primaria compuesta
	 * {@code (cursoAcademico, email)}. Se resuelve internamente (curso académico seleccionado) al persistir; NO viaja
	 * en el CSV ni en los DTO de Firebase.
	 */
	@Id
	@Column(length = 9)
	private String cursoAcademico;

	/**
	 * Correo electrónico del profesor. Junto con {@code cursoAcademico} forma la clave primaria de la entidad.
	 */
	@Id
	@Column(length = 100)
	private String email;
	
	/**
	 * Nombre del profesor.
	 */
	@Column(length = 50, nullable = false)
	private String nombre;
	
	/**
	 * Apellidos del profesor.
	 */
	@Column(length = 100, nullable = false)
	private String apellidos;
	
	/**
	 * Departamento al que pertenece el profesor. Relación de muchos a uno con la entidad {@link Departamento}.
	 */
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "departamento_curso_academico", referencedColumnName = "cursoAcademico"),
		@JoinColumn(name = "departamento_nombre", referencedColumnName = "nombre")
	})
	private Departamento departamento;
	
	/**
	 * Lista de asignaturas que el profesor imparte. Relación de uno a muchos con la entidad {@link Impartir}.
	 */
	@OneToMany(mappedBy = "profesor")
	private List<Impartir> impartir;
	
	/**
	 * Lista de reducciones de horas que el profesor tiene. Relación de uno a muchos con la entidad {@link ProfesorReduccion}.
	 */
	@OneToMany(mappedBy = "profesor")
	private List<ProfesorReduccion> profesorReducciones;

	/**
	 * Observaciones adicionales del profesor. Relación de uno a uno con la entidad {@link ObservacionesAdicionales}.
	 */
	@OneToOne(mappedBy = "profesor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private ObservacionesAdicionales observacionesAdicionales;

	/**
	 * Preferencias horarias del profesor. Relación de uno a muchos con la entidad {@link PreferenciasHorariasProfesor}.
	 */
	@OneToMany(mappedBy = "profesor")
	private List<PreferenciasHorariasProfesor> preferenciasHorariasProfesor;

	@Override
    public String toString()
    {
    	return this.nombre + " " + this.apellidos ;
    }
    
    @Override
	public int hashCode()
    {
		return Objects.hash(this.cursoAcademico, this.email) ;
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
		
		return Objects.equals(this.cursoAcademico, other.cursoAcademico) && Objects.equals(this.email, other.email) ;
	}
}
