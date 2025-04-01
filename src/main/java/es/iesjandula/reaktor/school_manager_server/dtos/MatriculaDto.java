package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatriculaDto 
{
	
	private String nombreAlumno;

	private String apellidosAlumno;
	
	private int curso;
	
	private String etapa;
	
	private Character grupo;
	
	private String nombreAsignatura;

}
