package es.iesjandula.reaktor.school_manager_server.models.ids;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link ProfesorReduccion}.
 * -----------------------------------------------------------------------------------------------------------------
 * La clase {@link IdProfesorReduccion} define una clave primaria compuesta que se utiliza en la entidad {@link ProfesorReduccion}.
 * Esta clave está compuesta por dos atributos: {@link Profesor} y {@link Reduccion}, que juntos identifican de manera única 
 * la asignación de una reducción horaria a un profesor.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdProfesorReduccion 
{
    /**
     * El profesor al que se le asigna una reducción.
     * Representa una relación con la entidad {@link Profesor}.
     */
    @ManyToOne
    private Profesor profesor;
    
    /**
     * La reducción horaria asignada al profesor.
     * Representa una relación con la entidad {@link Reduccion}.
     */
    @ManyToOne
    private Reduccion reduccion;
}
