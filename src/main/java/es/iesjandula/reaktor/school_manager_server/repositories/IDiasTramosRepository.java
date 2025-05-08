package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.dtos.DiasTramosTipoHorarioDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.school_manager_server.models.DiasTramosTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDiasTramosTipoHorario;

import java.util.List;

@Repository
public interface IDiasTramosRepository extends JpaRepository<DiasTramosTipoHorario, IdDiasTramosTipoHorario>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.DiasTramosTipoHorarioDto(d.diasDesc, d.idDiasTramosTipoHorario.tramo + 1, d.idDiasTramosTipoHorario.tipoHorario) " +
            "FROM DiasTramosTipoHorario d " +
            "WHERE NOT ((d.idDiasTramosTipoHorario.dia = 0 AND d.idDiasTramosTipoHorario.tramo = 0) OR (d.idDiasTramosTipoHorario.dia = 4 AND d.idDiasTramosTipoHorario.tramo = 5))")
    List<DiasTramosTipoHorarioDto> findByTipoHorario();

    @Query("SELECT d.idDiasTramosTipoHorario.dia " +
            "FROM DiasTramosTipoHorario d " +
            "WHERE d.idDiasTramosTipoHorario.tramo = :tramo AND d.idDiasTramosTipoHorario.tipoHorario = :tipoHorario AND d.diasDesc = :dia")
    Integer encontrarTodoPorTramoAndTipoHorarioAndDiasDesc(@Param("tramo") Integer tramo,
                                                           @Param("tipoHorario") String tipoHorario,
                                                           @Param("dia") String dia);
}
