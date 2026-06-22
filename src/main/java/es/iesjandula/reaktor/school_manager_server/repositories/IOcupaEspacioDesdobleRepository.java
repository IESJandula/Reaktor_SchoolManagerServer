package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.OcupaEspacioDesdoble;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdOcupaEspacioDesdoble;
import jakarta.transaction.Transactional;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link OcupaEspacioDesdoble}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que permite trabajar con la entidad {@link OcupaEspacioDesdoble}
 * y su clave primaria {@link IdOcupaEspacioDesdoble}.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IOcupaEspacioDesdobleRepository extends JpaRepository<OcupaEspacioDesdoble, IdOcupaEspacioDesdoble>
{
	/**
	 * Borra todas las ocupaciones de espacios desdoble asociadas a un curso académico.
	 * <p>
	 * Se utiliza antes de borrar los espacios desdoble del catálogo para no violar la clave foránea.
	 *
	 * @param cursoAcademico - El curso académico cuyas ocupaciones de desdoble se desean borrar.
	 */
	@Transactional
	@Modifying
	@Query("DELETE FROM OcupaEspacioDesdoble o WHERE o.idOcupaEspacioDesdoble.espacioId.cursoAcademico = :cursoAcademico")
	void borrarPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);
}
