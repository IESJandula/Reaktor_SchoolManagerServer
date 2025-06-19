package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.dtos.ImpartidaGrupoDeptDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ImpartirDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ImpartirHorasDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ProfesorImpartirDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Impartir;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdImpartir;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;

import java.util.List;
import java.util.Optional;

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

    @Query("SELECT COUNT(i) " +
            "FROM Impartir i " +
            "WHERE i.asignatura.idAsignatura.nombre = :nombre AND i.cupoHoras = :horas AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND i.asignatura.idAsignatura" +
            ".cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND i.asignatura.departamentoReceptor.nombre = :departamento ")
    Long encontrarAsignaturaAsignada(@Param("nombre") String nombre,
                                              @Param("horas") Integer horas,
                                              @Param("curso") Integer curso,
                                              @Param("etapa") String etapa,
                                     @Param("departamento") String departamento);

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
                                                     @Param("grupo") String grupo);

    @Query("SELECT i " +
            "FROM Impartir i " +
            "WHERE i.idImpartir.asignatura.idAsignatura.nombre = :nombreAsignatura AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<Impartir> encontrarAsignaturaImpartidaPorNombreAndCursoEtpa(@Param("nombreAsignatura") String nombreAsignatura,
                                                               @Param("curso") Integer curso,
                                                               @Param("etapa") String etapa);
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ProfesorImpartirDto(i.idImpartir.profesor.nombre, i.idImpartir.profesor.apellidos) " +
            "FROM Impartir i " +
            "WHERE i.idImpartir.asignatura.idAsignatura.nombre = :nombreAsignatura AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<ProfesorImpartirDto> encontrarProfesorPorNombreAndCursoEtpa(@Param("nombreAsignatura") String nombreAsignatura,
                                                            @Param("curso") Integer curso,
                                                            @Param("etapa") String etapa);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ImpartidaGrupoDeptDto(i.idImpartir.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo, a.departamentoReceptor.nombre)  " +
            "FROM Impartir i " +
            "JOIN i.asignatura a " +
            "WHERE i.idImpartir.asignatura.idAsignatura.nombre = :nombreAsignatura AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<ImpartidaGrupoDeptDto> encontrarGruposYDeptAsignaturaImpartidaPorNombreAndCursoEtapa(@Param("nombreAsignatura") String nombreAsignatura,
                                                                                         @Param("curso") Integer curso,
                                                                                         @Param("etapa") String etapa);

    /**
     * Método que devuelve los cursos/etapas/grupos que no tienen 30 horas a la semana asignadas de clase
     * @return Lista de cursos/etapas/grupos que no tienen 30 horas a la semana asignadas de clase
     */
    @Query("SELECT c " +
            "FROM CursoEtapaGrupo c " +
            "WHERE (SELECT COALESCE(SUM(i.cupoHoras), 0) " +
            "       FROM Impartir i " +
            "       WHERE i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = c.idCursoEtapaGrupo.curso AND " +
            "             i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = c.idCursoEtapaGrupo.etapa AND " +
            "             (i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = c.idCursoEtapaGrupo.grupo OR " +
            "              i.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = '" + Constants.GRUPO_OPTATIVAS + "')) <> 30")
    Optional<List<CursoEtapaGrupo>> cursoConHorasAsignadasIncorrectas();

}
