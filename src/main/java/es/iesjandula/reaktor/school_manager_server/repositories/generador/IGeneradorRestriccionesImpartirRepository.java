package es.iesjandula.reaktor.school_manager_server.repositories.generador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorRestriccionesImpartir;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto;
import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionesImpartirDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorRestriccionesImpartir;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;

@Repository
public interface IGeneradorRestriccionesImpartirRepository extends JpaRepository<GeneradorRestriccionesImpartir, IdGeneradorRestriccionesImpartir>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionBaseDto(gri.idGeneradorRestriccionesImpartir.numeroRestriccion, gri.idGeneradorRestriccionesImpartir.diaTramoTipoHorario.diaDesc, gri.idGeneradorRestriccionesImpartir.diaTramoTipoHorario.tramoDesc) " +
           "FROM GeneradorRestriccionesImpartir gri " +
           "WHERE gri.idGeneradorRestriccionesImpartir.asignatura = :asignatura " +
             "AND gri.idGeneradorRestriccionesImpartir.profesor = :profesor")
    Optional<List<GeneradorRestriccionBaseDto>> buscarRestriccionesPorAsignaturaProfesorDto(@Param("asignatura") Asignatura asignatura, @Param("profesor") Profesor profesor);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesImpartir gri " +
           "WHERE gri.idGeneradorRestriccionesImpartir.asignatura = :asignatura " +
             "AND gri.idGeneradorRestriccionesImpartir.profesor = :profesor")
    Optional<List<GeneradorRestriccionesImpartir>> buscarRestriccionesPorAsignaturaProfesor(@Param("asignatura") Asignatura asignatura, @Param("profesor") Profesor profesor);

    @Query("SELECT gri " +
           "FROM GeneradorRestriccionesImpartir gri " +
           "WHERE gri.idGeneradorRestriccionesImpartir.numeroRestriccion = :numeroRestriccion " +
             "AND gri.idGeneradorRestriccionesImpartir.profesor = :profesor " +
             "AND gri.idGeneradorRestriccionesImpartir.asignatura = :asignatura")
    Optional<GeneradorRestriccionesImpartir> buscarRestriccionesPorNumeroRestriccionProfesorAsignatura(@Param("numeroRestriccion") int numeroRestriccion, @Param("profesor") Profesor profesor, @Param("asignatura") Asignatura asignatura);


    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorRestriccionesImpartirDto(gri.idGeneradorRestriccionesImpartir.asignatura, gri.idGeneradorRestriccionesImpartir.profesor, gri.idGeneradorRestriccionesImpartir.asignatura.idAsignatura.cursoEtapaGrupo, gri.idGeneradorRestriccionesImpartir.diaTramoTipoHorario) " +
           "FROM GeneradorRestriccionesImpartir gri JOIN gri.idGeneradorRestriccionesImpartir.asignatura a " +
           "JOIN a.idAsignatura ai " +
           "JOIN ai.cursoEtapaGrupo ceg " +
           "JOIN gri.idGeneradorRestriccionesImpartir.profesor p " +
           "JOIN gri.idGeneradorRestriccionesImpartir.diaTramoTipoHorario dth")
    Optional<List<GeneradorRestriccionesImpartirDto>> obtenerTodasLasRestricciones();
}
