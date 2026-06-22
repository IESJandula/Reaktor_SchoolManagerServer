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
 * Todas las consultas del conjunto de trabajo (matrículas, asignaturas, grupos docentes y horarios) se filtran por
 * el curso académico activo ({@code cursoAcademico}), que el backend resuelve desde {@code seleccionado = true}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface ICursoEtapaGrupoRepository extends JpaRepository<CursoEtapaGrupo, IdCursoEtapaGrupo>
{
    /**
     * Obtiene todos los cursos, etapas y grupos del curso académico indicado en formato DTO ordenados por curso, etapa y grupo.
     *
     * @param cursoAcademico - El curso académico activo.
     * @return La lista de cursos, etapas y grupos en formato DTO.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto(c.idCursoEtapaGrupo.curso, c.idCursoEtapaGrupo.etapa, c.idCursoEtapaGrupo.grupo, c.horarioMatutino, c.esoBachillerato) "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "ORDER BY c.idCursoEtapaGrupo.curso ASC, c.idCursoEtapaGrupo.etapa ASC, c.idCursoEtapaGrupo.grupo ASC")
    List<CursoEtapaGrupoDto> findAllDto(@Param("cursoAcademico") String cursoAcademico);

    /**
     * Método que busca un curso etapa grupo por curso, etapa y grupo dentro del curso académico activo.
     * @param cursoAcademico - El curso académico activo.
     * @param curso - Curso
     * @param etapa - Etapa
     * @param grupo - Grupo
     * @return - Curso etapa grupo
     */ 
    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo = :grupo")
    CursoEtapaGrupo buscarCursoEtapaGrupo(@Param("cursoAcademico") String cursoAcademico,
                                          @Param("curso") int curso,
                                          @Param("etapa") String etapa,
                                          @Param("grupo") String grupo);

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    public List<CursoEtapaGrupo> buscarTodosLosCursosEtapasGrupos(@Param("cursoAcademico") String cursoAcademico);

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "' AND c.idCursoEtapaGrupo.grupo <> '" + Constants.GRUPO_OPTATIVAS + "'")
    public List<CursoEtapaGrupo> buscarTodosLosCursosEtapasGruposSinOptativas(@Param("cursoAcademico") String cursoAcademico);

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "' AND c.idCursoEtapaGrupo.grupo <> '" + Constants.GRUPO_OPTATIVAS + "' AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public List<CursoEtapaGrupo> buscarTodosLosCursosEtapasGruposSinOptativas(@Param("cursoAcademico") String cursoAcademico,
                                                                              @Param("curso") int curso,
                                                                              @Param("etapa") String etapa);

    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo = 'Optativas'")
    CursoEtapaGrupo buscarCursosEtapasGrupoOptativas(@Param("cursoAcademico") String cursoAcademico,
                                                     @Param("curso") int curso,
                                                     @Param("etapa") String etapa);

    /**
     * Obtiene una lista de los grupos que corresponden a un curso y etapa específicos dentro del curso académico activo.
     *
     * @param cursoAcademico - El curso académico activo.
     * @param curso - El curso para el que se desea obtener los grupos.
     * @param etapa - La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto(c.idCursoEtapaGrupo.curso, c.idCursoEtapaGrupo.etapa, c.idCursoEtapaGrupo.grupo, c.horarioMatutino, c.esoBachillerato) "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    public List<CursoEtapaGrupoDto> buscaTodosCursoEtapaGruposCreados(@Param("cursoAcademico") String cursoAcademico, @Param("curso") int curso, @Param("etapa") String etapa);

    /**
     * Obtiene una lista de los grupos incluidos los del grupo Z que corresponden a un curso y etapa específicos.
     *
     * @param cursoAcademico - El curso académico activo.
     * @param curso 		- El curso para el que se desea obtener los grupos.
     * @param etapa 		- La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto(c.idCursoEtapaGrupo.curso, c.idCursoEtapaGrupo.etapa, c.idCursoEtapaGrupo.grupo, c.horarioMatutino, c.esoBachillerato) "
    		+ "FROM CursoEtapaGrupo c "
    		+ "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
    		+ "AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public List<CursoEtapaGrupoDto> encontrarCursoEtapaGruposCreados(@Param("cursoAcademico") String cursoAcademico, @Param("curso") int curso, @Param("etapa") String etapa);

    /**
     * Obtiene una lista de los grupos que corresponden a un curso y etapa específicos dentro del curso académico activo.
     *
     * @param cursoAcademico - El curso académico activo.
     * @param curso 		- El curso para el que se desea obtener los grupos.
     * @param etapa 		- La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT c.idCursoEtapaGrupo.grupo "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    public List<String> buscaLetrasGruposDeCursoEtapas(@Param("cursoAcademico") String cursoAcademico, @Param("curso") int curso, @Param("etapa") String etapa);

    @Transactional
    @Modifying
    @Query("DELETE FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public void borrarPorCursoEtapa(@Param("cursoAcademico") String cursoAcademico,
                                    @Param("curso") Integer curso,
                                    @Param("etapa") String etapa) ;

    @Transactional
    @Modifying
    @Query("DELETE FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa AND c.idCursoEtapaGrupo.grupo = '" + Constants.GRUPO_OPTATIVAS + "'")
    public void borrarPorCursoEtapaGrupo(@Param("cursoAcademico") String cursoAcademico,
                                         @Param("curso") Integer curso,
                                         @Param("etapa") String etapa);

    /**
     * Borra todas las filas de catálogo curso/etapa (grupo {@link Constants#GRUPO_CATALOGO_CURSO_ETAPA}) de un curso académico.
     *
     * @param cursoAcademico - El curso académico.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "AND c.idCursoEtapaGrupo.grupo = '" + Constants.GRUPO_CATALOGO_CURSO_ETAPA + "'")
    void deleteAllCatalogoByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

    

    /**
     * Método que devuelve todos los cursos/etapas/grupos del curso académico activo que no tienen 30 horas a la semana asignadas de clase
     * @return Lista de cursos/etapas/grupos que no tienen 30 horas a la semana asignadas de clase
     */
    @Query(value = """
        SELECT c.*
        FROM `Curso_Etapa_Grupo` c
        WHERE c.cursoAcademico = :cursoAcademico AND
              c.grupo <> :sinGrupo  AND
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
              AND a.cursoAcademico = i.asignatura_cursoAcademico
            WHERE i.asignatura_curso = c.curso
              AND i.asignatura_etapa = c.etapa
              AND i.asignatura_grupo = c.grupo
              AND i.asignatura_cursoAcademico = c.cursoAcademico
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
               AND a.cursoAcademico = i.asignatura_cursoAcademico
              JOIN bloque b
                ON b.id = a.bloque_id
              WHERE i.asignatura_curso = c.curso
                AND i.asignatura_etapa = c.etapa
                AND i.asignatura_grupo = :optativas
                AND i.asignatura_cursoAcademico = c.cursoAcademico
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
              AND r.cursoAcademico = c.cursoAcademico
          ), 0)
        ) <> 30
        """, nativeQuery = true)
        Optional<List<CursoEtapaGrupo>> cursoConHorasAsignadasIncorrectas(@Param("cursoAcademico") String cursoAcademico,
                                                                          @Param("sinGrupo") String sinGrupo,
                                                                          @Param("optativas") String optativas);
        

    /**
     * Método helper que llama a cursoConHorasAsignadasIncorrectas con las constantes
     * @param cursoAcademico - El curso académico activo.
     * @return Lista de cursos/etapas/grupos que no tienen 30 horas a la semana asignadas de clase
     */
    default Optional<List<CursoEtapaGrupo>> cursoConHorasAsignadasIncorrectas(String cursoAcademico)
    {
        return cursoConHorasAsignadasIncorrectas(cursoAcademico, Constants.SIN_GRUPO_ASIGNADO, Constants.GRUPO_OPTATIVAS);
    }

  /**
     * Método que devuelve el total de horas asignadas (asignaturas + reducciones) para un curso/etapa/grupo específico del curso académico activo
     * @param cursoAcademico el curso académico activo
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
              AND a.cursoAcademico = i.asignatura_cursoAcademico
            WHERE i.asignatura_curso = :curso
              AND i.asignatura_etapa = :etapa
              AND i.asignatura_grupo = :grupo
              AND i.asignatura_cursoAcademico = :cursoAcademico
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
               AND a.cursoAcademico = i.asignatura_cursoAcademico
              JOIN bloque b
                ON b.id = a.bloque_id
              WHERE i.asignatura_curso = :curso
                AND i.asignatura_etapa = :etapa
                AND i.asignatura_grupo = :optativas
                AND i.asignatura_cursoAcademico = :cursoAcademico
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
              AND r.cursoAcademico = :cursoAcademico
          ), 0) AS total
        """, nativeQuery = true)
    Long obtenerTotalHorasAsignadas(@Param("cursoAcademico") String cursoAcademico,
                                   @Param("curso") Integer curso, 
                                   @Param("etapa") String etapa, 
                                   @Param("grupo") String grupo,
                                   @Param("sinGrupo") String sinGrupo,
                                   @Param("optativas") String optativas);

    /**
     * Método helper que llama a obtenerTotalHorasAsignadas con las constantes
     * @param cursoAcademico el curso académico activo
     * @param curso el curso del grupo
     * @param etapa la etapa del grupo
     * @param grupo el grupo específico
     * @return el total de horas asignadas (asignaturas + reducciones)
     */
    default Long obtenerTotalHorasAsignadas(String cursoAcademico, Integer curso, String etapa, String grupo)
    {
        return obtenerTotalHorasAsignadas(cursoAcademico, curso, etapa, grupo, Constants.SIN_GRUPO_ASIGNADO, Constants.GRUPO_OPTATIVAS);
    }
}
