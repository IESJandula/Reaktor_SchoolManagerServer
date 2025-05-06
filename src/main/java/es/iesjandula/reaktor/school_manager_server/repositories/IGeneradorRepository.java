package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import es.iesjandula.reaktor.school_manager_server.models.Generador;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface IGeneradorRepository extends JpaRepository<Generador, Integer>
{
    /**
     * Método que obtiene el último generador creado
     * @return - Generador
     */
    @Query("SELECT g " + 
           "FROM Generador g " + 
           "WHERE g.estado = :estado")
    Optional<Generador> buscarGeneradorPorEstado(@Param("estado") String estado) ;
}
