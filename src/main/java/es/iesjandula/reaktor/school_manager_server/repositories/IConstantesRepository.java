package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.dtos.DtoConstantes;
import es.iesjandula.reaktor.school_manager_server.models.Constantes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IConstantesRepository extends JpaRepository<Constantes, String>
{
	/**
	 * Búsqueda de constante por clave
	 * 
	 * @param clave clave de la constante
	 * @return constante encontrada
	 */
	Optional<Constantes> findByClave(String clave);

	@Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.DtoConstantes(c.clave, c.valor) "
			+ "FROM Constantes c")
	List<DtoConstantes> encontrarTodoComoDto();

}
