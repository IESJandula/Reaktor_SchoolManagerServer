package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionBase;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.SesionBaseDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionBase;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;

@Repository
public interface IGeneradorSesionBaseRepository extends JpaRepository<GeneradorSesionBase, IdGeneradorSesionBase>
{
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.SesionBaseDto(gsb.idGeneradorSesionBase.numeroSesion, gsb.idGeneradorSesionBase.diasTramosTipoHorario.idDiasTramosTipoHorario.dia, gsb.idGeneradorSesionBase.diasTramosTipoHorario.idDiasTramosTipoHorario.tramo) " +
           "FROM GeneradorSesionBase gsb " +
           "WHERE gsb.idGeneradorSesionBase.asignatura = :asignatura " +
           "AND gsb.idGeneradorSesionBase.profesor = :profesor")
    Optional<List<SesionBaseDto>> buscarSesionesBasePorAsignaturaProfesor(@Param("asignatura") Asignatura asignatura, @Param("profesor") Profesor profesor);

    @Query("SELECT gsb " +
           "FROM GeneradorSesionBase gsb " +
           "WHERE gsb.idGeneradorSesionBase.numeroSesion = :numeroSesion " +
           "AND gsb.idGeneradorSesionBase.profesor = :profesor " +
           "AND gsb.idGeneradorSesionBase.asignatura = :asignatura")
    Optional<GeneradorSesionBase> buscarSesionBasePorNumeroSesionProfesorAsignatura(@Param("numeroSesion") int numeroSesion, @Param("profesor") Profesor profesor, @Param("asignatura") Asignatura asignatura);
}
