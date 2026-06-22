package es.iesjandula.reaktor.school_manager_server.dtos;

/**
 * DTO - EspacioDesdobleDto
 * -----------------------------------------------------------------------------------------------------------------
 * Objeto de transferencia de datos que representa un espacio desdoble.
 * -----------------------------------------------------------------------------------------------------------------
 */
public class EspacioDesdobleDto extends EspacioDto
{
	/**
	 * Constructor con todos los campos.
	 *
	 * @param cursoAcademico - El curso académico del espacio.
	 * @param nombre         - El nombre del espacio.
	 */
	public EspacioDesdobleDto(String cursoAcademico, String nombre)
	{
		super(cursoAcademico, nombre);
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public boolean equals(Object object)
	{
		return (this == object) || (super.equals(object) && (object instanceof EspacioDesdobleDto));
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
