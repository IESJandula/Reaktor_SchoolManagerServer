package es.iesjandula.reaktor.school_manager_server.models.ids;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.Data; 

@Data   
@Embeddable
public class IdGeneradorInstanciaSolucionInfoProfesor
{
    /** Identificador de la instancia de solución. */
    @ManyToOne
    private GeneradorInstancia generadorInstancia ;

    /** Identificador del profesor. */
    @ManyToOne
    private Profesor profesor;

    /** Tipo de información de la solución. */
    @Column(name = "tipo")
    private String tipo ;
}
