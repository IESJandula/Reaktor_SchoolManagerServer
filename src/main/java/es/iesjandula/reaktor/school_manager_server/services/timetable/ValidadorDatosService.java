package es.iesjandula.reaktor.school_manager_server.services.timetable;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.dtos.ErroresDatosDto;
import es.iesjandula.reaktor.school_manager_server.dtos.ValidadorDatosDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidadorDatosService
{
    @Autowired
    private ICursoEtapaRepository cursoEtapaRepository ;

    @Autowired
    private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository ;

    @Autowired
    private IDepartamentoRepository departamentoRepository ;

    @Autowired
    private IAsignaturaRepository asignaturaRepository ;

    @Autowired
    private IProfesorRepository profesorRepository ;

    @Autowired
    private IImpartirRepository impartirRepository ;

    /**
     * Método que realiza una serie de validaciones previas
     */
    public ValidadorDatosDto validacionDatos()
    {
        ValidadorDatosDto validadorDatosDto = new ValidadorDatosDto() ;

        // Validaciones previas en cursos y etapas
        this.validacionDatosCursosEtapas(validadorDatosDto) ;

        // Validaciones previas en departamentos
        this.validacionDatosDepartamentos(validadorDatosDto) ;

        // Validaciones previas en asignaturas
        this.validacionDatosAsignaturas(validadorDatosDto) ;

        // Validaciones previas en profesores
        this.validacionDatosProfesores(validadorDatosDto) ;

        // Validaciones previas en impartir
        this.validacionDatosImpartir(validadorDatosDto) ;

        return validadorDatosDto ;
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param validadorDatosDto - DTO de validador de datos
     */
    private void validacionDatosCursosEtapas(ValidadorDatosDto validadorDatosDto)
    {
        // Validamos si los hay cursos/etapas/grupos por cada curso/etapa (Consulta 1)
        Optional<List<CursoEtapa>> cursosEtapas = this.cursoEtapaRepository.buscarCursosEtapasSinCursosEtapasGrupo() ;

        if (!cursosEtapas.isEmpty() && cursosEtapas.get().size() > 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Cursos y etapas sin grupos asignados") ;

            // Recorremos la lista de cursos/etapas
            for (CursoEtapa cursoEtapa : cursosEtapas.get())
            {   
                erroresDatosDto.agregarValorImplicado(cursoEtapa.getCursoEtapaString()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
    }
    
    /**
     * Método que realiza una serie de validaciones previas
     * @param validadorDatosDto - DTO de validador de datos
     */
    private void validacionDatosDepartamentos(ValidadorDatosDto validadorDatosDto)
    {
        // Validamos si hay departamentos con número de profesores en plantilla incorrecto
        Optional<List<Departamento>> departamentos = this.departamentoRepository.departamentoConNumeroProfesoresEnPlantillaIncorrecto() ;

        if (!departamentos.isEmpty() && departamentos.get().size() > 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Departamentos con plantilla de profesores incorrecta") ;

            // Recorremos la lista de departamentos para avisarlas al usuario
            for (Departamento departamento : departamentos.get())
            {
                erroresDatosDto.agregarValorImplicado(departamento.getNombre()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param validadorDatosDto - DTO de validador de datos
     */
    private void validacionDatosAsignaturas(ValidadorDatosDto validadorDatosDto)
    {
        // Validamos si hay asignaturas sin cursos/etapas/grupos asignados (Consulta 1)
        Optional<List<Asignatura>> asignaturas = this.asignaturaRepository.asignaturaSinCursoEtapaGrupo() ;

        if (!asignaturas.isEmpty() && asignaturas.get().size() > 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Asignaturas sin grupos asignados") ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturas.get())
            {   
                erroresDatosDto.agregarValorImplicado(asignatura.getIdAsignatura().getNombre()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }

        // Validamos si las asignaturas tienen departamentos asociados
        Optional<List<Asignatura>> asignaturasSinDepartamentos = this.asignaturaRepository.asignaturaSinDepartamentos() ;

        if (!asignaturasSinDepartamentos.isEmpty() && asignaturasSinDepartamentos.get().size() > 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Asignaturas sin departamentos asociados") ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturasSinDepartamentos.get())
            {   
                erroresDatosDto.agregarValorImplicado(asignatura.getIdAsignatura().getNombre()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
        
        // Validamos si las asignaturas tienen horas de clase
        Optional<List<Asignatura>> asignaturasSinHorasDeClase = this.asignaturaRepository.asignaturaSinHorasDeClase() ;

        if (!asignaturasSinHorasDeClase.isEmpty() && asignaturasSinHorasDeClase.get().size() > 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Asignaturas sin horas de clase") ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturasSinHorasDeClase.get())
            {
                erroresDatosDto.agregarValorImplicado(asignatura.getIdAsignatura().getNombre()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param validadorDatosDto - DTO de validador de datos
     */
    private void validacionDatosProfesores(ValidadorDatosDto validadorDatosDto)
    {
        // Validamos si existen profesores en la BBDD
        if (this.profesorRepository.count() == 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("No se ha cargado aún la lista de profesores en BBDD") ;

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }

        // Validamos si la tabla impartir está vacía
        if (this.impartirRepository.count() == 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("No se han asignado aún asignaturas a profesores") ;

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }

        // Validamos si hay profesores con la suma de horas de docencia y reducciones incorrectas
        Optional<List<Profesor>> profesores = this.profesorRepository.profesorConSumaHorasDocenciaReduccionesIncorrectas() ;

        if (!profesores.isEmpty() && profesores.get().size() > 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Profesores con suma de horas de docencia y reducciones incorrecta") ;

            // Recorremos la lista de profesores para avisarlas al usuario
            for (Profesor profesor : profesores.get())
            {
                erroresDatosDto.agregarValorImplicado(profesor.getNombre() + " " + profesor.getApellidos()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param validadorDatosDto - DTO de validador de datos
     */
    private void validacionDatosImpartir(ValidadorDatosDto validadorDatosDto)
    {
        // Validamos que todos los cursos tienen 30 horas a la semana asignadas de clase
        Optional<List<CursoEtapaGrupo>> cursosEtapasGrupos = this.cursoEtapaGrupoRepository.cursoConHorasAsignadasIncorrectas() ;

        if (!cursosEtapasGrupos.isEmpty() && cursosEtapasGrupos.get().size() > 0)
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Grupos sin 30 horas a la semana") ;

            // Recorremos la lista de cursos para avisarlas al usuario
            for (CursoEtapaGrupo cursoEtapaGrupo : cursosEtapasGrupos.get())
            {
                Long totalHorasAsignadas = this.cursoEtapaGrupoRepository.obtenerTotalHorasAsignadas(cursoEtapaGrupo.getIdCursoEtapaGrupo().getCurso(), cursoEtapaGrupo.getIdCursoEtapaGrupo().getEtapa(), cursoEtapaGrupo.getIdCursoEtapaGrupo().getGrupo()) ;

                erroresDatosDto.agregarValorImplicado(cursoEtapaGrupo.getCursoEtapaGrupoString() + " - " + totalHorasAsignadas) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
    }
}
