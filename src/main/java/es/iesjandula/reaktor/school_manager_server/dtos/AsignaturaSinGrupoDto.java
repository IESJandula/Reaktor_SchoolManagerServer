package es.iesjandula.reaktor.school_manager_server.dtos;

import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaSinGrupoDto
{
    private Integer horas;

    private boolean esoBachillerato;

    private boolean sinDocencia;

    private boolean desdoble;

    private Long idBloque;
}
