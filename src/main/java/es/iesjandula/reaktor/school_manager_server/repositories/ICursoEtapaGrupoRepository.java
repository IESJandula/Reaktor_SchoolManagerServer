package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;

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


    @Query("SELECT c "
            + "FROM CursoEtapaGrupo c "
            + "WHERE c.idCursoEtapaGrupo.grupo <> '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    public List<CursoEtapaGrupo> buscarTodosLosCursosEtapasGrupos();

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

    


}
