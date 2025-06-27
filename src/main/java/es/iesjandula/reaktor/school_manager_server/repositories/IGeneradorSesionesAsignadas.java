package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionesAsignadas;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionesAsignadas;

@Repository
public interface IGeneradorSesionesAsignadas extends JpaRepository<GeneradorSesionesAsignadas, IdGeneradorSesionesAsignadas>
{

}
