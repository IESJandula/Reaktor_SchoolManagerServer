package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;

import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Clase que representa la clave primaria compuesta para la entidad GeneradorAsignadaReduccion (o la entidad que la use).
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria está compuesta por las claves primarias de las entidades
 * GeneradorInstancia ({@link Integer}), Reduccion ({@link Reduccion}), Profesor ({@link Profesor}) y
 * DiaTramoTipoHorario ({@link DiaTramoTipoHorario}), identificando de manera única la asignación
 * de una reducción aplicada en un tramo horario específico dentro de una instancia de generador de horario.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class IdGeneradorAsignadaReduccion implements Serializable
{
    /** Serialización de la clase para persistencia */
    private static final long serialVersionUID = 2L ; // Incrementado por el cambio
    
    /** Identificador de la instancia del generador a la que pertenece esta asignación */
    private Integer idGeneradorInstancia ;

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