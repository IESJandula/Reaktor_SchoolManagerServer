package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;

@Repository
public interface IGeneradorSesionAsignadaRepository extends JpaRepository<GeneradorSesionAsignada, IdGeneradorSesionAsignada>
{

}
