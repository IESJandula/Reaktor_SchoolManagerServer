package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstanciaSolucionInfoProfesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorInstanciaSolucionInfoProfesor;

@Repository
public interface IGeneradorInstanciaSolucionInfoProfesor extends JpaRepository<GeneradorInstanciaSolucionInfoProfesor, IdGeneradorInstanciaSolucionInfoProfesor>
{
    /**
     * Método que busca las puntuaciones de profesores de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     * @return List con las puntuaciones de profesores
     */
    @Query("SELECT gisip FROM GeneradorInstanciaSolucionInfoProfesor gisip WHERE gisip.idGeneradorInstanciaSolucionInfoProfesor.generadorInstancia.id = :id")
    Optional<List<GeneradorInstanciaSolucionInfoProfesor>> buscarPorGeneradorInstancia(int id);

    /**
     * Método que borra todas las puntuaciones de profesores de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM GeneradorInstanciaSolucionInfoProfesor gisip WHERE gisip.idGeneradorInstanciaSolucionInfoProfesor.generadorInstancia.id = :id")
    void borrarPorIdGeneradorInstancia(Integer id);
}
