package es.iesjandula.reaktor.school_manager_server.models.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IdReduccion
{

    /**
     * Nombre de la reducción. Este es un identificador único de la reducción.
     */
    @Column(length = 100)
    private String nombre;

    /**
     * Número de horas que se reducen para el profesor. Este valor es obligatorio.
     */
    private Integer horas;
}
