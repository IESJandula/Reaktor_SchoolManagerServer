package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una fila del fichero {@code alumnos_cursoEtapaGrupo.csv} usado en "Creación de grupos" para
 * cargar alumnos por fichero: nombre y apellidos del alumno y el curso, etapa y grupo al que debe asignarse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoCursoEtapaGrupoDto
{
    /** Nombre del alumno. */
    private String nombre;

    /** Apellidos del alumno. */
    private String apellidos;

    /** Curso (p. ej. 1). */
    private Integer curso;

    /** Etapa (p. ej. "ESO"). */
    private String etapa;

    /** Grupo (p. ej. "A"). */
    private String grupo;
}
