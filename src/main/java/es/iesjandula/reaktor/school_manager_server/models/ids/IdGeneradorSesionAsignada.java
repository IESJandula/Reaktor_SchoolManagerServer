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
 * Clase que representa la clave primaria compuesta para la entidad GeneradorSesionAsignada (o la entidad que la use).
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria está compuesta por las claves primarias de las entidades
 * Impartir ({@link IdImpartir}), DiaTramoTipoHorario ({@link Long}) y un identificador
 * de generación ({@code idGeneracion}), identificando de manera única la asignación
 * de una clase impartida en un tramo horario específico dentro de una generación de horario concreta.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class IdGeneradorSesionAsignada implements Serializable
{
    /** Serialización de la clase para persistencia */
    private static final long serialVersionUID = 2L ; // Incrementado por el cambio
    
    /** Identificador de la instancia del generador a la que pertenece esta asignación */
    private Integer idGeneradorInstancia ;

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