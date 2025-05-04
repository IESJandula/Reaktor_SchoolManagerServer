package es.iesjandula.reaktor.school_manager_server.models.ids;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class IdObservacionesAdicionales implements Serializable
{
    /**
     * Atributo único de serialización para la clase.
     */
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "profesor_email", referencedColumnName = "email")
    private Profesor profesor;

}
