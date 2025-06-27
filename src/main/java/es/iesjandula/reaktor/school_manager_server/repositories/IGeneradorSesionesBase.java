package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionesBase;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.SesionesBaseDto;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionesBase;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdImpartir;

@Repository
public interface IGeneradorSesionesBase extends JpaRepository<GeneradorSesionesBase, IdGeneradorSesionesBase>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.SesionesBaseDto(gsb.idGeneradorSesionesBase.numeroSesion, gsb.idGeneradorSesionesBase.diasTramosTipoHorario.dia, gsb.idGeneradorSesionesBase.diasTramosTipoHorario.tramo) " +
           "FROM GeneradorSesionesBase gsb " +
           "WHERE gsb.idGeneradorSesionesBase.idImpartir = :idImpartir")
    Optional<List<SesionesBaseDto>> buscarSesionesBasePorIdImpartir(@Param("idImpartir") IdImpartir idImpartir);

    @Query("SELECT gsb " +
           "FROM GeneradorSesionesBase gsb " +
           "WHERE gsb.idGeneradorSesionesBase.numeroSesion = :numeroSesion " +
           "AND gsb.idGeneradorSesionesBase.idImpartir = :idImpartir")
    Optional<GeneradorSesionesBase> buscarSesionBasePorNumeroSesionIdImpartir(@Param("numeroSesion") int numeroSesion, @Param("idImpartir") IdImpartir idImpartir);
}
