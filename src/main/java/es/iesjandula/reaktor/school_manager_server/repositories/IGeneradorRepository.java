package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import org.springframework.stereotype.Repository;

@Repository
public interface IGeneradorRepository extends JpaRepository<Generador, Integer>
{

}
