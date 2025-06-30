package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDiasTramosTipoHorario;

import org.springframework.stereotype.Repository;

@Repository
public interface IDiasTramosTipoHorarioRepository extends JpaRepository<DiasTramosTipoHorario, IdDiasTramosTipoHorario>
{

}