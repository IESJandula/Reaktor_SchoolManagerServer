package es.iesjandula.reaktor.school_manager_server.dtos;

/**
 * DTO - EspacioFijoDto
 * -----------------------------------------------------------------------------------------------------------------
 * Objeto de transferencia de datos que representa un espacio fijo y el grupo (curso, etapa y grupo) al que está
 * asociado.
 * -----------------------------------------------------------------------------------------------------------------
 */
public class EspacioFijoDto extends EspacioDto
{
	/** Curso del grupo asociado. */
	private Integer curso;

	/** Etapa del grupo asociado. */
	private String etapa;

	/** Grupo asociado. */
	private String grupo;

	/**
	 * Constructor por defecto.
	 */
	public EspacioFijoDto()
	{
		super();
	}

	/**
	 * Constructor con el curso académico y el nombre.
	 *
	 * @param cursoAcademico - El curso académico del espacio.
	 * @param nombre         - El nombre del espacio.
	 */
	public EspacioFijoDto(String cursoAcademico, String nombre)
	{
		super(cursoAcademico, nombre);
	}

	/**
	 * Constructor con todos los campos.
	 *
	 * @param cursoAcademico - El curso académico del espacio.
	 * @param nombre         - El nombre del espacio.
	 * @param curso          - El curso del grupo.
	 * @param etapa          - La etapa del grupo.
	 * @param grupo          - El grupo.
	 */
	public EspacioFijoDto(String cursoAcademico, String nombre, Integer curso, String etapa, String grupo)
	{
		super(cursoAcademico, nombre);
		this.curso = curso;
		this.etapa = etapa;
		this.grupo = grupo;
	}

	public Integer getCurso()
	{
		return this.curso;
	}

	public void setCurso(Integer curso)
	{
		this.curso = curso;
	}

	public String getEtapa()
	{
		return this.etapa;
	}

	public void setEtapa(String etapa)
	{
		this.etapa = etapa;
	}

	public String getGrupo()
	{
		return this.grupo;
	}

	public void setGrupo(String grupo)
	{
		this.grupo = grupo;
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public boolean equals(Object object)
	{
		return (this == object) || (super.equals(object) && (object instanceof EspacioFijoDto));
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
