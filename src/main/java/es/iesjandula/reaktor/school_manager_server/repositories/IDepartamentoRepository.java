package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.school_manager_server.dtos.DepartamentoDto;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDepartamento;
import jakarta.transaction.Transactional;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Departamento}.
 * ----------------------------------------------------------------------------------------------------------------------
 * Esta interfaz extiende {@link JpaRepository}, lo que facilita la ejecución de operaciones CRUD sobre la tabla correspondiente
 * a la entidad {@link Departamento}. La clave primaria es compuesta {@link IdDepartamento} {@code (cursoAcademico, nombre)}.
 * ----------------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IDepartamentoRepository extends JpaRepository<Departamento, IdDepartamento>
{

	/**
     * Busca un Departamento global (matrículas/horarios/profesores) por su nombre.
     * <p>
     * Se filtra por {@code cursoAcademico = ''} ({@link es.iesjandula.reaktor.school_manager_server.utils.Constants#CURSO_ACADEMICO_GLOBAL})
     * para evitar resultados no únicos cuando existen filas de catálogo del mismo nombre en distintos cursos académicos.
     * </p>
     * @param nombre Nombre del departamento.
     * @return Departamento global encontrado o vacío si no existe.
     */
    @Query("SELECT d FROM Departamento d WHERE d.cursoAcademico = '' AND d.nombre = :nombre")
    Optional<Departamento> findByNombre(@Param("nombre") String nombre);

    /**
     * Obtiene el catálogo de departamentos de un curso académico en formato DTO.
     *
     * @param cursoAcademico - El curso académico.
     * @return La lista de departamentos del curso académico.
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.DepartamentoDto(d.nombre) " +
           "FROM Departamento d " +
           "WHERE d.cursoAcademico = :cursoAcademico " +
           "ORDER BY d.nombre ASC")
    List<DepartamentoDto> findAllDtoByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

    /**
     * Borra todos los departamentos de un curso académico (catálogo). No afecta a los departamentos globales.
     *
     * @param cursoAcademico - El curso académico.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Departamento d WHERE d.cursoAcademico = :cursoAcademico")
    void deleteAllByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

    /**
     * Método que busca los departamentos globales con número de profesores en plantilla incorrecto
     * @return - Lista de departamentos globales con número de profesores en plantilla incorrecto
     */
    @Query("SELECT d FROM Departamento d " +
           "WHERE d.cursoAcademico = '' AND (" +
           "      d.plantilla = 0 OR " +
           "      d.plantilla != (" +
           "   SELECT COUNT(p) FROM Profesor p " +
           "   WHERE p.departamento = d))")
    Optional<List<Departamento>> departamentoConNumeroProfesoresEnPlantillaIncorrecto();
}
