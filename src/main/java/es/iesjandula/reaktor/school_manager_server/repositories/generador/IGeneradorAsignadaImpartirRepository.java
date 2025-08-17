package es.iesjandula.reaktor.school_manager_server.repositories.generador;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorAsignadaImpartirCursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorAsignadaImpartirProfesorDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorAsignadaImpartir;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorAsignadaImpartir;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;

@Repository
public interface IGeneradorAsignadaImpartirRepository extends JpaRepository<GeneradorAsignadaImpartir, IdGeneradorAsignadaImpartir>
{
    /**
     * Método que borra todas las sesiones asignadas de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM GeneradorAsignadaImpartir gai WHERE gai.idGeneradorAsignadaImpartir.idGeneradorInstancia = :id")
    void borrarPorIdGeneradorInstancia(Integer id);
    
    /**
     * Método que busca todas las sesiones asignadas de un profesor en la instancia elegida como solución
     * @param profesor - Profesor
     * @return - Lista de sesiones asignadas del profesor en la solución elegida
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorAsignadaImpartirProfesorDto(gai.diaTramoTipoHorario.diaDesc, " +
                                                                                                                      "gai.diaTramoTipoHorario.tramoDesc, " +
                                                                                                                      "gai.asignatura.idAsignatura.cursoEtapaGrupo.horarioMatutino, " +
                                                                                                                      "gai.asignatura.idAsignatura.nombre, " +
                                                                                                                      "gai.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, " +
                                                                                                                      "gai.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, " +
                                                                                                                      "gai.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
           "FROM GeneradorAsignadaImpartir gai " +
           "WHERE gai.profesor = :profesor " +
           "AND gai.generadorInstancia.solucionElegida = true " + 
           "ORDER BY gai.diaTramoTipoHorario.dia, gai.diaTramoTipoHorario.tramo")
    Optional<List<GeneradorAsignadaImpartirProfesorDto>> buscarHorarioProfesorSolucionElegida(Profesor profesor);

    /**
     * Método que busca todas las sesiones asignadas de un curso etapa grupo en la instancia elegida como solución
     * @param cursoEtapaGrupo - Curso etapa grupo
     * @return - Lista de sesiones asignadas del curso etapa grupo en la solución elegida
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.generador.GeneradorAsignadaImpartirCursoEtapaGrupoDto(gai.diaTramoTipoHorario.diaDesc, " +
                                                                                                                             "gai.diaTramoTipoHorario.tramoDesc, " +
                                                                                                                             "gai.asignatura.idAsignatura.cursoEtapaGrupo.horarioMatutino, " +
                                                                                                                             "gai.asignatura.idAsignatura.nombre, " +
                                                                                                                             "gai.profesor.nombre, " +
                                                                                                                             "gai.profesor.apellidos) " +
           "FROM GeneradorAsignadaImpartir gai " +
           "WHERE gai.asignatura.idAsignatura.cursoEtapaGrupo = :cursoEtapaGrupo " +
           "AND gai.generadorInstancia.solucionElegida = true " +
           "ORDER BY gai.diaTramoTipoHorario.dia, gai.diaTramoTipoHorario.tramo")
    Optional<List<GeneradorAsignadaImpartirCursoEtapaGrupoDto>> buscarHorarioCursoEtapaGrupoSolucionElegida(CursoEtapaGrupo cursoEtapaGrupo);

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
           "  SELECT (MAX(dtth.tramo) - MIN(dtth.tramo) + 1) - COUNT(gai.profesor_email) AS huecos " +
           "  FROM dia_tramo_tipo_horario dtth " +
           "  JOIN generador_asignada_impartir gai ON gai.dia_tramo_tipo_horario_id = dtth.id " +
           "  WHERE gai.profesor_email = :email " +
           "    AND gai.generador_instancia_id = :generadorInstanciaId " +
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
    @Query("SELECT DISTINCT p FROM GeneradorAsignadaImpartir gai " +
           "JOIN gai.profesor p " +
           "LEFT JOIN FETCH p.preferenciasHorariasProfesor " +
           "LEFT JOIN FETCH p.observacionesAdicionales " +
           "WHERE gai.generadorInstancia = :generadorInstancia " +
             "AND gai.diaTramoTipoHorario.horarioMatutino = :esMatutino")
    List<Profesor> buscarProfesoresTipoHorario(GeneradorInstancia generadorInstancia, Boolean esMatutino);

    /**
     * Método que cuenta cuántas veces se ha asignado una sesión a un profesor en la instancia elegida como solución
     * @param generadorInstancia - Generador instancia
     * @param profesor - Profesor
     * @param esMatutino - Indica si es matutino
     * @return - Número de veces que se ha asignado una sesión a un profesor
     */
    @Query(value = "SELECT COUNT(*) " +
                   "FROM GeneradorAsignadaImpartir gai " +
                   "WHERE gai.profesor.email = :profesorEmail " + 
                     "AND gai.generadorInstancia.id = :generadorInstanciaId " +
                     "AND gai.diaTramoTipoHorario.horarioMatutino = :esMatutino " +
                     "AND gai.diaTramoTipoHorario.tramo = " + Constants.TRAMO_HORARIO_PRIMERA_HORA)
    Integer contarPreferenciasDiariasPrimeraHora(@Param("generadorInstanciaId") Integer generadorInstanciaId, @Param("profesorEmail") String profesorEmail, @Param("esMatutino") Boolean esMatutino);

