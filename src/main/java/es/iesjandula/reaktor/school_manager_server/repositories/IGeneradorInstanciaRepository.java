package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;

public interface IGeneradorInstanciaRepository extends JpaRepository<GeneradorInstancia, Integer>
{
    /**
     * Método que busca la máxima puntuación de errores
     * @return Optional con la máxima puntuación de errores
     */
    @Query("SELECT MAX(gi.puntuacion) FROM GeneradorInstancia gi WHERE gi.estado = '" + Constants.ESTADO_GENERADOR_ERROR + "'")
    Optional<Integer> buscarMaximaPuntuacionError();

    /**
     * Método que busca la máxima puntuación de soluciones
     * @return Optional con la máxima puntuación de soluciones
     */
    @Query("SELECT MAX(gi.puntuacion) FROM GeneradorInstancia gi WHERE gi.estado = '" + Constants.ESTADO_GENERADOR_FINALIZADO + "'")
    Optional<Integer> buscarMaximaPuntuacionSolucion();
}
