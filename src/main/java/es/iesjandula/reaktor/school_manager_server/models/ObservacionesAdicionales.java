package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdObservacionesAdicionales;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ObservacionesAdicionales
{
    @EmbeddedId
    private IdObservacionesAdicionales idObservacionesAdicionales;

    @Column
    private Boolean conciliacion;

    @Column
    private Boolean trabajarPrimeraHora;

    @Column(length = 1000)
    private String otrasObservaciones;
}
