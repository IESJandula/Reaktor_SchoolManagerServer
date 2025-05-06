package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.models.PreferenciasHorariasProfesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdPreferenciasHorariasProfesor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPreferenciasHorariasRepository extends JpaRepository<PreferenciasHorariasProfesor, IdPreferenciasHorariasProfesor>
{

}
