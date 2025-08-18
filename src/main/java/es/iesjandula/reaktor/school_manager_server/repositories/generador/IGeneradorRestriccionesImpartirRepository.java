package es.iesjandula.reaktor.school_manager_server.repositories.generador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorRestriccionesImpartir;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorRestriccionesImpartir;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;

@Repository
public interface IGeneradorRestriccionesImpartirRepository extends JpaRepository<GeneradorRestriccionesImpartir, IdGeneradorRestriccionesImpartir>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto(gri.idGeneradorRestriccionesImpartir.numeroRestriccion, gri.idGeneradorRestriccionesImpartir.diaTramoTipoHorario.diaDesc, gri.idGeneradorRestriccionesImpartir.diaTramoTipoHorario.tramoDesc) " +
           "FROM GeneradorRestriccionesImpartir gri " +
           "WHERE gri.idGeneradorRestriccionesImpartir.impartir = :impartir")
    Optional<List<GeneradorRestriccionBaseDto>> buscarRestriccionesPorImpartirDto(@Param("impartir") Impartir impartir);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesImpartir gri " +
           "WHERE gri.idGeneradorRestriccionesImpartir.impartir = :impartir")
    Optional<List<GeneradorRestriccionesImpartir>> buscarRestriccionesPorImpartir(@Param("impartir") Impartir impartir);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesImpartir gri " +
           "WHERE gri.idGeneradorRestriccionesImpartir.numeroRestriccion = :numeroRestriccion " +
             "AND gri.idGeneradorRestriccionesImpartir.impartir = :impartir")
    Optional<GeneradorRestriccionesImpartir> buscarRestriccionesPorNumeroRestriccionImpartir(@Param("numeroRestriccion") int numeroRestriccion, @Param("impartir") Impartir impartir);
}
