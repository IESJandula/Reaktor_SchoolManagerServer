package es.iesjandula.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursoEtapaGrupoDto 
{
	private int curso;
	
	private String etapa;
	
	private Character grupo;

}
