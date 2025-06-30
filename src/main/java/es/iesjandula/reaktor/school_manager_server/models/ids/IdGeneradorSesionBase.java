package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Clase que representa la clave primaria compuesta para la entidad GeneradorSesionBase (o la entidad que la use).
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria está compuesta por las claves primarias de las entidades
 * Impartir ({@link IdImpartir}) y DiasTramosTipoHorario ({@link IdDiasTramosTipoHorario}),
 * identificando de manera única la asignación de una clase impartida en un tramo horario específico.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class IdGeneradorSesionBase implements Serializable
{
    /** Serialización de la clase para persistencia */
    private static final long serialVersionUID = 1L ;

    /** Id de la sesión base */
    private int numeroSesion ;

	/** Asignatura que está siendo impartida por un profesor */
	@ManyToOne
	private Asignatura asignatura;

	/** Profesor que imparte la asignatura */
	@ManyToOne
	private Profesor profesor;

	/** Día de la semana y tramo horario */
	@ManyToOne
	private DiasTramosTipoHorario diasTramosTipoHorario ;
} 