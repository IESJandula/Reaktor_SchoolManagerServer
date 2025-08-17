package es.iesjandula.reaktor.school_manager_server.repositories.generador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorRestriccionesReduccion;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto;
import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionesReduccionDto;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorRestriccionesReduccion;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;

@Repository
public interface IGeneradorRestriccionesReduccionRepository extends JpaRepository<GeneradorRestriccionesReduccion, IdGeneradorRestriccionesReduccion>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto(gri.idGeneradorRestriccionesReduccion.numeroRestriccion, gri.idGeneradorRestriccionesReduccion.diaTramoTipoHorario.diaDesc, gri.idGeneradorRestriccionesReduccion.diaTramoTipoHorario.tramoDesc) " +
           "FROM GeneradorRestriccionesReduccion gri " +
           "WHERE gri.idGeneradorRestriccionesReduccion.reduccion = :reduccion " +
             "AND gri.idGeneradorRestriccionesReduccion.profesor = :profesor")
    Optional<List<GeneradorRestriccionBaseDto>> buscarRestriccionesReduccionProfesorDto(@Param("reduccion") Reduccion reduccion, @Param("profesor") Profesor profesor);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesReduccion gri " +
           "WHERE gri.idGeneradorRestriccionesReduccion.reduccion = :reduccion " +
             "AND gri.idGeneradorRestriccionesReduccion.profesor = :profesor")
    Optional<List<GeneradorRestriccionesReduccion>> buscarRestriccionesPorReduccionProfesor(@Param("reduccion") Reduccion reduccion, @Param("profesor") Profesor profesor);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesReduccion gri " +
           "WHERE gri.idGeneradorRestriccionesReduccion.numeroRestriccion = :numeroRestriccion " +
             "AND gri.idGeneradorRestriccionesReduccion.profesor = :profesor " +
             "AND gri.idGeneradorRestriccionesReduccion.reduccion = :reduccion")
    Optional<GeneradorRestriccionesReduccion> buscarRestriccionesPorNumeroRestriccionProfesorReduccion(@Param("numeroRestriccion") int numeroRestriccion, @Param("profesor") Profesor profesor, @Param("reduccion") Reduccion reduccion);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionesReduccionDto(gri.idGeneradorRestriccionesReduccion.reduccion, gri.idGeneradorRestriccionesReduccion.profesor, gri.idGeneradorRestriccionesReduccion.reduccion.cursoEtapaGrupo, gri.idGeneradorRestriccionesReduccion.diaTramoTipoHorario) " +
           "FROM GeneradorRestriccionesReduccion gri JOIN gri.idGeneradorRestriccionesReduccion.reduccion r " +
           "JOIN r.cursoEtapaGrupo ceg " +
           "JOIN gri.idGeneradorRestriccionesReduccion.profesor p " +
           "JOIN gri.idGeneradorRestriccionesReduccion.diaTramoTipoHorario dth")
    Optional<List<GeneradorRestriccionesReduccionDto>> obtenerTodasLasRestricciones();
}
