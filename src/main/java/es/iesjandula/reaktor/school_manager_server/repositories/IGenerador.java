package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.school_manager_server.models.Generador;

@Repository
public interface IGenerador extends JpaRepository<Generador, Long>
{
    @Query("SELECT g " + 
           "FROM Generador g " + 
           "WHERE g.fechaInicio IS NOT NULL AND g.fechaFin IS NULL " + 
           "ORDER BY g.fechaInicio DESC")
    public Generador buscarGeneradorActivo();
}