    /**
     * Método que cuenta cuántas veces se ha asignado una sesión a un profesor en la instancia elegida como solución
     * @param generadorInstancia - Generador instancia
     * @param profesor - Profesor
     * @param esMatutino - Indica si es matutino o vespertino
     * @return - Número de veces que se ha asignado una sesión a un profesor
     */
    @Query(value = "SELECT COUNT(*) " +
                   "FROM GeneradorAsignadaImpartir gai " +
                   "WHERE gai.profesor.email = :profesorEmail " + 
                     "AND gai.generadorInstancia.id = :generadorInstanciaId " +
                     "AND gai.diaTramoTipoHorario.horarioMatutino = :esMatutino " +
                     "AND gai.diaTramoTipoHorario.tramo = " + Constants.TRAMO_HORARIO_SEXTA_HORA)
    Integer contarPreferenciasDiariasUltimaHora(@Param("generadorInstanciaId") Integer generadorInstanciaId, @Param("profesorEmail") String profesorEmail, @Param("esMatutino") Boolean esMatutino);

    /**
     * Método que cuenta cuántas veces se ha asignado una sesión a un profesor en la instancia elegida como solución
     * @param generadorInstancia - Generador instancia
     * @param profesor - Profesor
     * @param esMatutino - Indica si es matutino o vespertino
     * @param tramo - Tramo
     * @param dia - Día
     * @return - Número de veces que se ha asignado una sesión a un profesor
     */
    @Query(value = "SELECT CASE WHEN COUNT(gai) > 0 THEN 0 ELSE 1 END  " +
                   "FROM GeneradorAsignadaImpartir gai " +
                   "WHERE gai.profesor.email = :profesorEmail " + 
                     "AND gai.generadorInstancia.id = :generadorInstanciaId " +
                     "AND gai.diaTramoTipoHorario.horarioMatutino = :esMatutino " +
                     "AND gai.diaTramoTipoHorario.tramo = :tramo " +
                     "AND gai.diaTramoTipoHorario.dia = :dia")
    Integer contarPreferenciasConcretas(@Param("generadorInstanciaId") Integer generadorInstanciaId, @Param("profesorEmail") String profesorEmail, @Param("esMatutino") Boolean esMatutino, @Param("dia") Integer dia, @Param("tramo") Integer tramo);
}
