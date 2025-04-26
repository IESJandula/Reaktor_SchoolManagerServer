package es.iesjandula.reaktor.school_manager_server.models;

import java.util.List;

import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
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
@Slf4j
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
	private List<Impartir> impartir;
	
	/**
	 * Lista de reducciones de horas que el profesor tiene. Relación de uno a muchos con la entidad {@link ProfesorReduccion}.
	 */
	@OneToMany(mappedBy = "profesor")
	private List<ProfesorReduccion> profesorReducciones;

	/**
	 * Indica la conciliación del profesor que puede ser:
	 * - SIN_CONCILIACION
	 * - ENTRAR_DESPUES_SEGUNDA_HORA
	 * - SALIR_ANTES_QUINTA_HORA
	 */
	@Column(length = 100)
	private String conciliacion ;

	/**
	 * Establece la conciliación del profesor.
	 * @param conciliacion - La conciliación del profesor.
	 * @throws SchoolManagerServerException - Si la conciliación no es válida.
	 */
	public void setConciliacion(String conciliacion) throws SchoolManagerServerException
	{
		if (conciliacion != null)
		{
			boolean esValida = conciliacion.equals(Constants.CONCILIACION_SIN_CONCILIACION) 		   ||
							   conciliacion.equals(Constants.CONCILIACION_ENTRAR_DESPUES_SEGUNDA_HORA) ||
							   conciliacion.equals(Constants.CONCILIACION_SALIR_ANTES_QUINTA_HORA) ;
			if (esValida)
			{
				this.conciliacion = conciliacion;
			}
			else
			{
				String mensajeError = "La conciliación no es válida.";

				log.error(mensajeError) ;
				throw new SchoolManagerServerException(Constants.ERROR_CONCILIACION_NO_VALIDA, mensajeError);
			}
		}
	}
}
