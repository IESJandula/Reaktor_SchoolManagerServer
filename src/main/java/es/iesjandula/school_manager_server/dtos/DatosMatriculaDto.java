package es.iesjandula.school_manager_server.dtos;

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

}
