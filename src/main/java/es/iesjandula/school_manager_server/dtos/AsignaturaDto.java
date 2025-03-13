package es.iesjandula.school_manager_server.dtos;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaDto 
{

	private String nombre ;
	
	private Character grupo ;
	
	private String etapa ;
	
	private int curso ;

	private int horas;
	
	private int numeroDeAlumnos ;
	
	private Map<String, Integer> numeroAlumnosEnGrupo ;
	
	private Long bloqueId ;
	
}
