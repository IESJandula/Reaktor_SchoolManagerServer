package es.iesjandula.reaktor.school_manager_server.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
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

        if (cursosEtapas.isPresent())
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Cursos y etapas sin grupos asignados") ;

            // Recorremos la lista de cursos/etapas
            for (CursoEtapa cursoEtapa : cursosEtapas.get())
            {   
                erroresDatosDto.getValoresImplicados().add(cursoEtapa.getCursoEtapaString()) ;
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

        if (departamentos.isPresent())
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Departamentos con plantilla de profesores incorrecta") ;

            // Recorremos la lista de departamentos para avisarlas al usuario
            for (Departamento departamento : departamentos.get())
            {
                erroresDatosDto.getValoresImplicados().add(departamento.getNombre()) ;
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

        if (asignaturas.isPresent())
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Asignaturas sin grupos asignados") ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturas.get())
            {   
                erroresDatosDto.getValoresImplicados().add(asignatura.getIdAsignatura().getNombre()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }

        // Validamos si las asignaturas tienen departamentos asociados
        Optional<List<Asignatura>> asignaturasSinDepartamentos = this.asignaturaRepository.asignaturaSinDepartamentos() ;

        if (asignaturasSinDepartamentos.isPresent())
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Asignaturas sin departamentos asociados") ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturasSinDepartamentos.get())
            {   
                erroresDatosDto.getValoresImplicados().add(asignatura.getIdAsignatura().getNombre()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
        
        // Validamos si las asignaturas tienen horas de clase
        Optional<List<Asignatura>> asignaturasSinHorasDeClase = this.asignaturaRepository.asignaturaSinHorasDeClase() ;

        if (asignaturasSinHorasDeClase.isPresent())
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Asignaturas sin horas de clase") ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturasSinHorasDeClase.get())
            {
                erroresDatosDto.getValoresImplicados().add(asignatura.getIdAsignatura().getNombre()) ;
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
        // Validamos si hay profesores con la suma de horas de docencia y reducciones incorrectas
        Optional<List<Profesor>> profesores = this.profesorRepository.profesorConSumaHorasDocenciaReduccionesIncorrectas() ;

        if (profesores.isPresent())
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Profesores con suma de horas de docencia y reducciones incorrecta") ;

            // Recorremos la lista de profesores para avisarlas al usuario
            for (Profesor profesor : profesores.get())
            {
                erroresDatosDto.getValoresImplicados().add(profesor.getEmail()) ;
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
        Optional<List<CursoEtapaGrupo>> cursosEtapasGrupos = this.impartirRepository.cursoConHorasAsignadasIncorrectas() ;

        if (cursosEtapasGrupos.isPresent())
        {
            ErroresDatosDto erroresDatosDto = new ErroresDatosDto("Grupos sin 30 horas a la semana") ;

            // Recorremos la lista de cursos para avisarlas al usuario
            for (CursoEtapaGrupo cursoEtapaGrupo : cursosEtapasGrupos.get())
            {
                erroresDatosDto.getValoresImplicados().add(cursoEtapaGrupo.getCursoEtapaGrupoString()) ;
            }

            // Añadimos a la instancia del DTO el mensaje de error
            validadorDatosDto.getErroresDatos().add(erroresDatosDto) ;
        }
    }
}
