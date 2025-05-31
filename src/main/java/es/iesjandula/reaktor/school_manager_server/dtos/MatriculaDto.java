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
	
	private String grupo;
	
	private String nombreAsignatura;

	private int horas;

	private boolean esoBachillerato;

	private Long bloque;

}
