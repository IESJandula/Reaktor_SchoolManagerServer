package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link CursoEtapa}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla 
 * correspondiente a la entidad {@link CursoEtapa}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface ICursoEtapaRepository extends JpaRepository<CursoEtapa, IdCursoEtapa>
{	
	/**
	 * Método que busca todos los cursos/etapas/grupos
	 * @return - Lista de cursos/etapas/grupos
	 */
    @Query("SELECT ce FROM CursoEtapa ce " +
           "WHERE EXISTS (" +
           "   SELECT 1 FROM CursoEtapaGrupo ceg2 " +
           "   WHERE ceg2.idCursoEtapaGrupo.curso = ce.idCursoEtapa.curso AND " +
           "         ceg2.idCursoEtapaGrupo.etapa = ce.idCursoEtapa.etapa " +
           "   GROUP BY ceg2.idCursoEtapaGrupo.curso, ceg2.idCursoEtapaGrupo.etapa " +
           "   HAVING COUNT(ceg2) = 1)")
	Optional<List<CursoEtapa>> buscarCursosEtapasSinCursosEtapasGrupo() ;
}
