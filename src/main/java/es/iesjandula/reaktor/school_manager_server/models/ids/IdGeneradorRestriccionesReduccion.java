package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Clase que representa la clave primaria compuesta para la entidad GeneradorRestriccionesReduccion (o la entidad que la use).
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria está compuesta por las claves primarias de las entidades
 * GeneradorInstancia ({@link Integer}), Asignatura ({@link Asignatura}), Profesor ({@link Profesor}) y
 * DiaTramoTipoHorario ({@link DiaTramoTipoHorario}), identificando de manera única la asignación
 * de una reducción aplicada en un tramo horario específico dentro de una instancia de generador de horario.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class IdGeneradorRestriccionesReduccion implements Serializable
{
    /** Serialización de la clase para persistencia */
    private static final long serialVersionUID = 1L ;

    /** Id de la restricción de reducción */
    private int numeroRestriccion ;

	/** Reducción que está siendo aplicada al profesor */
	@ManyToOne
	private Reduccion reduccion;

	/** Profesor que aplica la reducción */
	@ManyToOne
	private Profesor profesor;

	/** Día de la semana y tramo horario */
	@ManyToOne
	private DiaTramoTipoHorario diaTramoTipoHorario ;
} 