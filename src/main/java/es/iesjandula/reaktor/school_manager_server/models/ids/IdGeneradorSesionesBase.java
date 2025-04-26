package es.iesjandula.reaktor.school_manager_server.models.ids;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Clase que representa la clave primaria compuesta para la entidad GeneradorSesionesBase (o la entidad que la use).
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clave primaria está compuesta por las claves primarias de las entidades
 * Impartir ({@link IdImpartir}) y DiasTramosTipoHorario ({@link IdDiasTramosTipoHorario}),
 * identificando de manera única la asignación de una clase impartida en un tramo horario específico.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class IdGeneradorSesionesBase implements Serializable
{
    /** Serialización de la clase para persistencia */
    private static final long serialVersionUID = 1L ;

    /** Parte de la clave primaria compuesta que referencia a la entidad Impartir */
    private IdImpartir idImpartir ;

    /** Parte de la clave primaria compuesta que referencia a la entidad DiasTramosTipoHorario */
    private IdDiasTramosTipoHorario diasTramosTipoHorario ;
} 