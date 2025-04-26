package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Clase que representa la clave primaria compuesta para la entidad GeneradorSesionesAsignadas (o la entidad que la use).
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria está compuesta por las claves primarias de las entidades
 * Impartir ({@link IdImpartir}), DiasTramosTipoHorario ({@link IdDiasTramosTipoHorario}) y un identificador
 * de generación ({@code idGeneracion}), identificando de manera única la asignación
 * de una clase impartida en un tramo horario específico dentro de una generación de horario concreta.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class IdGeneradorSesionesAsignadas implements Serializable
{
    /** Serialización de la clase para persistencia */
    private static final long serialVersionUID = 2L ; // Incrementado por el cambio
    
    /** Identificador de la generación del horario a la que pertenece esta asignación */
    private Integer idGeneracion ;

    /** Parte de la clave primaria compuesta que referencia a la entidad Impartir */
    private IdImpartir impartir ;

    /** Parte de la clave primaria compuesta que referencia a la entidad DiasTramosTipoHorario */
    private IdDiasTramosTipoHorario diasTramosTipoHorario ;

} 