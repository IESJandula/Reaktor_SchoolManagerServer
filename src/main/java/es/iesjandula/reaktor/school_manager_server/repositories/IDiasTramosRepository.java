package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDiasTramosTipoHorario;

@Repository
public interface IDiasTramosRepository extends JpaRepository<DiasTramosTipoHorario, IdDiasTramosTipoHorario>
{
    
}
