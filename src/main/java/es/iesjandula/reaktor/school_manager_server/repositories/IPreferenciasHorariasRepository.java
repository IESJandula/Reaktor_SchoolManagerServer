package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.models.PreferenciasHorariasProfesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdPreferenciasHorariasProfesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPreferenciasHorariasRepository extends JpaRepository<PreferenciasHorariasProfesor, IdPreferenciasHorariasProfesor>
{
    @Query("SELECT p " +
            "FROM PreferenciasHorariasProfesor p " +
            "WHERE p.idPreferenciasHorariasProfesor.profesor.email = :email " + 
            "ORDER BY p.idPreferenciasHorariasProfesor.idSeleccion ASC")
    Optional<List<PreferenciasHorariasProfesor>> buscarPorEmail(@Param("email") String email);

    @Query("SELECT p " +
            "FROM PreferenciasHorariasProfesor p " +
            "WHERE p.idPreferenciasHorariasProfesor.profesor.email = :email AND p.idPreferenciasHorariasProfesor.idSeleccion = :idSeleccion")
    Optional<PreferenciasHorariasProfesor> buscarPorEmailIdSeleccion(@Param("email") String email, @Param("idSeleccion") Integer idSeleccion);
}
