package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.Objects;

/**
 * DTO - EspacioDisponibleDto
 * -----------------------------------------------------------------------------------------------------------------
 * Objeto de transferencia de datos que representa un espacio del catálogo del instituto (creado en la ventana de
 * configuración básica) que todavía NO está asignado como aula de referencia a ningún curso, etapa y grupo.
 * <p>Se utiliza en el paso de creación de grupos para listar los espacios que pueden asignarse a un grupo.</p>
 * -----------------------------------------------------------------------------------------------------------------
 */
public class EspacioDisponibleDto
{
	/** Curso académico del espacio. */
	private String cursoAcademico;

	/** Nombre del espacio. */
	private String nombre;

	/**
	 * Constructor por defecto.
	 */
	public EspacioDisponibleDto()
	{
	}

	/**
	 * Constructor con todos los campos.
	 *
	 * @param cursoAcademico - El curso académico del espacio.
	 * @param nombre         - El nombre del espacio.
	 */
	public EspacioDisponibleDto(String cursoAcademico, String nombre)
	{
		this.cursoAcademico = cursoAcademico;
		this.nombre = nombre;
	}

	public String getCursoAcademico()
	{
		return this.cursoAcademico;
	}

	public void setCursoAcademico(String cursoAcademico)
	{
		this.cursoAcademico = cursoAcademico;
	}

	public String getNombre()
	{
		return this.nombre;
	}

	public void setNombre(String nombre)
	{
		this.nombre = nombre;
	}

	@Override
	public String toString()
	{
		return "EspacioDisponibleDto [cursoAcademico=" + this.cursoAcademico + ", nombre=" + this.nombre + "]";
	}

	@Override
	public boolean equals(Object object)
	{
		if (this == object)
		{
			return true;
		}
		else if (!(object instanceof EspacioDisponibleDto))
		{
			return false;
		}

		final EspacioDisponibleDto other = (EspacioDisponibleDto) object;

		return Objects.equals(this.cursoAcademico, other.cursoAcademico) && Objects.equals(this.nombre, other.nombre);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.cursoAcademico, this.nombre);
	}
}
