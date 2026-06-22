package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.Objects;

/**
 * DTO - EspacioDto
 * -----------------------------------------------------------------------------------------------------------------
 * Clase abstracta que representa los datos comunes de un espacio (curso académico y nombre). Es la base de los
 * DTOs concretos de espacio sin docencia, fijo y desdoble.
 * -----------------------------------------------------------------------------------------------------------------
 */
public abstract class EspacioDto
{
	/** Curso académico del espacio. */
	private String cursoAcademico;

	/** Nombre del espacio. */
	private String nombre;

	/**
	 * Constructor por defecto.
	 */
	public EspacioDto()
	{
	}

	/**
	 * Constructor con todos los campos comunes.
	 *
	 * @param cursoAcademico - El curso académico del espacio.
	 * @param nombre         - El nombre del espacio.
	 */
	public EspacioDto(String cursoAcademico, String nombre)
	{
		this.cursoAcademico = cursoAcademico;
		this.nombre = nombre;
	}

	public String getCursoAcademico()
	{
		return this.cursoAcademico;
	}

	public String getNombre()
	{
		return this.nombre;
	}

	public void setCursoAcademico(String cursoAcademico)
	{
		this.cursoAcademico = cursoAcademico;
	}

	public void setNombre(String nombre)
	{
		this.nombre = nombre;
	}

	@Override
	public String toString()
	{
		return "EspacioDto [cursoAcademico=" + this.cursoAcademico + ", nombre=" + this.nombre + "]";
	}

	@Override
	public boolean equals(Object object)
	{
		if (this == object)
		{
			return true;
		}
		else if (object == null)
		{
			return false;
		}
		else if (!(object instanceof EspacioDto))
		{
			return false;
		}

		final EspacioDto other = (EspacioDto) object;

		return this.cursoAcademico.equals(other.cursoAcademico) && this.nombre.equals(other.nombre);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.cursoAcademico, this.nombre);
	}
}
