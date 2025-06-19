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
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
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
    public List<String> validacionDatos()
    {
        List<String> mensajesError = new ArrayList<String>() ;

        // Validaciones previas en cursos y etapas
        this.validacionDatosCursosEtapas(mensajesError) ;

        // Validaciones previas en departamentos
        this.validacionDatosDepartamentos(mensajesError) ;

        // Validaciones previas en asignaturas
        this.validacionDatosAsignaturas(mensajesError) ;

        // Validaciones previas en profesores
        this.validacionDatosProfesores(mensajesError) ;

        // Validaciones previas en impartir
        this.validacionDatosImpartir(mensajesError) ;

        return mensajesError ;
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param mensajesError - Lista de mensajes de error
     */
    private void validacionDatosCursosEtapas(List<String> mensajesError)
    {
        // Validamos si los hay cursos/etapas/grupos por cada curso/etapa (Consulta 1)
        Optional<List<CursoEtapa>> cursosEtapas = this.cursoEtapaRepository.buscarCursosEtapasSinCursosEtapasGrupo1() ;

        if (cursosEtapas.isPresent())
        {
            String mensajeError = "Los siguientes cursos/etapas no tienen cursos/etapas/grupos asignados: " ;

            // Recorremos la lista de cursos/etapas para avisarlas al usuario
            for (CursoEtapa cursoEtapa : cursosEtapas.get())
            {   
                mensajeError += cursoEtapa.getCursoEtapaString() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }

        // Validamos si los hay cursos/etapas/grupos por cada curso/etapa (Consulta 2)
        cursosEtapas = this.cursoEtapaRepository.buscarCursosEtapasSinCursosEtapasGrupo2() ;

        if (cursosEtapas.isPresent())
        {
            String mensajeError = "Los siguientes cursos/etapas no tienen cursos/etapas/grupos asignados (Consulta 2): " ;

            // Recorremos la lista de cursos/etapas para avisarlas al usuario
            for (CursoEtapa cursoEtapa : cursosEtapas.get())
            {   
                mensajeError += cursoEtapa.getCursoEtapaString() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }
    }
    
    /**
     * Método que realiza una serie de validaciones previas
     * @param mensajesError - Lista de mensajes de error
     */
    private void validacionDatosDepartamentos(List<String> mensajesError)
    {
        // Validamos si hay departamentos con número de profesores en plantilla incorrecto
        Optional<List<Departamento>> departamentos = this.departamentoRepository.departamentoConNumeroProfesoresEnPlantillaIncorrecto() ;

        if (departamentos.isPresent())
        {
            String mensajeError = "Los siguientes departamentos tienen un número de profesores en plantilla incorrecto: " ;

            // Recorremos la lista de departamentos para avisarlas al usuario
            for (Departamento departamento : departamentos.get())
            {
                mensajeError += departamento.getNombre() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param mensajesError - Lista de mensajes de error
     */
    private void validacionDatosAsignaturas(List<String> mensajesError)
    {
        // Validamos si hay asignaturas sin cursos/etapas/grupos asignados (Consulta 1)
        Optional<List<Asignatura>> asignaturas = this.asignaturaRepository.asignaturaSinCursoEtapaGrupo() ;

        if (asignaturas.isPresent())
        {
            String mensajeError = "Las siguientes asignaturas no tienen cursos/etapas/grupos asignados (Consulta 1): " ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturas.get())
            {   
                mensajeError += asignatura.getIdAsignatura().getNombre() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }

        // Validamos si las asignaturas tienen departamentos asociados
        Optional<List<Asignatura>> asignaturasSinDepartamentos = this.asignaturaRepository.asignaturaSinDepartamentos() ;

        if (asignaturasSinDepartamentos.isPresent())
        {
            String mensajeError = "Las siguientes asignaturas no tienen departamentos asociados: " ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturasSinDepartamentos.get())
            {   
                mensajeError += asignatura.getIdAsignatura().getNombre() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }
        
        // Validamos si las asignaturas tienen horas de clase
        Optional<List<Asignatura>> asignaturasSinHorasDeClase = this.asignaturaRepository.asignaturaSinHorasDeClase() ;

        if (asignaturasSinHorasDeClase.isPresent())
        {
            String mensajeError = "Las siguientes asignaturas no tienen horas de clase: " ;

            // Recorremos la lista de asignaturas para avisarlas al usuario
            for (Asignatura asignatura : asignaturasSinHorasDeClase.get())
            {
                mensajeError += asignatura.getIdAsignatura().getNombre() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param mensajesError - Lista de mensajes de error
     */
    private void validacionDatosProfesores(List<String> mensajesError)
    {
        // Validamos si hay profesores con la suma de horas de docencia y reducciones incorrectas
        Optional<List<Profesor>> profesores = this.profesorRepository.profesorConSumaHorasDocenciaReduccionesIncorrectas() ;

        if (profesores.isPresent())
        {
            String mensajeError = "Los siguientes profesores no tienen la suma de horas de docencia y reducciones con el mínimo legal: " ;

            // Recorremos la lista de profesores para avisarlas al usuario
            for (Profesor profesor : profesores.get())
            {
                mensajeError += profesor.getEmail() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }
    }

    /**
     * Método que realiza una serie de validaciones previas
     * @param mensajesError - Lista de mensajes de error
     */
    private void validacionDatosImpartir(List<String> mensajesError)
    {
        // Validamos que todos los cursos tienen 30 horas a la semana asignadas de clase
        Optional<List<CursoEtapaGrupo>> cursosEtapasGrupos = this.impartirRepository.cursoConHorasAsignadasIncorrectas() ;

        if (cursosEtapasGrupos.isPresent())
        {
            String mensajeError = "Los siguientes cursos no tienen 30 horas a la semana asignadas de clase: " ;

            // Recorremos la lista de cursos para avisarlas al usuario
            for (CursoEtapaGrupo cursoEtapaGrupo : cursosEtapasGrupos.get())
            {
                mensajeError += cursoEtapaGrupo.getCursoEtapaGrupoString() + ", " ;
            }

            // Eliminamos la última coma
            mensajeError = mensajeError.substring(0, mensajeError.length() - 2) ;

            // Añadimos el mensaje de error a la lista de mensajes de error
            mensajesError.add(mensajeError) ;
        }
    }
}
