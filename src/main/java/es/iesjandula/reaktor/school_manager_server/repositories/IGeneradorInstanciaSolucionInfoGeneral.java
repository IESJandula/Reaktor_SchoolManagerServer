package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstanciaSolucionInfoGeneral;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorInstanciaSolucionInfoGeneral;

@Repository
public interface IGeneradorInstanciaSolucionInfoGeneral extends JpaRepository<GeneradorInstanciaSolucionInfoGeneral, IdGeneradorInstanciaSolucionInfoGeneral>
{

    /**
     * Método que busca las puntuaciones generales de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     * @return List con las puntuaciones generales
     */
    @Query("SELECT gisig FROM GeneradorInstanciaSolucionInfoGeneral gisig WHERE gisig.idGeneradorInstanciaSolucionInfoGeneral.generadorInstancia.id = :id")
    Optional<List<GeneradorInstanciaSolucionInfoGeneral>> buscarPorGeneradorInstancia(int id);

    /**
     * Método que borra todas las puntuaciones generales de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     */
    @Modifying
    @Query("DELETE FROM GeneradorInstanciaSolucionInfoGeneral gisig WHERE gisig.idGeneradorInstanciaSolucionInfoGeneral.generadorInstancia.id = :id")
    void borrarPorGeneradorInstanciaId(int id);

}
