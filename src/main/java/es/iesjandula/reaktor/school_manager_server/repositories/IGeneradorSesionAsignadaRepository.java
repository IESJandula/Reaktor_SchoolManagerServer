package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaCursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaProfesorDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;

@Repository
public interface IGeneradorSesionAsignadaRepository extends JpaRepository<GeneradorSesionAsignada, IdGeneradorSesionAsignada>
{
    /**
     * Método que borra todas las sesiones asignadas de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM GeneradorSesionAsignada gsa WHERE gsa.idGeneradorSesionAsignada.idGeneradorInstancia = :id")
    void borrarPorIdGeneradorInstancia(Integer id);
    
    /**
     * Método que busca todas las sesiones asignadas de un profesor en la instancia elegida como solución
     * @param profesor - Profesor
     * @return - Lista de sesiones asignadas del profesor en la solución elegida
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaProfesorDto(gsa.asignatura.idAsignatura.nombre, " +
                                                                                                          "gsa.diaTramoTipoHorario.diaDesc, " +
                                                                                                          "gsa.diaTramoTipoHorario.tramoDesc, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.horarioMatutino, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
           "FROM GeneradorSesionAsignada gsa " +
           "WHERE gsa.profesor = :profesor " +
           "AND gsa.generadorInstancia.solucionElegida = true " + 
           "ORDER BY gsa.diaTramoTipoHorario.dia, gsa.diaTramoTipoHorario.tramo")
    Optional<List<GeneradorSesionAsignadaProfesorDto>> buscarHorarioProfesorSolucionElegida(Profesor profesor);

    /**
     * Método que busca todas las sesiones asignadas de un curso etapa grupo en la instancia elegida como solución
     * @param cursoEtapaGrupo - Curso etapa grupo
     * @return - Lista de sesiones asignadas del curso etapa grupo en la solución elegida
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaCursoEtapaGrupoDto(gsa.asignatura.idAsignatura.nombre, " +
                                                                                                                 "gsa.profesor.nombre, " +
                                                                                                                 "gsa.profesor.apellidos, " +
                                                                                                                 "gsa.diaTramoTipoHorario.diaDesc, " +
                                                                                                                 "gsa.diaTramoTipoHorario.tramoDesc, " +
                                                                                                                 "gsa.asignatura.idAsignatura.cursoEtapaGrupo.horarioMatutino) " +
           "FROM GeneradorSesionAsignada gsa " +
           "WHERE gsa.asignatura.idAsignatura.cursoEtapaGrupo = :cursoEtapaGrupo " +
           "AND gsa.generadorInstancia.solucionElegida = true " +
           "ORDER BY gsa.diaTramoTipoHorario.dia, gsa.diaTramoTipoHorario.tramo")
    Optional<List<GeneradorSesionAsignadaCursoEtapaGrupoDto>> buscarHorarioCursoEtapaGrupoSolucionElegida(CursoEtapaGrupo cursoEtapaGrupo);

    /**
     * Método que cuenta cuántos huecos hay entre sesiones de un profesor, teniendo en cuenta
     * que se considera un hueco aquel en el que el profesor ya ha tenido clase previamente pero no tiene clase
     * para posteriormente tenerla de nuevo
     * 
     * @param generadorInstancia - Generador instancia
     * @param profesor - Profesor
     * @param esMatutino - Indica si es matutino
     * @return - Número de huecos entre sesiones
     */
    @Query(value = "SELECT COALESCE(SUM(huecos),0) FROM ( " +
           "  SELECT (MAX(dtth.tramo) - MIN(dtth.tramo) + 1) - COUNT(gsa.profesor_email) AS huecos " +
           "  FROM dia_tramo_tipo_horario dtth " +
           "  JOIN generador_sesion_asignada gsa ON gsa.dia_tramo_tipo_horario_id = dtth.id " +
           "  WHERE gsa.profesor_email = :email " +
           "    AND gsa.generador_instancia_id = :generadorInstanciaId " +
           "    AND dtth.horario_matutino = :esMatutino " +
           "    AND dtth.dia != -1 AND dtth.tramo != -1 " +
           "  GROUP BY dtth.dia " +
           ") AS subconsulta", nativeQuery = true)
    Integer contarHuecosEntreSesiones(@Param("generadorInstanciaId") Integer generadorInstanciaId, @Param("email") String profesorId, @Param("esMatutino") Boolean esMatutino);

    /**
     * Método que busca todos los profesores que tengan horario matutino o vespertino
     * @param esMatutino - Indica si es matutino
     * @return - Lista de profesores
     */
    @Query("SELECT DISTINCT gsa.profesor FROM GeneradorSesionAsignada gsa " +
           "WHERE gsa.generadorInstancia = :generadorInstancia " +
             "AND gsa.diaTramoTipoHorario.horarioMatutino = :esMatutino")
    List<Profesor> buscarProfesoresTipoHorario(GeneradorInstancia generadorInstancia, Boolean esMatutino);
}
