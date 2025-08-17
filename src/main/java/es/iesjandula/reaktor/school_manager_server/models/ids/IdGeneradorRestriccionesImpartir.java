package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Clase que representa la clave primaria compuesta para la entidad GeneradorRestriccionesImpartir (o la entidad que la use).
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria está compuesta por las claves primarias de las entidades
 * GeneradorInstancia ({@link Integer}), Asignatura ({@link Asignatura}), Profesor ({@link Profesor}) y
 * DiaTramoTipoHorario ({@link DiaTramoTipoHorario}), identificando de manera única la asignación
 * de una clase impartida en un tramo horario específico dentro de una instancia de generador de horario.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class IdGeneradorRestriccionesImpartir implements Serializable
{
    /** Serialización de la clase para persistencia */
    private static final long serialVersionUID = 1L ;

    /** Id de la restricción de tipo de horario */
    private int numeroRestriccion ;

	/** Asignatura que está siendo impartida por un profesor */
	@ManyToOne
	private Asignatura asignatura;

	/** Profesor que imparte la asignatura */
	@ManyToOne
	private Profesor profesor;

	/** Día de la semana y tramo horario */
	@ManyToOne
	private DiaTramoTipoHorario diaTramoTipoHorario ;
} 