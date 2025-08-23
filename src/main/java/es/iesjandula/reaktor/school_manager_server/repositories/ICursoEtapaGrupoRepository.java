package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link CursoEtapaGrupo}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite trabajar con la entidad {@link CursoEtapaGrupo} 
 * y su clave primaria {@link IdCursoEtapaGrupo}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface ICursoEtapaGrupoRepository extends JpaRepository<CursoEtapaGrupo, IdCursoEtapaGrupo>
{
    /**
     * Método que busca un curso etapa grupo por curso, etapa y grupo
     * @param curso - Curso
     * @param etapa - Etapa
     * @param grupo - Grupo
     * @return - Curso etapa grupo
     */ 
    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo = :grupo")
    CursoEtapaGrupo buscarCursoEtapaGrupo(@Param("curso") int curso,
                                          @Param("etapa") String etapa,
                                          @Param("grupo") String grupo);

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    public List<CursoEtapaGrupo> buscarTodosLosCursosEtapasGrupos();

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "' AND c.idCursoEtapaGrupo.grupo <> '" + Constants.GRUPO_OPTATIVAS + "'")
    public List<CursoEtapaGrupo> buscarTodosLosCursosEtapasGruposSinOptativas();

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "' AND c.idCursoEtapaGrupo.grupo <> '" + Constants.GRUPO_OPTATIVAS + "' AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public List<CursoEtapaGrupo> buscarTodosLosCursosEtapasGruposSinOptativas(@Param("curso") int curso,
                                                                              @Param("etapa") String etapa);

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.grupo = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo = 'Optativas'")
    CursoEtapaGrupo buscarCursosEtapasGrupoOptativas(@Param("curso") int curso,
                                                     @Param("etapa") String etapa);

    /**
     * Cuenta el número de elementos en la tabla {@link CursoEtapaGrupo} que corresponden a un curso y etapa 
     * específicos.
     * 
     * @param curso - El curso para el que se desea contar los registros.
     * @param etapa - La etapa para la que se desea contar los registros.
     * @return Número de registros que coinciden con el curso y etapa proporcionados.
     */
    @Query("SELECT COUNT(c) "
    		+ "FROM CursoEtapaGrupo c "
    		+ "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "' AND c.idCursoEtapaGrupo.grupo <> '" + Constants.GRUPO_OPTATIVAS + "'")
    public int cuentaCursoEtapaGruposCreados(@Param("curso") int curso, @Param("etapa") String etapa);

    /**
     * Obtiene una lista de los grupos que corresponden a un curso y etapa específicos excepto los grupos Sin grupo y Optativas.
     *
     * @param curso - El curso para el que se desea obtener los grupos.
     * @param etapa - La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto(c.idCursoEtapaGrupo.curso, c.idCursoEtapaGrupo.etapa, c.idCursoEtapaGrupo.grupo, c.horarioMatutino, c.esoBachillerato) "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "' AND c.idCursoEtapaGrupo.grupo <> '" + Constants.GRUPO_OPTATIVAS + "'")
    public List<CursoEtapaGrupoDto> buscaCursoEtapaGruposCreados(@Param("curso") int curso, @Param("etapa") String etapa);

    /**
     * Obtiene una lista de los grupos que corresponden a un curso y etapa específicos.
     *
     * @param curso - El curso para el que se desea obtener los grupos.
     * @param etapa - La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto(c.idCursoEtapaGrupo.curso, c.idCursoEtapaGrupo.etapa, c.idCursoEtapaGrupo.grupo, c.horarioMatutino, c.esoBachillerato) "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    public List<CursoEtapaGrupoDto> buscaTodosCursoEtapaGruposCreados(@Param("curso") int curso, @Param("etapa") String etapa);

    /**
     * Obtiene una lista de los grupos incluidos los del grupo Z que corresponden a un curso y etapa específicos.
     *
     * @param curso 		- El curso para el que se desea obtener los grupos.
     * @param etapa 		- La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto(c.idCursoEtapaGrupo.curso, c.idCursoEtapaGrupo.etapa, c.idCursoEtapaGrupo.grupo, c.horarioMatutino, c.esoBachillerato) "
    		+ "FROM CursoEtapaGrupo c "
    		+ "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public List<CursoEtapaGrupoDto> encontrarCursoEtapaGruposCreados(@Param("curso") int curso, @Param("etapa") String etapa);

    /**
     * Obtiene una lista de los grupos que corresponden a un curso y etapa específicos.
     *
     * @param curso 		- El curso para el que se desea obtener los grupos.
     * @param etapa 		- La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT c.idCursoEtapaGrupo.grupo "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    public List<String> buscaLetrasGruposDeCursoEtapas(@Param("curso") int curso, @Param("etapa") String etapa);

    @Transactional
    @Modifying
    @Query("DELETE FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public void borrarPorCursoEtapa(@Param("curso") Integer curso,
                                    @Param("etapa") String etapa) ;

    @Transactional
    @Modifying
    @Query("DELETE FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo = '" + Constants.GRUPO_OPTATIVAS + "'")
    public void borrarPorCursoEtapaGrupo(@Param("curso") Integer curso,
                                         @Param("etapa") String etapa);

    

    /**
     * Método que devuelve todos los cursos/etapas/grupos que no tienen 30 horas a la semana asignadas de clase
     * @return Lista de cursos/etapas/grupos que no tienen 30 horas a la semana asignadas de clase
     */
    @Query(value = """
        SELECT c.*
        FROM `Curso_Etapa_Grupo` c
        WHERE c.grupo <> :sinGrupo  AND
              c.grupo <> :optativas AND
          (
          COALESCE((
            SELECT SUM(a.horas)
            FROM impartir i
            JOIN asignatura a
               ON a.curso  = i.asignatura_curso
              AND a.etapa  = i.asignatura_etapa
              AND a.grupo  = i.asignatura_grupo
			        AND a.nombre = i.asignatura_nombre
            WHERE i.asignatura_curso = c.curso
              AND i.asignatura_etapa = c.etapa
              AND i.asignatura_grupo = c.grupo
              AND i.asignatura_grupo <> :sinGrupo
              AND i.asignatura_grupo <> :optativas
          ), 0)
          +
          COALESCE((
            SELECT SUM(t.horas)
            FROM (
              SELECT MAX(a.horas) AS horas
              FROM impartir i
              JOIN asignatura a
                ON a.curso  = i.asignatura_curso
               AND a.etapa  = i.asignatura_etapa
               AND a.grupo  = :optativas
			         AND a.nombre = i.asignatura_nombre
              JOIN bloque b
                ON b.id = a.bloque_id
              WHERE i.asignatura_curso = c.curso
                AND i.asignatura_etapa = c.etapa
                AND i.asignatura_grupo = :optativas
              GROUP BY a.bloque_id
            ) t
          ), 0)
          +
          COALESCE((
            SELECT SUM(r.horas)
            FROM profesor_reduccion pr
            JOIN reduccion r
              ON pr.reduccion_nombre = r.nombre
             AND pr.reduccion_horas  = r.horas
            WHERE r.curso = c.curso
              AND r.etapa = c.etapa
              AND r.grupo = c.grupo
          ), 0)
        ) <> 30
        """, nativeQuery = true)
        Optional<List<CursoEtapaGrupo>> cursoConHorasAsignadasIncorrectas(@Param("sinGrupo") String sinGrupo,
                                                                          @Param("optativas") String optativas);
        

    /**
     * Método helper que llama a cursoConHorasAsignadasIncorrectas con las constantes
     * @return Lista de cursos/etapas/grupos que no tienen 30 horas a la semana asignadas de clase
     */
    default Optional<List<CursoEtapaGrupo>> cursoConHorasAsignadasIncorrectas()
    {
        return cursoConHorasAsignadasIncorrectas(Constants.SIN_GRUPO_ASIGNADO, Constants.GRUPO_OPTATIVAS);
    }

  /**
     * Método que devuelve el total de horas asignadas (asignaturas + reducciones) para un curso/etapa/grupo específico
     * @param curso el curso del grupo
     * @param etapa la etapa del grupo
     * @param grupo el grupo específico
     * @return el total de horas asignadas (asignaturas + reducciones)
     */
    @Query(value = """
        SELECT
          COALESCE((
            SELECT SUM(a.horas)
            FROM impartir i 
            JOIN asignatura a
               ON a.curso  = i.asignatura_curso
              AND a.etapa  = i.asignatura_etapa
              AND a.grupo  = i.asignatura_grupo
			        AND a.nombre = i.asignatura_nombre
            WHERE i.asignatura_curso = :curso
              AND i.asignatura_etapa = :etapa
              AND i.asignatura_grupo = :grupo
              AND i.asignatura_grupo <> :sinGrupo
              AND i.asignatura_grupo <> :optativas
          ), 0)
        +
          COALESCE((
            SELECT SUM(t.horas)
            FROM (
              SELECT MAX(a.horas) AS horas
              FROM impartir i
              JOIN asignatura a
                ON a.curso  = i.asignatura_curso
               AND a.etapa  = i.asignatura_etapa
               AND a.grupo  = :optativas
			         AND a.nombre = i.asignatura_nombre
              JOIN bloque b
                ON b.id = a.bloque_id
              WHERE i.asignatura_curso = :curso
                AND i.asignatura_etapa = :etapa
                AND i.asignatura_grupo = :optativas
              GROUP BY a.bloque_id
            ) t
          ), 0)
        +
          COALESCE((
            SELECT SUM(r.horas)
            FROM profesor_reduccion pr
            JOIN reduccion r
              ON pr.reduccion_nombre = r.nombre
             AND pr.reduccion_horas  = r.horas
            WHERE r.curso = :curso
              AND r.etapa = :etapa
              AND r.grupo = :grupo
          ), 0) AS total
        """, nativeQuery = true)
    Long obtenerTotalHorasAsignadas(@Param("curso") Integer curso, 
                                   @Param("etapa") String etapa, 
                                   @Param("grupo") String grupo,
                                   @Param("sinGrupo") String sinGrupo,
                                   @Param("optativas") String optativas);

    /**
     * Método helper que llama a obtenerTotalHorasAsignadas con las constantes
     * @param curso el curso del grupo
     * @param etapa la etapa del grupo
     * @param grupo el grupo específico
     * @return el total de horas asignadas (asignaturas + reducciones)
     */
    default Long obtenerTotalHorasAsignadas(Integer curso, String etapa, String grupo)
    {
        return obtenerTotalHorasAsignadas(curso, etapa, grupo, Constants.SIN_GRUPO_ASIGNADO, Constants.GRUPO_OPTATIVAS);
    }
}
