package es.iesjandula.reaktor.school_manager_server.models;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionesAsignadas;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "GeneradorSesionesAsignadas")
public class GeneradorSesionesAsignadas
{
    @EmbeddedId
    private IdGeneradorSesionesAsignadas idGeneradorSesionesAsignadas;
}
