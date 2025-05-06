package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdObservacionesAdicionales;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IObservacionesAdicionalesRepository extends JpaRepository<ObservacionesAdicionales, IdObservacionesAdicionales>
{
}
