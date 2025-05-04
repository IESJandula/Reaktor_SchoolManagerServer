package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdPreferenciasHorariasProfesor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PreferenciasHorariasProfesor
{
    @EmbeddedId
    private IdPreferenciasHorariasProfesor idPreferenciasHorariasProfesor;
}
