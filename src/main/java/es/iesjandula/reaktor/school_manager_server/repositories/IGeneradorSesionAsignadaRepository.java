package es.iesjandula.reaktor.school_manager_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaCursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaProfesorDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.GeneradorSesionAsignada;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdGeneradorSesionAsignada;

@Repository
public interface IGeneradorSesionAsignadaRepository extends JpaRepository<GeneradorSesionAsignada, IdGeneradorSesionAsignada>
{
    /**
     * Método que borra todas las sesiones asignadas de una instancia de GeneradorInstancia
     * @param id - Id de la instancia de GeneradorInstancia
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM GeneradorSesionAsignada gsa WHERE gsa.idGeneradorSesionAsignada.idGeneradorInstancia = :id")
    void borrarPorIdGeneradorInstancia(Integer id);
    
    /**
     * Método que busca todas las sesiones asignadas de un profesor en la instancia elegida como solución
     * @param profesor - Profesor
     * @return - Lista de sesiones asignadas del profesor en la solución elegida
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaProfesorDto(gsa.asignatura.idAsignatura.nombre, " +
                                                                                                          "gsa.diaTramoTipoHorario.diaDesc, " +
                                                                                                          "gsa.diaTramoTipoHorario.tramoDesc, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.horarioMatutino, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.curso, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.etapa, " +
                                                                                                          "gsa.asignatura.idAsignatura.cursoEtapaGrupo.idCursoEtapaGrupo.grupo) " +
           "FROM GeneradorSesionAsignada gsa " +
           "WHERE gsa.profesor = :profesor " +
           "AND gsa.generadorInstancia.solucionElegida = true")
    Optional<List<GeneradorSesionAsignadaProfesorDto>> buscarHorarioProfesorSolucionElegida(Profesor profesor);

    /**
     * Método que busca todas las sesiones asignadas de un curso etapa grupo en la instancia elegida como solución
     * @param cursoEtapaGrupo - Curso etapa grupo
     * @return - Lista de sesiones asignadas del curso etapa grupo en la solución elegida
     */
    @Query("SELECT new es.iesjandula.reaktor.school_manager_server.dtos.GeneradorSesionAsignadaCursoEtapaGrupoDto(gsa.asignatura.idAsignatura.nombre, " +
                                                                                                                 "gsa.profesor.nombre, " +
                                                                                                                 "gsa.profesor.apellidos, " +
                                                                                                                 "gsa.diaTramoTipoHorario.diaDesc, " +
                                                                                                                 "gsa.diaTramoTipoHorario.tramoDesc, " +
                                                                                                                 "gsa.asignatura.idAsignatura.cursoEtapaGrupo.horarioMatutino) " +
           "FROM GeneradorSesionAsignada gsa " +
           "WHERE gsa.asignatura.idAsignatura.cursoEtapaGrupo = :cursoEtapaGrupo " +
           "AND gsa.generadorInstancia.solucionElegida = true")
    Optional<List<GeneradorSesionAsignadaCursoEtapaGrupoDto>> buscarHorarioCursoEtapaGrupoSolucionElegida(CursoEtapaGrupo cursoEtapaGrupo);
}
