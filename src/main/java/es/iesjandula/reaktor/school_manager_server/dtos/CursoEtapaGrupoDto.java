package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class 
CursoEtapaGrupoDto 
{
	/** Curso */
	private int curso ;
	
	/** Etapa */
	private String etapa ;
	
	/** Grupo */
	private String grupo;

	/** Booleano a true si es horario matutino */
	private Boolean horarioMatutino ;

	/** Booleano a true si es ESO o Bachillerato */
	private Boolean esBachillerato ;
}
