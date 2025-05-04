package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.dtos.DiasTramosTipoHorarioDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDiasTramosTipoHorario;

import java.util.List;

@Repository
public interface IDiasTramosRepository extends JpaRepository<DiasTramosTipoHorario, IdDiasTramosTipoHorario>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.DiasTramosTipoHorarioDto(d.idDiasTramosTipoHorario.dia, d.idDiasTramosTipoHorario.tramo, d.idDiasTramosTipoHorario.tipoHorario) " +
            "FROM DiasTramosTipoHorario d")
    List<DiasTramosTipoHorarioDto> findByTipoHorario();
}
