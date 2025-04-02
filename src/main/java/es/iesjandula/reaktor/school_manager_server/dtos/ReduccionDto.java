package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReduccionDto 
{
	
	private String nombre;
	
	private int horas;
	
	private boolean decideDireccion;

}
