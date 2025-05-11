package es.iesjandula.reaktor.school_manager_server.repositories;

import es.iesjandula.reaktor.school_manager_server.models.PreferenciasHorariasProfesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdPreferenciasHorariasProfesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IPreferenciasHorariasRepository extends JpaRepository<PreferenciasHorariasProfesor, IdPreferenciasHorariasProfesor>
{
    @Query("SELECT p " +
            "FROM PreferenciasHorariasProfesor p " +
            "WHERE p.idPreferenciasHorariasProfesor.profesor.email = :email")
    List<PreferenciasHorariasProfesor> encontrarPrefenciasPorEmail(@Param("email") String email);

}
