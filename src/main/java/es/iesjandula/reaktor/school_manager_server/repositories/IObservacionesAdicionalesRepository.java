package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.models.ObservacionesAdicionales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IObservacionesAdicionalesRepository extends JpaRepository<ObservacionesAdicionales, String>
{
  @Query("SELECT oa FROM ObservacionesAdicionales oa WHERE oa.profesor.email = :email")
  Optional<ObservacionesAdicionales> buscarPorEmail(String email) ;
}
