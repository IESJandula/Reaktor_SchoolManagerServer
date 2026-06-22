package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.*;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;

/**
 * Interfaz que define los métodos para acceder y manipular los datos de la entidad {@link Asignatura}.
 * -----------------------------------------------------------------------------------------------------------------
 * Todas las consultas del conjunto de trabajo se filtran por el curso académico activo ({@code cursoAcademico}),
 * que el backend resuelve desde {@code seleccionado = true}. De este modo cada curso académico mantiene sus propias
 * asignaturas aisladas del resto.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Repository
public interface IAsignaturaRepository extends JpaRepository<Asignatura, IdAsignatura>
{

    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDto(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo" +
            ".idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas, a.bloqueId.id, a.sinDocencia, a.desdoble) "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa "
            + "GROUP BY a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas, a.bloqueId.id, a.sinDocencia," +
            " a.desdoble")
    List<AsignaturaDto> findByCursoAndEtapa(@Param("cursoAcademico") String cursoAcademico,
                                            @Param("curso") Integer curso,
                                            @Param("etapa") String etapa);

    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturasUnicasDto(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas) "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa "
            + "GROUP BY a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas")
    List<AsignaturasUnicasDto> findByCursoAndEtapaDistinct(@Param("cursoAcademico") String cursoAcademico,
                                                           @Param("curso") Integer curso,
                                                           @Param("etapa") String etapa);

    /**
     * Obtiene, para todo el curso académico activo, las asignaturas únicas (curso, etapa, nombre, horas).
     * Se usa en el listado de la configuración básica al elegir la categoría "Asignaturas".
     *
     * @param cursoAcademico - El curso académico activo.
     * @return La lista de asignaturas únicas del curso académico ordenadas por curso, etapa y nombre.
     */
    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturasUnicasDto(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, a.idAsignatura.nombre, a.horas) "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico "
            + "ORDER BY a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso ASC, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa ASC, a.idAsignatura.nombre ASC")
    List<AsignaturasUnicasDto> findAsignaturasUnicasByCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre")
    List<Asignatura> findAsignaturasByCursoEtapaAndNombre(@Param("cursoAcademico") String cursoAcademico,
                                                          @Param("curso") int curso,
                                                          @Param("etapa") String etapa,
                                                          @Param("nombre") String nombre);

    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<Asignatura> findAllByCursoAndEtapa(@Param("cursoAcademico") String cursoAcademico,
                                            @Param("curso") int curso,
                                            @Param("etapa") String etapa);

    @Transactional
    @Modifying
    @Query("DELETE "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    void borrarPorCursoYEtapa(@Param("cursoAcademico") String cursoAcademico,
                              @Param("curso") int curso,
                              @Param("etapa") String etapa);

    /**
     * Borra TODAS las asignaturas del curso académico activo (todas sus combinaciones de curso/etapa/grupo). Se usa
     * en el "borrar todos" del catálogo de asignaturas de la configuración básica.
     *
     * @param cursoAcademico curso académico cuyas asignaturas se borran.
     */
    @Transactional
    @Modifying
    @Query("DELETE "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico")
    void borrarTodasPorCursoAcademico(@Param("cursoAcademico") String cursoAcademico);

    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre "
            + "AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo ")
    Optional<Asignatura> encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(@Param("cursoAcademico") String cursoAcademico,
                                                                        @Param("curso") int curso,
                                                                        @Param("etapa") String etapa,
                                                                        @Param("nombre") String nombre,
                                                                        @Param("grupo") String grupo);

    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre "
            + "AND (a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo "
            + "OR a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = '"
            + Constants.SIN_GRUPO_ASIGNADO + "')")
    Optional<Asignatura> encontrarAsignaturaPorNombreYCursoYEtapaYGrupoOSinEl(@Param("cursoAcademico") String cursoAcademico,
                                                                              @Param("curso") int curso,
                                                                              @Param("etapa") String etapa,
                                                                              @Param("nombre") String nombre,
                                                                              @Param("grupo") String grupo);

    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre "
            + "AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = '" + Constants.GRUPO_OPTATIVAS + "'")
    Optional<Asignatura> encontrarAsignaturaPorNombreYCursoYEtapaYGrupoOptativas(@Param("cursoAcademico") String cursoAcademico,
                                                                                 @Param("curso") int curso,
                                                                                 @Param("etapa") String etapa,
                                                                                 @Param("nombre") String nombre);

    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombres")
    List<Asignatura> findNombreByCursoEtapaAndNombres(@Param("cursoAcademico") String cursoAcademico,
                                                      @Param("curso") int curso,
                                                      @Param("etapa") String etapa,
                                                      @Param("nombres") String nombres);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaHorasDto(a.idAsignatura.nombre, a.horas) "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<AsignaturaHorasDto> findNombreAndHorasByCursoEtapa(@Param("cursoAcademico") String cursoAcademico,
                                                            @Param("curso") Integer curso,
                                                            @Param("etapa") String etapa);

    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaSinGrupoDto(a.horas, a.esoBachillerato, a.sinDocencia, a.desdoble , a.bloqueId.id ) "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombre")
    AsignaturaSinGrupoDto encontrarPorCursoYEtapaYNombre(@Param("cursoAcademico") String cursoAcademico,
                                                         @Param("curso") int curso,
                                                         @Param("etapa") String etapa,
                                                         @Param("nombre") String nombre);

    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo "
            + "AND a.departamentoPropietario is null AND a.sinDocencia = false")
    List<Asignatura> asignaturasPorCursoEtapaGrupo(@Param("cursoAcademico") String cursoAcademico,
                                                   @Param("curso") int curso,
                                                   @Param("etapa") String etapa,
                                                   @Param("grupo") String grupo);


    @Query("SELECT a "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = :grupo "
            + "AND a.idAsignatura.nombre = :nombre")
    Optional<Asignatura> findAsignaturasByCursoEtapaGrupoAndNombre(@Param("cursoAcademico") String cursoAcademico,
                                                                   @Param("curso") int curso,
                                                                   @Param("etapa") String etapa,
                                                                   @Param("grupo") String grupo,
                                                                   @Param("nombre") String nombre);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaConDepartamentoDto(" +
            "a.departamentoReceptor.nombre, " +
            "a.departamentoReceptor.plantilla, " +
            "SUM(a.horas), " +
            "(depto.plantilla * :horasLectivasProfesor), " +
            "((depto.plantilla * :horasLectivasProfesor) - SUM(a.horas)) as desfase) " +
            "FROM Asignatura a " +
            "JOIN a.departamentoReceptor depto " +
            "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico " +
            "GROUP BY a.departamentoReceptor.nombre, depto.plantilla")
    List<AsignaturaConDepartamentoDto> encontrarAsignaturasConDepartamento(@Param("cursoAcademico") String cursoAcademico,
                                                                           @Param("horasLectivasProfesor") long horasLectivasProfesor);

    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.HorasYBloquesDto(a.horas, a.bloqueId.id, a.sinDocencia, a.desdoble) "
            + "FROM Asignatura a "
            + "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.idAsignatura.nombre = :nombres")
    Optional<HorasYBloquesDto> encontrarAsignaturaPorCursoEtapaNombre(@Param("cursoAcademico") String cursoAcademico,
                                                                      @Param("curso") int curso,
                                                                      @Param("etapa") String etapa,
                                                                      @Param("nombres") String nombres);

    @Query("SELECT a.bloqueId.id " +
            "FROM Asignatura a " +
            "WHERE a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<Long> encontrarBloquePorCursoEtapa(@Param("cursoAcademico") String cursoAcademico,
                                            @Param("curso") int curso,
                                            @Param("etapa") String etapa);

    /**
     * Obtiene, para un curso/etapa del curso académico activo, las parejas bloque-asignatura de todas las asignaturas
     * que pertenecen a un bloque. Se usa para mostrar, en el paso de creación de grupos, los bloques con sus
     * asignaturas y poder asignarles un aula de desdoble.
     *
     * @param cursoAcademico - El curso académico activo.
     * @param curso          - El curso.
     * @param etapa          - La etapa.
     * @return La lista de parejas (id de bloque, nombre de asignatura).
     */
    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.BloqueAsignaturaDto(a.bloqueId.id, a.idAsignatura.nombre) " +
            "FROM Asignatura a " +
            "WHERE a.bloqueId IS NOT NULL AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa " +
            "ORDER BY a.bloqueId.id, a.idAsignatura.nombre")
    List<es.iesjandula.reaktor.school_manager_server.dtos.BloqueAsignaturaDto> encontrarBloquesConAsignaturasPorCursoEtapa(@Param("cursoAcademico") String cursoAcademico,
                                                                                                                          @Param("curso") int curso,
                                                                                                                          @Param("etapa") String etapa);

    @Query("SELECT a " +
            "FROM Asignatura a " +
            "WHERE a.bloqueId = :#{#asignatura.bloqueId} AND a != :asignatura")
    List<Asignatura> buscaOptativasRelacionadas(@Param("asignatura") Asignatura asignatura);

    /**
     * Indica si una asignatura (por nombre) pertenece a un bloque concreto. Se usa al asignar el aula de desdoble
     * por asignatura, para validar que la asignatura elegida realmente forma parte del bloque indicado.
     *
     * @param bloqueId   - El identificador del bloque.
     * @param asignatura - El nombre de la asignatura.
     * @return true si existe una asignatura con ese nombre en ese bloque, false en caso contrario.
     */
    @Query("SELECT COUNT(a) > 0 FROM Asignatura a " +
            "WHERE a.bloqueId.id = :bloqueId AND a.idAsignatura.nombre = :asignatura")
    boolean existeAsignaturaEnBloque(@Param("bloqueId") Long bloqueId, @Param("asignatura") String asignatura);

    /**
     * Cuenta el número de asignaturas (DISTINTAS por nombre) que componen un bloque. Se usa como TOPE de aulas de
     * desdoble: no puede haber más aulas asignadas a un bloque que asignaturas tiene.
     *
     * @param bloqueId - El identificador del bloque.
     * @return El número de asignaturas distintas del bloque.
     */
    @Query("SELECT COUNT(DISTINCT a.idAsignatura.nombre) FROM Asignatura a WHERE a.bloqueId.id = :bloqueId")
    long contarAsignaturasDeBloque(@Param("bloqueId") Long bloqueId);

    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.ImpartirAsignaturaDto(a.idAsignatura.nombre, a.horas, a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, a" +
            ".idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa)" +
            "FROM Asignatura a " +
            "WHERE a.departamentoReceptor.nombre = :departamento AND a.sinDocencia = false AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico ")
    List<ImpartirAsignaturaDto> encontrarAsignaturasPorDepartamento(@Param("cursoAcademico") String cursoAcademico, @Param("departamento") String departamento);

    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.GrupoAsignaturaDto(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
            "FROM Asignatura a " +
            "WHERE a.idAsignatura.nombre = :nombre AND a.horas = :horas AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND  a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<GrupoAsignaturaDto> encontrarGrupoPorNombreAndHorasAndCursoAndEtapa(@Param("cursoAcademico") String cursoAcademico,
                                                                             @Param("nombre") String nombre,
                                                                             @Param("horas") Integer horas,
                                                                             @Param("curso") Integer curso,
                                                                             @Param("etapa") String etapa);

    @Query("SELECT a " +
            "FROM Asignatura a " +
            "WHERE a.idAsignatura.nombre = :nombre AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<Asignatura> encontrarAsignaturaPorNombre(@Param("cursoAcademico") String cursoAcademico,
                                                  @Param("curso") Integer curso,
                                                  @Param("etapa") String etapa,
                                                  @Param("nombre") String nombre);

    @Query("SELECT COUNT(DISTINCT a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
            "FROM Asignatura a " +
            "WHERE a.idAsignatura.nombre = :nombre " +
            "AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    Long contarGruposPorAsignatura(@Param("cursoAcademico") String cursoAcademico,
                                   @Param("nombre") String nombre,
                                   @Param("curso") Integer curso,
                                   @Param("etapa") String etapa);

    @Query("SELECT DISTINCT a.desdoble " +
            "FROM Asignatura  a " +
            "WHERE a.idAsignatura.nombre = :nombre " +
            "AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    Boolean isDesdoble(@Param("cursoAcademico") String cursoAcademico,
                       @Param("nombre") String nombre,
                       @Param("curso") Integer curso,
                       @Param("etapa") String etapa);

    @Query("SELECT DISTINCT new es.iesjandula.reaktor.school_manager_server.dtos.DepartamentoDto(a.departamentoReceptor.nombre) " +
            "FROM Asignatura a " +
            "WHERE a.idAsignatura.nombre = :nombre " +
            "AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa")
    List<DepartamentoDto> encontrarDepartamentoReceptor(@Param("cursoAcademico") String cursoAcademico,
                                               @Param("nombre") String nombre,
                                               @Param("curso") Integer curso,
                                               @Param("etapa") String etapa);

    @Query("SELECT COUNT(a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
            "FROM Asignatura a " +
            "WHERE a.idAsignatura.nombre = :nombre " +
            "AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso = :curso AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa = :etapa AND a.departamentoReceptor.nombre = :departamento")
    Long contarGruposPorNombreCursoEtapaDepartamento(@Param("cursoAcademico") String cursoAcademico,
                                                     @Param("nombre") String nombre,
                                                     @Param("curso") Integer curso,
                                                     @Param("etapa") String etapa,
                                                     @Param("departamento") String departamento);

    /**
     * Método que busca las asignaturas del curso académico activo sin cursos/etapas/grupos asignados
     * @param cursoAcademico - El curso académico activo.
     * @return - Lista de asignaturas sin cursos/etapas/grupos asignados
     */
    @Query("SELECT a FROM Asignatura a " +
           "WHERE a.sinDocencia = false AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo = '" + Constants.SIN_GRUPO_ASIGNADO + "'")
    Optional<List<Asignatura>> asignaturaSinCursoEtapaGrupo(@Param("cursoAcademico") String cursoAcademico);

    /**
     * Método que busca las asignaturas del curso académico activo sin departamentos asociados
     * @param cursoAcademico - El curso académico activo.
     * @return - Lista de asignaturas sin departamentos asociados
     */
    @Query("SELECT a FROM Asignatura a " +
           "WHERE a.sinDocencia = false AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND (a.departamentoPropietario IS NULL OR a.departamentoReceptor IS NULL)")
    Optional<List<Asignatura>> asignaturaSinDepartamentos(@Param("cursoAcademico") String cursoAcademico);

    /**
     * Método que busca las asignaturas del curso académico activo sin horas de clase
     * @param cursoAcademico - El curso académico activo.
     * @return - Lista de asignaturas sin horas de clase
     */
    @Query("SELECT a FROM Asignatura a " +
           "WHERE a.sinDocencia = false AND a.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.cursoAcademico = :cursoAcademico AND a.horas = 0")
    Optional<List<Asignatura>> asignaturaSinHorasDeClase(@Param("cursoAcademico") String cursoAcademico);


}
