package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;

import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la
 * entidad {@link Reduccion}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de
 * operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Reduccion}. La clave primaria de la entidad
 * {@link Reduccion} está compuesta por un {@link String}, que
 * representa el {@code nombre} de la reducción.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IReduccionRepository extends JpaRepository<Reduccion, IdReduccion>
{
	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ReduccionDto(r.idReduccion.nombre, r.idReduccion.horas, r.decideDireccion, r.cursoEtapaGrupo) "
			+ "FROM Reduccion r LEFT JOIN r.cursoEtapaGrupo")
	List<ReduccionDto> encontrarTodasReducciones();

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.ReduccionProfesoresDto(r.idReduccion.nombre, r.idReduccion.horas) " +
			"FROM Reduccion r " +
			"WHERE r.decideDireccion = false ")
	List<ReduccionProfesoresDto> encontrarReduccionesParaProfesores();

}
