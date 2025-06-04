package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdObservacionesAdicionales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IObservacionesAdicionalesRepository extends JpaRepository<ObservacionesAdicionales, IdObservacionesAdicionales>
{
  Optional<ObservacionesAdicionales> findByIdObservacionesAdicionales_Profesor(Profesor profesor);
}
