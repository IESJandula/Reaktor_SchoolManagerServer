package es.iesjandula.reaktor.school_manager_server.services;

import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto2;
import es.iesjandula.reaktor.school_manager_server.dtos.MatriculaDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AlumnoService {

    private final IMatriculaRepository iMatriculaRepository;
    private final IAsignaturaRepository iAsignaturaRepository;
    private final IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    public void borrarAlumno(@RequestBody AlumnoDto2 alumnoDto) throws SchoolManagerServerException {

            List<MatriculaDto> listaAlumnosABorrar = iMatriculaRepository.encontrarAlumnoPorNombreYApellidosYGrupo(alumnoDto.getNombre(), alumnoDto.getApellidos(), alumnoDto.getGrupo());

            if(listaAlumnosABorrar.isEmpty())
            {
                String mensajeError = "ERROR - No se encontraron alumnos para borrar";

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_ALUMNOS_ENCONTRADOS, mensajeError);
            }

            // Por cada matricula del Alumno
            for (MatriculaDto matriculaDtoAlumnoABorrar : listaAlumnosABorrar)
            {
                List<Integer> listaIds = iMatriculaRepository.encontrarIdAlumnoPorCursoEtapaGrupoYNombre(matriculaDtoAlumnoABorrar.getCurso(),
                        matriculaDtoAlumnoABorrar.getEtapa(),
                        matriculaDtoAlumnoABorrar.getGrupo(),
                        matriculaDtoAlumnoABorrar.getNombreAsignatura(),
                        matriculaDtoAlumnoABorrar.getNombreAlumno(),
                        matriculaDtoAlumnoABorrar.getApellidosAlumno());

                for (Integer id : listaIds){
//                  Eliminar el registro en la tabla Matricula
                    this.iMatriculaRepository.borrarPorTodo(matriculaDtoAlumnoABorrar.getCurso(),matriculaDtoAlumnoABorrar.getEtapa(),
                            matriculaDtoAlumnoABorrar.getNombreAsignatura(),id);
                }

                //Si la matricula actual de asignatura del alumno es la unica de su grupo
                if (this.iMatriculaRepository.numeroAsignaturasPorNombreYGrupo(matriculaDtoAlumnoABorrar.getNombreAsignatura(), matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa(), matriculaDtoAlumnoABorrar.getGrupo()) == 0){

                    Optional<Asignatura> asignaturaEncontrada = iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa(),
                            matriculaDtoAlumnoABorrar.getNombreAsignatura(), matriculaDtoAlumnoABorrar.getGrupo());

                    iAsignaturaRepository.delete(asignaturaEncontrada.get());
                }


                IdCursoEtapa idCursoEtapa = new IdCursoEtapa(matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa());
                CursoEtapa cursoEtapa = new CursoEtapa(idCursoEtapa,matriculaDtoAlumnoABorrar.isEsoBachillerato());

                List<DatosBrutoAlumnoMatricula> datosBrutoAlumnoMatricula =
                        this.iDatosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(matriculaDtoAlumnoABorrar.getNombreAlumno(),matriculaDtoAlumnoABorrar.getApellidosAlumno(),cursoEtapa);

                for(DatosBrutoAlumnoMatricula datosAlumnoBorrado : datosBrutoAlumnoMatricula)
                {
                    datosAlumnoBorrado.setAsignado(false);
                }

                // Guardar el registro en la tabla DatosBrutoAlumnoMatricula
                this.iDatosBrutoAlumnoMatriculaRepository.saveAllAndFlush(datosBrutoAlumnoMatricula);
            }

    }

}
