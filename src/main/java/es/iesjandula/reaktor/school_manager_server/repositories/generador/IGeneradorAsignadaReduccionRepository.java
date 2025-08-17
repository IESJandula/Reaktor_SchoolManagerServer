package es.iesjandula.reaktor.school_manager_server.repositories.generador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorAsignadaReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorAsignadaReduccion;

@Repository
public interface IGeneradorAsignadaReduccionRepository extends JpaRepository<GeneradorAsignadaReduccion, IdGeneradorAsignadaReduccion>
{

}
