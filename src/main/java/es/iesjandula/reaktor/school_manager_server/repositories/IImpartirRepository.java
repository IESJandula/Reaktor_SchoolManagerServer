package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.dtos.ImpartirDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ImpartirHorasDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdImpartir;

import java.util.List;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Impartir}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Impartir}.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IImpartirRepository extends JpaRepository<Impartir, IdImpartir>
{

    @Query("SELECT i " +
            "FROM Impartir i " +
            "WHERE i.asignatura.idAsignatura.nombre = :nombre AND i.cupoHoras = :horas AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo")
    Impartir encontrarAsignaturaAsignada(@Param("nombre") String nombre,
                                         @Param("horas") Integer horas,
                                         @Param("curso") Integer curso,
                                         @Param("etapa") String etapa,
                                         @Param("grupo") Character grupo);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ImpartirHorasDto(i.asignatura.idAsignatura.nombre, i.asignatura.horas, i.cupoHoras, i.asignatura.idAsignatura.cursoEtapaGrupo" +
            ".idCursoEtapaGrupo.curso, i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo, i.asignadoDireccion) " +
            "FROM Impartir i " +
            "WHERE i.idImpartir.profesor.email = :email")
    List<ImpartirHorasDto> encontrarAsignaturasImpartidasPorEmail(@Param("email") String email);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ImpartirDto(i.asignatura.idAsignatura.nombre, i.cupoHoras, i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
            "FROM Impartir i " +
            "WHERE i.idImpartir.profesor.email = :email AND i.asignatura.idAsignatura.nombre = :nombre AND i.cupoHoras = :horas AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo")
    ImpartirDto encontrarAsignaturaImpartidaPorEmail(@Param("email") String email,
                                                     @Param("nombre") String nombre,
                                                     @Param("horas") Integer horas,
                                                     @Param("curso") Integer curso,
                                                     @Param("etapa") String etapa,
                                                     @Param("grupo") Character grupo);

}
