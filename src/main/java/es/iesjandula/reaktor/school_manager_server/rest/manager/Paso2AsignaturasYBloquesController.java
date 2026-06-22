package es.iesjandula.reaktor.school_manager_server.rest.manager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaHorasDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CargaHorasAsignaturasResultDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Alumno;
import es.iesjandula.reaktor.school_manager_server.models.Bloque;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.models.Matricula;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IBloqueRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.services.manager.ParseoCsvHorasAsignaturasService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/asignaturasYBloques")
public class Paso2AsignaturasYBloquesController
{
    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private IBloqueRepository iBloqueRepository;

    @Autowired
    private IMatriculaRepository iMatriculaRepository;

    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IAlumnoRepository iAlumnoRepository;

    @Autowired
    private ParseoCsvHorasAsignaturasService parseoCsvHorasAsignaturasService;

    @Autowired
    private es.iesjandula.reaktor.school_manager_server.services.manager.CursoAcademicoResolver cursoAcademicoResolver;

    /**
     * Obtiene la lista de asignaturas de un curso y etapa determinados.
     * <p>
     * Devuelve el nombre de la asignatura, el número de horas y el número de alumnos, tanto en general como por grupos.
     *
     * @param curso el curso para el que se solicitan las asignaturas.
     * @param etapa la etapa educativa correspondiente.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y la lista de asignaturas si se encuentra información.
     * - 404 (Not Found) si no existen asignaturas para ese curso y etapa.
     * - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/asignaturas")
    public ResponseEntity<?> cargarAsignaturas(@RequestHeader("curso") int curso,
                                               @RequestHeader("etapa") String etapa)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<AsignaturaDto> asignaturas = iAsignaturaRepository.findByCursoAndEtapa(cursoAcademico, curso, etapa);

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No existen asignaturas para " + curso + " " + etapa;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            return ResponseEntity.ok().body(asignaturas);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudieron cargar las asignaturas";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Crea un nuevo bloque de asignaturas para un curso y etapa determinados.
     * <p>
     * El método valida que se hayan seleccionado al menos dos asignaturas, verifica que existan
     * y comprueba que no estén ya asignadas a otro bloque o a un departamento antes de crear uno nuevo.
     *
     * @param curso       el identificador del curso para el que se crea el bloque.
     * @param etapa       la etapa educativa asociada al bloque.
     * @param asignaturas una lista de nombres de asignaturas que se incluirán en el bloque;
     *                    debe contener al menos dos asignaturas.
     * @return una {@link ResponseEntity} con:
     * - 201 (Created) y el ID del bloque si se crea correctamente.
     * - 400 (Bad Request) si se seleccionan menos de dos asignaturas, si las asignaturas con docencia tienen horas distintas
     *                         o si alguna asignatura está marcada como sin docencia.
     * - 404 (Not Found) si alguna asignatura no se encuentra.
     * - 409 (Conflict) si alguna asignatura ya está asignada a otro bloque.
     * - 409 (CONFLICT) si la asignatura ya tiene un departamento asignado
     * - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/bloques")
    public ResponseEntity<?> crearBloques(@RequestParam("curso") int curso,
                                          @RequestParam("etapa") String etapa,
                                          @RequestParam("asignaturas") List<String> asignaturas)
    {
        try
        {

            if (asignaturas == null || asignaturas.size() < 2)
            {
                String mensajeError = "Tienes que seleccionar al menos 2 asignaturas";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURAS_MINIMAS_NO_SELECCIONADAS, mensajeError);
            }

            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            this.validarHorasIgualesParaBloque(cursoAcademico, curso, etapa, asignaturas);
            this.validarSinDocenciaParaBloque(cursoAcademico, curso, etapa, asignaturas);

            Bloque bloque = new Bloque();
            this.iBloqueRepository.saveAndFlush(bloque);

            for (String asignaturaString : asignaturas)
            {
                List<Asignatura> optionalAsignatura = this.iAsignaturaRepository.findAsignaturasByCursoEtapaAndNombre(cursoAcademico, curso, etapa, asignaturaString);

                for (Asignatura asignatura : optionalAsignatura)
                {

                    if (asignatura == null)
                    {
                        String mensajeError = "La asignatura no fue encontrada";
                        log.error(mensajeError);
                        throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
                    }

                    if (asignatura.getBloqueId() != null)
                    {
                        String mensajeError = "Una de las asignaturas ya tiene un bloque asignado";
                        log.error(mensajeError);
                        throw new SchoolManagerServerException(Constants.ASIGNATURA_CON_BLOQUE, mensajeError);
                    }

                    if (asignatura.getDepartamentoReceptor() != null)
                    {
                        String mensajeError = "La asignatura " + asignatura.getIdAsignatura().getNombre() + " está asignada a un departamento, desvinculalá primero";
                        log.error(mensajeError);
                        throw new SchoolManagerServerException(Constants.ASIGNATURA_ASIGNADA_A_DEPARTAMENTO, mensajeError);
                    }

                    if (!Objects.equals(asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().getGrupo(), Constants.GRUPO_OPTATIVAS))
                    {
//                      Creo un grupo Optativas para la asignatura que se va a asignar.
                        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.GRUPO_OPTATIVAS);
                        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
                        this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

//                      Busco todas la matriculas de esa asignatura
                        List<Matricula> matriculaABorrar = this.iMatriculaRepository.encontrarMatriculaPorNombreAsignaturaAndCursoAndEtapa(cursoAcademico, asignatura.getIdAsignatura().getNombre(), curso, etapa);
//                      Borro las matrículas de la asignatura con el grupo viejo.
                        this.iMatriculaRepository.deleteAll(matriculaABorrar);
//                      Borro la asignatura con el grupo viejo.
                        this.iAsignaturaRepository.delete(asignatura);

//                      Creo la asignatura con el grupo nuevo.
                        Asignatura asignaturaOptativas = getAsignatura(cursoAcademico, curso, etapa, Constants.GRUPO_OPTATIVAS, asignatura.isDesdoble(), asignatura.isSinDocencia(), asignatura.isEsoBachillerato(), asignatura.getHoras(),
                                bloque.getId(), asignatura.getIdAsignatura().getNombre(), asignatura.getDepartamentoReceptor(), asignatura.getDepartamentoPropietario());
                        this.iAsignaturaRepository.saveAndFlush(asignaturaOptativas);

                        for (Matricula matricula : matriculaABorrar)
                        {
//                          Creo las matrículas con el grupo nuevo.
                            IdMatricula idMatricula = getIdMatricula(cursoAcademico, curso, etapa, Constants.GRUPO_OPTATIVAS, matricula.getIdMatricula().getAlumno().getId(),
                                    matricula.getIdMatricula().getAsignatura().getIdAsignatura().getNombre());
                            matricula.setIdMatricula(idMatricula);

                            this.iMatriculaRepository.saveAndFlush(matricula);
                        }
                    }

                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(bloque.getId());

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.ASIGNATURAS_MINIMAS_NO_SELECCIONADAS
                    || schoolManagerServerException.getCode() == Constants.ASIGNATURAS_BLOQUE_HORAS_DIFERENTES
                    || schoolManagerServerException.getCode() == Constants.ASIGNATURA_BLOQUE_SIN_DOCENCIA)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_NO_ENCONTRADA)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_ASIGNADA_A_DEPARTAMENTO)
            {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }

        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo crear el bloque";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Elimina un bloque de asignaturas en función del curso, la etapa y el nombre de la asignatura proporcionados.
     * <p>
     * Si al desvincular la asignatura, el bloque ya no contiene más asignaturas asociadas, el bloque será eliminado.
     *
     * @param curso  el identificador del curso, proporcionado en la cabecera de la solicitud.
     * @param etapa  la etapa educativa, proporcionada en la cabecera de la solicitud.
     * @param nombre el nombre de la asignatura a desvincular del bloque, proporcionado en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 204 (NO_CONTENT) si el bloque se eliminó correctamente.
     * - 404 (NOT_FOUND) si no se encontró la asignatura.
     * - 409 (CONFLICT) si la asignatura ya tiene un departamento asignado
     * - 500 (INTERNAL_SERVER_ERROR) si ocurrió un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/bloques")
    public ResponseEntity<?> eliminarBloque(@RequestHeader(value = "curso", required = true) Integer curso,
                                            @RequestHeader(value = "etapa", required = true) String etapa,
                                            @RequestHeader(value = "nombre", required = true) String nombreAsignatura)
    {
        try
        {

            String cursoAcademico = this.cursoAcademicoResolver.resolver();

            // Buscamos la asignatura
            List<Asignatura> listAsignatura = iAsignaturaRepository.findNombreByCursoEtapaAndNombres(cursoAcademico, curso, etapa, nombreAsignatura);

            if (listAsignatura.isEmpty())
            {
                String mensajeError = "No se han encontrado " + nombreAsignatura + " en " + curso + " " + etapa;
                log.error(mensajeError);

                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            for (Asignatura asignatura : listAsignatura)
            {
                if (asignatura.getDepartamentoReceptor() != null)
                {
                    String mensajeError = "La asignatura " + asignatura.getIdAsignatura().getNombre() + " está asignada a un departamento, desvinculalá primero";
                    log.error(mensajeError);
                    throw new SchoolManagerServerException(Constants.ASIGNATURA_CON_BLOQUE, mensajeError);
                }
                // Desasociar la asignatura del bloque
                Bloque bloque = asignatura.getBloqueId();

//              Busco todas la matriculas de esa asignatura
                List<Matricula> matriculasABorrar = this.iMatriculaRepository.encontrarMatriculaPorNombreAsignaturaAndCursoAndEtapa(cursoAcademico, asignatura.getIdAsignatura().getNombre(), curso, etapa);
                if (!matriculasABorrar.isEmpty())
                {
//                  Borro las matrículas de la asignatura con el grupo viejo.
                    this.iMatriculaRepository.deleteAll(matriculasABorrar);
                    this.iMatriculaRepository.flush();

                    for (Matricula matricula : matriculasABorrar)
                    {
//                      Busco el alumno
                        Optional<Alumno> alumno = this.iAlumnoRepository.findById(matricula.getIdMatricula().getAlumno().getId());

                        String grupoAlumno = this.iMatriculaRepository.encontrarGrupoPorMatricula(cursoAcademico, curso, etapa, alumno.get().getId());

//                      Creo un grupo Optativas para la asignatura que se va a asignar.
                        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupoAlumno);
                        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
                        this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

//                      Creo las matrículas con el grupo nuevo.
                        IdMatricula idMatricula = getIdMatricula(cursoAcademico, curso, etapa, grupoAlumno, alumno.get().getId(),
                                matricula.getIdMatricula().getAsignatura().getIdAsignatura().getNombre());
                        Matricula matriculaNueva = new Matricula();
                        matriculaNueva.setIdMatricula(idMatricula);

//                      Creo la asignatura con el grupo nuevo.
                        Asignatura asignaturaNuegoGrupo = getAsignatura(cursoAcademico, curso, etapa, grupoAlumno, asignatura.isDesdoble(), asignatura.isSinDocencia(), asignatura.isEsoBachillerato(), asignatura.getHoras(),
                                bloque.getId(), asignatura.getIdAsignatura().getNombre(), asignatura.getDepartamentoReceptor(), asignatura.getDepartamentoPropietario());
                        asignaturaNuegoGrupo.setBloqueId(null);
                        this.iAsignaturaRepository.saveAndFlush(asignaturaNuegoGrupo);

//                      Borro la asignatura con el grupo viejo.
                        this.iMatriculaRepository.saveAndFlush(matriculaNueva);

                    }
                    this.iAsignaturaRepository.delete(asignatura);
                    this.iAsignaturaRepository.flush();
                }
                else
                {
                    this.iAsignaturaRepository.delete(asignatura);
                    this.iAsignaturaRepository.flush();

                    IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.SIN_GRUPO_ASIGNADO);
                    CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                    cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
                    IdAsignatura idAsignatura = new IdAsignatura(cursoEtapaGrupo, asignatura.getIdAsignatura().getNombre());
                    asignatura.setIdAsignatura(idAsignatura);
                    asignatura.setBloqueId(null);

                    this.iAsignaturaRepository.saveAndFlush(asignatura);
                }

                if (bloque != null && bloque.getAsignaturas().isEmpty())
                {
                    this.iBloqueRepository.delete(bloque);
                    this.iBloqueRepository.flush();
                }
                else
                {
                    log.info("Queda bloques por eliminar");
                }

                Long bloqueId = -1l;
                if (bloque != null)
                {
                    bloqueId = bloque.getId();
                }

                log.info("INFO - Bloque " + bloqueId + " eliminado con éxito");
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_NO_ENCONTRADA)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - Error al eliminar el bloque";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Modifica el estado de docencia para una asignatura específica o un conjunto de asignaturas según el nombre
     * especificado.
     * <p>
     * Actualiza el valor del campo "sinDocencia" en la base de datos para las asignaturas encontradas.
     *
     * @param curso            el identificador del curso académico al que pertenecen las asignaturas, requerido.
     * @param etapa            la etapa educativa de las asignaturas, requerida.
     * @param nombreAsignatura el nombre de la asignatura o asignaturas cuyo estado de docencia se va a modificar.
     * @param sinDocencia      el estado a establecer en el campo "sinDocencia", que indica si las asignaturas no tienen
     *                         docencia asociada.
     * @return una instancia de {@link ResponseEntity} indicando el resultado de la operación:
     * - 204 (No Content) si la operación se realiza con éxito.
     * - 400 (Bad Request) si la asignatura pertenece a un bloque y se intenta marcar como sin docencia.
     * - 404 (Not Found) si no se encuentran asignaturas con el nombre proporcionado.
     * - 500 (Internal Server Error) en caso de un error general durante el procesamiento.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/sinDocencia")
    public ResponseEntity<?> asignaturasSinDocencia(@RequestHeader(value = "curso", required = true) Integer curso,
                                                    @RequestHeader(value = "etapa", required = true) String etapa,
                                                    @RequestHeader("nombreAsignatura") String nombreAsignatura,
                                                    @RequestHeader("sinDocencia") Boolean sinDocencia)
    {
        try
        {

            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<Asignatura> asignaturas = this.iAsignaturaRepository.encontrarAsignaturaPorNombre(cursoAcademico, curso, etapa, nombreAsignatura);

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No se ha encontrado " + nombreAsignatura + " en base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            for (Asignatura asignatura : asignaturas)
            {
                if (Boolean.TRUE.equals(sinDocencia) && asignatura.getBloqueId() != null)
                {
                    String mensajeError = "La asignatura «" + nombreAsignatura + "» pertenece a un bloque y no puede marcarse como sin docencia.";
                    log.error(mensajeError);
                    throw new SchoolManagerServerException(Constants.ASIGNATURA_BLOQUE_SIN_DOCENCIA, mensajeError);
                }

                asignatura.setSinDocencia(sinDocencia);
                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_BLOQUE_SIN_DOCENCIA)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_NO_ENCONTRADA)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo actualizar el estado de docencia de la asignatura";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Modifica el estado de desdoble para una asignatura específica o un conjunto de asignaturas
     * según el nombre especificado.
     * <p>
     * Actualiza el valor del campo "desdoble" en la base de datos para las asignaturas que coinciden
     * con los filtros proporcionados (curso, etapa y nombre de la asignatura).
     *
     * @param curso            el identificador del curso académico al que pertenecen las asignaturas, requerido.
     * @param etapa            la etapa educativa de las asignaturas, requerida.
     * @param nombreAsignatura el nombre de la asignatura o asignaturas cuyo estado de desdoble se va a modificar.
     * @param desdoble         el estado a establecer en el campo "desdoble", que indica si las asignaturas tienen
     *                         desdoble asociado.
     * @return una instancia de {@link ResponseEntity} indicando el resultado de la operación:
     * - 204 (No Content) si la operación se realiza con éxito.
     * - 404 (Not Found) si no se encuentran asignaturas con el nombre proporcionado.
     * - 500 (Internal Server Error) en caso de un error general durante el procesamiento.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/desdoble")
    public ResponseEntity<?> asignaturasDesdobles(@RequestHeader(value = "curso") Integer curso,
                                                  @RequestHeader(value = "etapa") String etapa,
                                                  @RequestHeader("nombreAsignatura") String nombreAsignatura,
                                                  @RequestHeader("desdoble") Boolean desdoble)
    {
        try
        {

            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<Asignatura> asignaturas = this.iAsignaturaRepository.encontrarAsignaturaPorNombre(cursoAcademico, curso, etapa, nombreAsignatura);

            if (asignaturas.isEmpty())
            {
                String mensajeError = "No se ha encontrado la asignatura " + nombreAsignatura + " en base de datos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            for (Asignatura asignatura : asignaturas)
            {
                asignatura.setDesdoble(desdoble);
                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo actualizar el estado del desdoble de la asignatura";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Obtiene el número de horas de las asignaturas para un curso y etapa determinados.
     * <p>
     * Recupera desde el repositorio una lista de asignaturas con sus nombres y horas correspondientes.
     *
     * @param curso el identificador del curso enviado en la cabecera de la solicitud.
     * @param etapa la etapa educativa enviada en la cabecera de la solicitud.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y la lista de asignaturas si se encuentran datos.
     * - 404 (Not Found) si no se encuentran asignaturas.
     * - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/horas")
    public ResponseEntity<?> mostrarHoras(@RequestHeader("curso") Integer curso,
                                          @RequestHeader("etapa") String etapa)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<AsignaturaHorasDto> listAsignatuasHoras = this.iAsignaturaRepository.findNombreAndHorasByCursoEtapa(cursoAcademico, curso, etapa);

            if (listAsignatuasHoras.isEmpty())
            {
                String mensajeError = "No se ha encontrado asignaturas con horas para '" + curso + " " + etapa;
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            return ResponseEntity.ok().body(listAsignatuasHoras);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudieron obtener las horas de las asignaturas";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Carga las horas de las asignaturas desde un CSV con la misma estructura que el export de matrículas
     * (columnas Selección, Materia, Grupo de materia, …). La columna Materia incluye el nombre y las horas
     * en formato {@code Nombre (H:MM)}.
     *
     * @param archivoCsv el fichero CSV con las horas por asignatura.
     * @param curso      el curso al que aplicar las horas.
     * @param etapa      la etapa educativa correspondiente.
     * @return una {@link ResponseEntity} con:
     * - 200 (OK) y {@link CargaHorasAsignaturasResultDto} si el procesamiento finaliza correctamente.
     * - 400 (Bad Request) si el archivo está vacío o el formato es inválido.
     * - 404 (Not Found) si no existen asignaturas para el curso/etapa.
     * - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/horas/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> cargarHorasDesdeCsv(@RequestParam(value = "csv", required = true) MultipartFile archivoCsv,
                                                 @RequestHeader(value = "curso", required = true) Integer curso,
                                                 @RequestHeader(value = "etapa", required = true) String etapa)
    {
        try
        {
            CargaHorasAsignaturasResultDto resultado = this.parseoCsvHorasAsignaturasService.cargarHorasDesdeCsv(archivoCsv, curso, etapa);
            return ResponseEntity.ok().body(resultado);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            if (schoolManagerServerException.getCode() == Constants.ARCHIVO_VACIO
                    || schoolManagerServerException.getCode() == Constants.DATOS_NO_PROCESADO)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else if (schoolManagerServerException.getCode() == Constants.ASIGNATURA_NO_ENCONTRADA
                    || schoolManagerServerException.getCode() == Constants.CURSO_ETAPA_NO_ENCONTRADO)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body(schoolManagerServerException.getBodyExceptionMessage());
            }
            else
            {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                        .body(schoolManagerServerException.getBodyExceptionMessage());
            }
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudieron cargar las horas desde el CSV";
            log.error(mensajeError, exception);

            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Asigna un número de horas a una asignatura, identificada por su curso, etapa y nombre.
     *
     * @param curso            el curso en el que se encuentra la asignatura.
     * @param etapa            la etapa educativa correspondiente.
     * @param nombreAsignatura el nombre de la asignatura a actualizar.
     * @param horas            el número de horas a asignar.
     * @return una {@link ResponseEntity} con:
     * - 204 (No Content) si la operación se realiza correctamente.
     * - 404 (Not Found) si no se encuentra la asignatura.
     * - 500 (Internal Server Error) si ocurre un error inesperado.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/horas")
    public ResponseEntity<?> asignarHoras(@RequestHeader("curso") Integer curso,
                                          @RequestHeader("etapa") String etapa,
                                          @RequestHeader("nombreAsignatura") String nombreAsignatura,
                                          @RequestHeader("horas") Integer horas)
    {
        try
        {
            String cursoAcademico = this.cursoAcademicoResolver.resolver();
            List<Asignatura> listAsignatura = this.iAsignaturaRepository.findNombreByCursoEtapaAndNombres(cursoAcademico, curso, etapa, nombreAsignatura);

            if (listAsignatura == null)
            {
                String mensajeError = "No se ha encontrado la asignatura '" + nombreAsignatura + "' para '" + curso + " " + etapa + "' para asignar las horas";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
            }

            for (Asignatura asignatura : listAsignatura)
            {

                asignatura.setHoras(horas);
                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudieron asignar las horas a la asignatura";
            log.error(mensajeError, exception);

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Comprueba que ninguna asignatura seleccionada esté marcada como sin docencia.
     */
    private void validarSinDocenciaParaBloque(String cursoAcademico, int curso, String etapa, List<String> asignaturas) throws SchoolManagerServerException
    {
        for (String asignaturaNombre : asignaturas)
        {
            List<Asignatura> asignaturasEncontradas = this.iAsignaturaRepository.findAsignaturasByCursoEtapaAndNombre(cursoAcademico, curso, etapa, asignaturaNombre);

            if (asignaturasEncontradas == null || asignaturasEncontradas.isEmpty())
            {
                continue;
            }

            Asignatura asignatura = asignaturasEncontradas.get(0);

            if (asignatura.isSinDocencia())
            {
                String mensajeError = "No se puede crear el bloque: la asignatura «" + asignaturaNombre + "» está marcada como sin docencia.";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURA_BLOQUE_SIN_DOCENCIA, mensajeError);
            }
        }
    }

    /**
     * Comprueba que todas las asignaturas con docencia del bloque tengan las mismas horas.
     * Las marcadas como sin docencia se excluyen de la comparación, coherente con el cálculo del total de horas por bloque.
     */
    private void validarHorasIgualesParaBloque(String cursoAcademico, int curso, String etapa, List<String> asignaturas) throws SchoolManagerServerException
    {
        Integer horasReferencia = null;
        String nombreReferencia = null;

        for (String asignaturaNombre : asignaturas)
        {
            List<Asignatura> asignaturasEncontradas = this.iAsignaturaRepository.findAsignaturasByCursoEtapaAndNombre(cursoAcademico, curso, etapa, asignaturaNombre);

            if (asignaturasEncontradas == null || asignaturasEncontradas.isEmpty())
            {
                continue;
            }

            Asignatura asignatura = asignaturasEncontradas.get(0);

            if (asignatura.isSinDocencia())
            {
                continue;
            }

            int horas = asignatura.getHoras();

            if (horasReferencia == null)
            {
                horasReferencia = horas;
                nombreReferencia = asignaturaNombre;
            }
            else if (horas != horasReferencia)
            {
                String mensajeError = "No se puede crear el bloque: las asignaturas con docencia deben tener las mismas horas. "
                        + "«" + nombreReferencia + "» tiene " + horasReferencia + " h y «" + asignaturaNombre + "» tiene " + horas + " h.";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ASIGNATURAS_BLOQUE_HORAS_DIFERENTES, mensajeError);
            }
        }
    }

    /**
     * Crea y devuelve una instancia de la clase {@code Asignatura} basada en los parámetros proporcionados.
     * <p>
     * Utiliza los datos de curso, etapa y grupo para construir una nueva asignatura con la información asociada.
     *
     * @param curso el número de curso académico asociado a la asignatura.
     * @param etapa la etapa educativa (por ejemplo, primaria, secundaria) correspondiente a la asignatura.
     * @param grupo el identificador del grupo al que pertenece la asignatura.
     * @return una nueva instancia de {@code Asignatura} construida con los parámetros proporcionados.
     */
    private static Asignatura getAsignatura(String cursoAcademico, int curso, String etapa, String grupo, boolean desdoble, boolean sinDocencia, boolean esoBachillerato, int horas, Long bloque, String nombre,
                                            Departamento departamentoReceptor, Departamento departamentoPropietario)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo);
        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
        IdAsignatura idAsignatura = new IdAsignatura(cursoEtapaGrupo, nombre);
        Bloque bloqueObjeto = null;
        if (bloque != null)
        {
            bloqueObjeto = new Bloque();
            bloqueObjeto.setId(bloque);
        }
        return new Asignatura(idAsignatura, horas, esoBachillerato, sinDocencia, desdoble, departamentoReceptor, departamentoPropietario, bloqueObjeto, false);
    }

    /**
     * Genera una instancia de {@link IdMatricula}, que representa el identificador único
     * para la matrícula de un alumno en una asignatura específica dentro de un curso,
     * etapa y grupo determinados.
     *
     * @param curso            el identificador del curso académico para la matrícula.
     * @param etapa            la etapa o nivel educativo del curso.
     * @param grupo            el grupo o división dentro del curso y etapa.
     * @param alumnoId         el identificador único del alumno.
     * @param nombreAsignatura el nombre de la asignatura en la que el alumno está matriculado.
     * @return una instancia de {@link IdMatricula} que encapsula la información
     * referente a la matrícula.
     */
    private static IdMatricula getIdMatricula(String cursoAcademico, int curso, String etapa, String grupo, int alumnoId, String nombreAsignatura)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo);
        CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
        cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

        IdAsignatura idAsignatura = new IdAsignatura(cursoEtapaGrupo, nombreAsignatura);

        Asignatura asignaturaOptativas = new Asignatura();
        asignaturaOptativas.setIdAsignatura(idAsignatura);

        Alumno alumno = new Alumno();
        alumno.setId(alumnoId);

        return new IdMatricula(asignaturaOptativas, alumno);
    }
}


