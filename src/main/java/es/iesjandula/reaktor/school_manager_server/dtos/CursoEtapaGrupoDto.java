package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CursoEtapaGrupoDto
{
	/** Curso académico (presente en catálogo por año; null en respuestas de grupos docentes globales) */
	private String cursoAcademico;

	/** Curso */
	private int curso;

	/** Etapa */
	private String etapa;

	/** Grupo (opcional en creación desde catálogo curso/etapa; se usa valor por defecto en servidor) */
	private String grupo;

	/** Booleano a true si es horario matutino */
	private Boolean horarioMatutino;

	/** Booleano a true si pertenece a ESO o Bachillerato */
	private Boolean esBachillerato;

	/**
	 * Constructor para consultas JPQL del catálogo curso/etapa por curso académico.
	 */
	public CursoEtapaGrupoDto(String cursoAcademico, int curso, String etapa, String grupo, Boolean horarioMatutino, Boolean esBachillerato)
	{
		this.cursoAcademico = cursoAcademico;
		this.curso = curso;
		this.etapa = etapa;
		this.grupo = grupo;
		this.horarioMatutino = horarioMatutino;
		this.esBachillerato = esBachillerato;
	}

	/**
	 * Constructor de compatibilidad para grupos docentes globales.
	 */
	public CursoEtapaGrupoDto(int curso, String etapa, String grupo, Boolean horarioMatutino, Boolean esBachillerato)
	{
		this(null, curso, etapa, grupo, horarioMatutino, esBachillerato);
	}
}
