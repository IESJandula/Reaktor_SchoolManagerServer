package es.iesjandula.reaktor.school_manager_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;

@Repository
public interface IGeneradorSesionAsignadaRepository extends JpaRepository<GeneradorSesionAsignada, IdGeneradorSesionAsignada>
{
    /**
     * Método que borra todas las sesiones asignadas de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     */
    @Modifying
    @Query("DELETE FROM GeneradorSesionAsignada gsa WHERE gsa.idGeneradorSesionAsignada.idGeneradorInstancia = :id")
    void borrarPorGeneradorInstanciaId(int id);

}
