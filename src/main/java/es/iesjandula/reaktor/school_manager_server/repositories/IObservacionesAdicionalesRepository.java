package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdObservacionesAdicionales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IObservacionesAdicionalesRepository extends JpaRepository<ObservacionesAdicionales, IdObservacionesAdicionales>
{
  @Query("SELECT oa FROM ObservacionesAdicionales oa WHERE oa.idObservacionesAdicionales.profesor.email = :email")
  Optional<ObservacionesAdicionales> buscarPorEmail(String email) ;
}
