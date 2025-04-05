package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import es.iesjandula.reaktor.school_manager_server.dtos.*;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/asignaturasYDepartamentos")
public class Paso5AsignaturasYDepartamentosController
{
    @Autowired
    private IDepartamentoRepository iDepartamentoRepository;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    /**
     * Endpoint para obtener todos los departamentos.
     * <p>
     * Este método devuelve la lista de departamentos con su nombre.
     *
     * @return ResponseEntity<> - Respuesta con la lista de departamentos.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/departamentos")
    public ResponseEntity<?> obtenerDepartamentos()
    {
        try
        {
            List<Departamento> departamentos = iDepartamentoRepository.findAll();

            if (departamentos.isEmpty())
            {
                String mensajeError = "No hay departamentos registrados en la base de datos";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            // Convertimos los departamentos a DTOs
            List<DepartamentoDto> departamentosDto = departamentos.stream()
                    .map(departamento -> new DepartamentoDto(departamento.getNombre()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(departamentosDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo obtener los datos del desplegable";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/departamentos")
    public ResponseEntity<?> asignarProfesoresADepartamentos(@RequestParam("nombre") String nombre,
                                                             @RequestParam("plantilla") int plantilla)
    {
        try
        {
            Optional<Departamento> departamentoOpt = this.iDepartamentoRepository.findByNombre(nombre);

            if (departamentoOpt.isEmpty())
            {
                String mensajeError = "No existe un departamento con ese nombre";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            Departamento departamento = departamentoOpt.get();
            departamento.setPlantilla(plantilla);

            iDepartamentoRepository.saveAndFlush(departamento);

            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage().toString());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - Error en el servidor";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas/infoDepartamentos")
    public ResponseEntity<?> obtenerDatosDepartamentosConAsignaturas()
    {

        try
        {
            List<AsignaturaConDepartamentoDto> asignaturaConDepartamentoDtos = this.iAsignaturaRepository.encontrarAsignaturasConDepartamento();

            if(asignaturaConDepartamentoDtos.isEmpty())
            {
                String mensajeError = "No hay asignaturas registrados en la base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            return ResponseEntity.ok(asignaturaConDepartamentoDtos);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage().toString());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - Error en el servidor";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para obtener curso etapa y grupo de forma disctint.
     * <p>
     * Este método devolvera curso etapa y grupo de forma disctint
     *
     * @return ResponseEntity<?> - Respuesta del endpoint que devuelve el curso etapa y grupo
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/curso")
    public ResponseEntity<?> obtenerCursosEtapasGrupos()
    {
        try
        {
            List<CursoEtapaGrupo> cursoEtapaGrupos = iAsignaturaRepository.distinctCursoEtapaGrupo();

            if (cursoEtapaGrupos.isEmpty())
            {
                String mensajeError = "No hay cursoEtapaGrupo registradas en la base de datos";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            List<CursoEtapaGrupoDto> cursoEtapaGrupoDto = cursoEtapaGrupos.stream().map(cursoEtapaGrupo ->
                    new CursoEtapaGrupoDto(
                            cursoEtapaGrupo.getIdCursoEtapaGrupo().getCurso(),
                            cursoEtapaGrupo.getIdCursoEtapaGrupo().getEtapa(),
                            cursoEtapaGrupo.getIdCursoEtapaGrupo().getGrupo()
                    )).collect(Collectors.toList());

            return ResponseEntity.ok(cursoEtapaGrupoDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo obtener los datos del desplegable";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }


    /**
     * Endpoint para obtener Asignaturas por curso etapa y grupo.
     * <p>
     * Este método recibirá un curso etapa y grupo y devolvera las asignaturas que tengan ese curso etapa y grupo especificos
     *
     * @param curso            Número entero que representa el curso de la asignatura.
     * @param etapa            Cadena de texto que indica la etapa educativa (ej. "ESO", "Bachillerato").
     * @param grupo            Caracter que identifica el grupo dentro del curso (ej. 'A', 'B').
     * @return ResponseEntity<> - Respuesta del endpoint que devuelve las asignaturas
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturasPorCursoEtapaGrupo")
    public ResponseEntity<?> obtenerAsignaturasPorCursoEtapaGrupo(@RequestParam("curso") int curso,
                                                                  @RequestParam("etapa") String etapa,
                                                                  @RequestParam("grupo") Character grupo)
    {
        try
        {
            List<Asignatura> asignaturas = iAsignaturaRepository.asignaturasPorCursoEtapaGrupo(curso, etapa, grupo);

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No hay asignaturas registradas para la selección dada";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            // Convertimos las asignaturas a DTOs
            List<NombreAsignaturaDto> NombreAsignaturaDto = asignaturas.stream().map(asignatura ->
                    new NombreAsignaturaDto(
                            asignatura.getIdAsignatura().getNombre()
                    )).collect(Collectors.toList());

            return ResponseEntity.ok(NombreAsignaturaDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo obtener los datos del desplegable";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para obtener todas las asignaturas con su información
     *
     * @return ResponseEntity<> - Lista de asignaturas con sus departamentos
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
    public ResponseEntity<?> obtenerTodasLasAsignaturas()
    {
        try
        {
            List<Asignatura> asignaturas = iAsignaturaRepository.findAll();

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No hay asignaturas registradas en el sistema";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            // Convertimos a DTO
            List<AsignaturaInfoDto> asignaturasDto = asignaturas.stream().map(asignatura ->
                    new AsignaturaInfoDto(
                            asignatura.getIdAsignatura().getNombre(),
                            asignatura.getIdAsignatura().getCurso(),
                            asignatura.getIdAsignatura().getEtapa(),
                            asignatura.getIdAsignatura().getGrupo(),
                            (asignatura.getDepartamentoPropietario() != null) ? asignatura.getDepartamentoPropietario().getNombre() : null,
                            (asignatura.getDepartamentoReceptor() != null) ? asignatura.getDepartamentoReceptor().getNombre() : null
                    )
            ).collect(Collectors.toList());

            return ResponseEntity.ok(asignaturasDto);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo obtener los datos del desplegable";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());

        }

    }

    /**
     * Endpoint para eliminar los departamentos de una asignatura.
     * <p>
     * Este endpoint recibe el curso, etapa, grupo y nombre de la asignatura
     * como parámetros y establece a `null` el departamento propietario y
     * el departamento receptor de dicha asignatura.
     *
     * @param curso            Número entero que representa el curso de la asignatura.
     * @param etapa            Cadena de texto que indica la etapa educativa (ej. "ESO", "Bachillerato").
     * @param grupo            Caracter que identifica el grupo dentro del curso (ej. 'A', 'B').
     * @return ResponseEntity<> - Respuesta del endpoint que indica el resultado de la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PATCH, value = "/asignaturas/quitarDepartamentos")
    public ResponseEntity<?> quitarDepartamentosDeAsignatura(@RequestParam("curso") int curso,
                                                             @RequestParam("etapa") String etapa,
                                                             @RequestParam("grupo") Character grupo,
                                                             @RequestParam("nombre") String nombre)
    {

        try
        {
            // Buscar la asignatura con los datos proporcionados
            Optional<Asignatura> asignaturaOpt = iAsignaturaRepository
                    .findAsignaturasByCursoEtapaGrupoAndNombre(curso, etapa, grupo, nombre);

            if (asignaturaOpt.isEmpty())
            {
                String mensajeError = "ERROR - No se encontró la asignatura con los datos proporcionados";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            // Obtener la asignatura y actualizar los departamentos a null
            Asignatura asignatura = asignaturaOpt.get();
            asignatura.setDepartamentoPropietario(null);
            asignatura.setDepartamentoReceptor(null);

            // Guardar cambios en la base de datos
            iAsignaturaRepository.saveAndFlush(asignatura);

            log.info("INFO - Departamentos eliminados para la asignatura: {}", nombre);
            return ResponseEntity.status(200).body("Departamentos eliminados correctamente");

        }
        catch (SchoolManagerServerException schoolManagerServerException) {

            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo eliminar los departamentos";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para asignar departamentos a una asignatura.
     * <p>
     * Este endpoint recibe el curso, etapa, grupo y nombre de la asignatura
     * como parámetros y permite establecer los departamentos propietario
     * y receptor de dicha asignatura.
     *
     * @param curso                   Número entero que representa el curso de la asignatura.
     * @param etapa                   Cadena de texto que indica la etapa educativa (ej. "ESO", "Bachillerato").
     * @param grupo                   Caracter que identifica el grupo dentro del curso (ej. 'A', 'B').
     * @param nombre                  Nombre de la asignatura que se desea modificar.
     * @param departamentoPropietario Nombre del departamento propietario.
     * @param departamentoReceptor    Nombre del departamento receptor.
     * @return ResponseEntity<> - Respuesta del endpoint que indica el resultado de la operación.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PATCH, value = "/asignaturas/asignarDepartamentos")
    public ResponseEntity<?> asignarDepartamentosAAsignatura(@RequestParam("curso") int curso,
                                                             @RequestParam("etapa") String etapa,
                                                             @RequestParam("grupo") Character grupo,
                                                             @RequestParam("nombre") String nombre,
                                                             @RequestParam("departamentoPropietario") String departamentoPropietario,
                                                             @RequestParam(value = "departamentoReceptor") String departamentoReceptor)
    {

        try
        {
            // Buscar la asignatura con los datos proporcionados
            Optional<Asignatura> asignaturaOpt = iAsignaturaRepository
                    .findAsignaturasByCursoEtapaGrupoAndNombre(curso, etapa, grupo, nombre);

            if (asignaturaOpt.isEmpty())
            {
                String mensajeError = "ERROR - No se encontró la asignatura con los datos proporcionados";
                log.error(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            Asignatura asignatura = asignaturaOpt.get();

            // Obtener la asignatura y actualizar los departamentos
            Optional<Departamento> departamentoPropietarioOpt = iDepartamentoRepository.findByNombre(departamentoPropietario);
            Optional<Departamento> departamentoReceptorOpt = iDepartamentoRepository.findByNombre(departamentoReceptor);

            if (departamentoPropietarioOpt.isEmpty())
            {
                String mensajeError = "ERROR - No se encontró los departamentos con los datos proporcionados";
                log.warn(mensajeError);
                throw new SchoolManagerServerException(1, mensajeError);
            }

            departamentoReceptorOpt.ifPresent(asignatura::setDepartamentoReceptor);
            asignatura.setDepartamentoPropietario(departamentoPropietarioOpt.get());

            // Guardar cambios en la base de datos
            iAsignaturaRepository.saveAndFlush(asignatura);

            log.info("INFO - Departamentos asignados para la asignatura: {}", nombre);
            return ResponseEntity.ok("Departamentos asignados correctamente");

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(400).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo asignar los departamentos";
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, mensajeError, exception);

            log.error(mensajeError, exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}