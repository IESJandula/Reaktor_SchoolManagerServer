package es.iesjandula.reaktor.school_manager_server.repositories.generador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorRestriccionesReduccion;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorRestriccionesReduccion;
import es.iesjandula.reaktor.school_manager_server.models.ProfesorReduccion;

@Repository
public interface IGeneradorRestriccionesReduccionRepository extends JpaRepository<GeneradorRestriccionesReduccion, IdGeneradorRestriccionesReduccion>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto(gri.idGeneradorRestriccionesReduccion.numeroRestriccion, gri.idGeneradorRestriccionesReduccion.diaTramoTipoHorario.diaDesc, gri.idGeneradorRestriccionesReduccion.diaTramoTipoHorario.tramoDesc) " +
           "FROM GeneradorRestriccionesReduccion gri " +
           "WHERE gri.idGeneradorRestriccionesReduccion.profesorReduccion = :profesorReduccion")
    Optional<List<GeneradorRestriccionBaseDto>> buscarRestriccionesReduccionProfesorDto(@Param("profesorReduccion") ProfesorReduccion profesorReduccion);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesReduccion gri " +
           "WHERE gri.idGeneradorRestriccionesReduccion.profesorReduccion = :profesorReduccion")
    Optional<List<GeneradorRestriccionesReduccion>> buscarRestriccionesPorReduccionProfesor(@Param("profesorReduccion") ProfesorReduccion profesorReduccion);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesReduccion gri " +
           "WHERE gri.idGeneradorRestriccionesReduccion.numeroRestriccion = :numeroRestriccion " +
             "AND gri.idGeneradorRestriccionesReduccion.profesorReduccion = :profesorReduccion")
    Optional<GeneradorRestriccionesReduccion> buscarRestriccionesPorNumeroRestriccionProfesorReduccion(@Param("numeroRestriccion") int numeroRestriccion, @Param("profesorReduccion") ProfesorReduccion profesorReduccion);
}
