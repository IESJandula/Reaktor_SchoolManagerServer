package es.iesjandula.reaktor.school_manager_server.models;

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
    @Id
    @Column(name = "profesor_email")
    private String profesorEmail;

    @OneToOne
    @JoinColumn(name = "profesor_email", referencedColumnName = "email")
    private Profesor profesor;

    @Column
    private Boolean conciliacion;

    @Column
    private Boolean sinClasePrimeraHora;

    @Column(length = 1000)
    private String otrasObservaciones;
}
