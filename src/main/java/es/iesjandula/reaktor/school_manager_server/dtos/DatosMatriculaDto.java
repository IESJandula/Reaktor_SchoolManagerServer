package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatosMatriculaDto 
{
	
	private String nombre;
	
	private String apellidos;
	
	private String asignatura;
	
	private String estadoMatricula;

	/**
	 * Indica si la asignatura es ad-hoc (creada a medida por dirección). El frontend muestra el aspa de borrado
	 * solo en las asignaturas ad-hoc.
	 */
	private boolean esAdHoc;

}
