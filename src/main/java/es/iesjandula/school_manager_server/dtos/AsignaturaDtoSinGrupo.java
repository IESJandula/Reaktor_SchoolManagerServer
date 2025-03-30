package es.iesjandula.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaDtoSinGrupo
{

	private Integer curso;

	private String etapa ;

	private String nombre;
	
	private Integer horas;

}
