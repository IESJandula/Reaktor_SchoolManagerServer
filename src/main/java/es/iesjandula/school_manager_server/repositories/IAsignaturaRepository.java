package es.iesjandula.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.school_manager_server.models.Asignatura;
import es.iesjandula.school_manager_server.models.ids.IdAsignatura;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite realizar operaciones CRUD sobre la tabla que representa
 * la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 * El uso de esta interfaz facilita la persistencia y la manipulación de objetos {@link Asignatura} en la base de datos,
 * sin necesidad de implementar manualmente los métodos.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz utiliza {@link IdAsignatura} como tipo para la clave primaria de la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IAsignaturaRepository extends JpaRepository<Asignatura, IdAsignatura>
{
	
	@Query("SELECT a FROM Asignatura a WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa")
	List<Asignatura> findByCursoAndEtapa(@Param("curso") int curso, @Param("etapa") String etapa) ;
	
	@Query("SELECT a FROM Asignatura a WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.nombre IN :nombres")
	List<Asignatura> findAsignaturasByCursoEtapaAndNombres(
	    @Param("curso") int curso, 
	    @Param("etapa") String etapa, 
	    @Param("nombres") List<String> nombres
	);
	
	@Query("SELECT a FROM Asignatura a WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa AND a.idAsignatura.nombre = :nombre")
	Optional<Asignatura> findAsignaturasByCursoEtapaAndNombre(
	    @Param("curso") int curso, 
	    @Param("etapa") String etapa, 
	    @Param("nombre") String nombre
	);

	@Transactional
	@Modifying
	@Query("DELETE FROM Asignatura a WHERE a.idAsignatura.curso = :curso AND a.idAsignatura.etapa = :etapa")
	void borrarPorCursoYEtapa(@Param("curso") int curso, @Param("etapa") String etapa) ;
	
	/*
	Optional<Asignatura> findByCursoAndEtapaAndNombre(
		    @Param("curso") int curso, 
		    @Param("etapa") String etapa, 
		    @Param("nombre") String nombre
		);
	*/
	
	
}
