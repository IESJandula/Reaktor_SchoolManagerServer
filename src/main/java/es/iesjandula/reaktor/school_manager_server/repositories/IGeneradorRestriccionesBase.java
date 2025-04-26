package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionesBase;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionesBase;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdImpartir;

@Repository
public interface IGeneradorRestriccionesBase extends JpaRepository<GeneradorSesionesBase, IdGeneradorSesionesBase>
{
    @Query("SELECT gsb " +
           "FROM GeneradorSesionesBase gsb " +
           "WHERE gsb.idGeneradorSesionesBase.idImpartir = :idImpartir")
    Optional<List<GeneradorSesionesBase>> buscarRestriccionesBasePorIdImpartir(@Param("idImpartir") IdImpartir idImpartir);
}
