package es.iesjandula.reaktor.school_manager_server.services;

import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto2;
import es.iesjandula.reaktor.school_manager_server.dtos.MatriculaDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AlumnoService
{
    private final IMatriculaRepository iMatriculaRepository;
    private final IAsignaturaRepository iAsignaturaRepository;
    private final IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;
    private final IAlumnoRepository iAlumnoRepository;

    public void borrarAlumno(AlumnoDto2 alumnoDto) throws SchoolManagerServerException
    {
        List<MatriculaDto> listaAlumnosABorrar = iMatriculaRepository.encontrarAlumnoPorNombreYApellidosYGrupo(alumnoDto.getNombre(), alumnoDto.getApellidos(), alumnoDto.getGrupo());
        List<Integer> idAlumnos= new ArrayList<>();

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

            idAlumnos.add(listaIds.get(listaIds.size() - 1)); //Añado el id encontrado a la lista general

            for (Integer id : listaIds)
            {
//                  Eliminar el registro en la tabla Matricula
                this.iMatriculaRepository.borrarPorTodo(matriculaDtoAlumnoABorrar.getCurso(),matriculaDtoAlumnoABorrar.getEtapa(),
                        matriculaDtoAlumnoABorrar.getNombreAsignatura(),id);
            }

            //Si la matricula actual de asignatura del alumno es la unica de su grupo
            if (this.iMatriculaRepository.numeroAsignaturasPorNombreYGrupo(matriculaDtoAlumnoABorrar.getNombreAsignatura(), matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa(), matriculaDtoAlumnoABorrar.getGrupo()) == 0)
            {
                Optional<Asignatura> asignaturaEncontrada = iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(matriculaDtoAlumnoABorrar.getCurso(), matriculaDtoAlumnoABorrar.getEtapa(),
                        matriculaDtoAlumnoABorrar.getNombreAsignatura(), matriculaDtoAlumnoABorrar.getGrupo());

                // Creo la asignatura sin el grupo
                Asignatura asignatura = getAsignatura(matriculaDtoAlumnoABorrar, asignaturaEncontrada);

                this.iAsignaturaRepository.saveAndFlush(asignatura);

                iAsignaturaRepository.delete(asignaturaEncontrada.get());
            }

            //Convertir a false el campo asignacion de los alumnos borrados
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
        List<Integer> listaIdsSinDuplicados = idAlumnos.stream()
            .distinct()
            .toList();
        for(Integer id : listaIdsSinDuplicados)
        {
            iAlumnoRepository.deleteByNombreAndApellidosAndId(alumnoDto.getNombre(), alumnoDto.getApellidos(),id);
        }
    }

    private static Asignatura getAsignatura(MatriculaDto matriculaDtoAlumnoABorrar, Optional<Asignatura> asignaturaEncontrada)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo();
        idCursoEtapaGrupo.setCurso(matriculaDtoAlumnoABorrar.getCurso());
        idCursoEtapaGrupo.setEtapa(matriculaDtoAlumnoABorrar.getEtapa());
        idCursoEtapaGrupo.setGrupo(Constants.SIN_GRUPO_ASIGNADO);

        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

        IdAsignatura idAsignatura = new IdAsignatura();
        idAsignatura.setNombre(matriculaDtoAlumnoABorrar.getNombreAsignatura());
        idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);

        Asignatura asignatura = new Asignatura();
        asignatura.setIdAsignatura(idAsignatura);
        asignatura.setHoras(asignaturaEncontrada.get().getHoras());

        return asignatura;
    }
}
