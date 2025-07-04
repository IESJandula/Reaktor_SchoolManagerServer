package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.dtos.DiaTramoTipoHorarioDto;
import es.iesjandula.reaktor.school_manager_server.dtos.DiaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.TramoDto;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;

import java.util.Set;

@Repository
public interface IDiaTramoTipoHorarioRepository extends JpaRepository<DiaTramoTipoHorario, Long>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.DiaTramoTipoHorarioDto(d.diaDesc, d.tramoDesc, d.horarioMatutino) " +
            "FROM DiaTramoTipoHorario d " +
            "WHERE NOT ((d.dia = 0 AND d.tramo = 0)    OR "  + 
                       "(d.dia = 4 AND d.tramo = 5)    OR "  +
                       "(d.dia = -1 AND d.tramo != -1) OR " +
                       "(d.dia != -1 AND d.tramo = -1)) ")
    Set<DiaTramoTipoHorarioDto> filtroDiaTramoTipoHorarioDisponiblesSeleccionProfesorado();

    @Query("SELECT d " +
            "FROM DiaTramoTipoHorario d " +
            "WHERE d.tramoDesc = :tramoDesc AND d.diaDesc = :diaDesc")
    DiaTramoTipoHorario buscarPorDiaDescTramoDesc(@Param("diaDesc") String diaDesc, @Param("tramoDesc") String tramoDesc);

    @Query("SELECT d " +
            "FROM DiaTramoTipoHorario d " +
            "WHERE d.dia = :dia AND d.tramo = :tramo AND d.horarioMatutino = :horarioMatutino")
    Optional<DiaTramoTipoHorario> findByDiaAndTramoAndHorarioMatutino(int dia, int tramo, Boolean horarioMatutino);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.DiaDto(d.dia, d.diaDesc) FROM DiaTramoTipoHorario d GROUP BY d.dia, d.diaDesc ORDER BY d.dia")
    Optional<List<DiaDto>> obtenerDiasSemana();

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.TramoDto(d.tramo, d.tramoDesc) FROM DiaTramoTipoHorario d GROUP BY d.tramo, d.tramoDesc ORDER BY d.tramo")
    Optional<List<TramoDto>> obtenerTramosHorarios();
}