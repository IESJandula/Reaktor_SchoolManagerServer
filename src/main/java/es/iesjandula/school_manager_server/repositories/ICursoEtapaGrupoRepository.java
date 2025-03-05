package es.iesjandula.school_manager_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.school_manager_server.models.ids.IdCursoEtapaGrupo;

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
     * Cuenta el número de elementos en la tabla {@link CursoEtapaGrupo} que corresponden a un curso y etapa 
     * específicos.
     * 
     * @param curso - El curso para el que se desea contar los registros.
     * @param etapa - La etapa para la que se desea contar los registros.
     * @return Número de registros que coinciden con el curso y etapa proporcionados.
     */
    @Query("SELECT COUNT(c) FROM CursoEtapaGrupo c WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public int findCountByCursoAndEtapa
    (
        @Param("curso") int curso, 
        @Param("etapa") String etapa
    );
    
    /**
     * Obtiene una lista de los grupos que corresponden a un curso y etapa específicos.
     * 
     * @param curso 		- El curso para el que se desea obtener los grupos.
     * @param etapa 		- La etapa para la que se desea obtener los grupos.
     * @return List<String> - Los grupos correspondientes al curso y etapa proporcionados.
     */
    @Query("SELECT c.idCursoEtapaGrupo.grupo FROM CursoEtapaGrupo c WHERE c.idCursoEtapaGrupo.curso = :curso AND c.idCursoEtapaGrupo.etapa = :etapa")
    public List<String> findGrupoByCursoAndEtapa
    (
        @Param("curso") int curso,
        @Param("etapa") String etapa
    );
}
