package es.iesjandula.reaktor.school_manager_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaInfoDto {
    private String nombre;
    private int curso;
    private String etapa;
    private Character grupo;
    private String departamentoPropietario;
    private String departamentoDonante;
    private Integer horas;
}
