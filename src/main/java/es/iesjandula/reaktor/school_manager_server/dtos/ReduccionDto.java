package es.iesjandula.reaktor.school_manager_server.dtos;

import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import lombok.Data;

@Data
public class ReduccionDto 
{
	/** Nombre de la reducción */
	private String nombre;
	
	/** Horas de la reducción */
	private int horas;
	
	/** Indica si la reducción ha sido decidida por la dirección */
	private boolean decideDireccion;

	/** Curso de la reducción (opcional) */
	private Integer curso ;

	/** Etapa de la reducción (opcional) */
	private String etapa ;

	/** Grupo de la reducción (opcional) */
	private String grupo ;

	/**
	 * Constructor de la clase ReduccionDto
	 * @param nombre Nombre de la reducción
	 * @param horas Horas de la reducción
	 * @param decideDireccion Indica si la reducción ha sido decidida por la dirección
	 * @param cursoEtapaGrupo CursoEtapaGrupo de la reducción
	 */
	public ReduccionDto(String nombre, int horas, boolean decideDireccion, CursoEtapaGrupo cursoEtapaGrupo)
	{
		this.nombre 		 = nombre;
		this.horas 			 = horas;
		this.decideDireccion = decideDireccion;

		// Por defecto, son nulas
		this.curso = null;
		this.etapa = null;
		this.grupo = null;
		
		// Manejar el caso donde cursoEtapaGrupo puede ser null
		if (cursoEtapaGrupo != null && cursoEtapaGrupo.getIdCursoEtapaGrupo() != null)
		{
			this.curso = cursoEtapaGrupo.getIdCursoEtapaGrupo().getCurso() ;
			this.etapa = cursoEtapaGrupo.getIdCursoEtapaGrupo().getEtapa() ;
			this.grupo = cursoEtapaGrupo.getIdCursoEtapaGrupo().getGrupo() ;
		}
	}
}
