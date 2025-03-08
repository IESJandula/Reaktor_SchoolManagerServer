package es.iesjandula.school_manager_server.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Profesor 
{
	/**
	 * Correo electrónico del profesor, que sirve como la clave primaria de la entidad.
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
	@JoinColumn(name = "departamento_nombre")
	private Departamento departamento;
	
	/**
	 * Lista de asignaturas que el profesor imparte. Relación de uno a muchos con la entidad {@link Impartir}.
	 */
	@OneToMany(mappedBy = "profesor")
	private List<Impartir> impartires;
	
	/**
	 * Lista de reducciones de horas que el profesor tiene. Relación de uno a muchos con la entidad {@link ProfesorReduccion}.
	 */
	@OneToMany(mappedBy = "profesor")
	private List<ProfesorReduccion> profesorReducciones;
}
