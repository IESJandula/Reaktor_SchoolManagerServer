package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor.school_manager_server.models.GeneradorInstancia;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import jakarta.transaction.Transactional;

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

    /**
     * Método que deselecciona todas las soluciones
     */
    @Modifying
    @Transactional
    @Query("UPDATE GeneradorInstancia gi SET gi.solucionElegida = false WHERE gi.estado = '" + Constants.ESTADO_GENERADOR_FINALIZADO + "'")
    void deseleccionarSoluciones();

    /**
     * Método que obtiene todas las posibles soluciones
     * @return List con todas las posibles soluciones
     */
    @Query("SELECT gi FROM GeneradorInstancia gi WHERE gi.estado = '" + Constants.ESTADO_GENERADOR_FINALIZADO + "' ORDER BY gi.puntuacion DESC")
    Optional<List<GeneradorInstancia>> obtenerTodasLasPosiblesSoluciones();

    /**
     * Método que obtiene la información de las puntuaciones
     * @param list - List con las posibles soluciones
     * @return Map con la información de las puntuaciones
     */
    @Query("SELECT gi FROM GeneradorInstancia gi WHERE gi.estado = '" + Constants.ESTADO_GENERADOR_FINALIZADO + "' ORDER BY gi.puntuacion DESC")
    Optional<List<GeneradorInstancia>> obtenerInfoPuntuaciones();
}
