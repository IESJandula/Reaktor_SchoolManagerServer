package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.dtos.*;
import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.repositories.*;
import es.iesjandula.reaktor.school_manager_server.services.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.services.CursoEtapaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/crearGrupos")
public class Paso3CrearGruposController 
{
    @Autowired
    private ICursoEtapaRepository iCursoEtapaRepository;

    @Autowired
	private CursoEtapaService cursoEtapaService ;

    @Autowired
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    @Autowired
    private IAlumnoRepository iAlumnoRepository;
    
    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;
    
    @Autowired
    private IMatriculaRepository iMatriculaRepository;

    @Autowired
    private IBloqueRepository iBloqueRepository;

    @Autowired
    private AlumnoService alumnoService;

    /**
	 * Endpoint para obtener los cursos etapas.
	 * 
	 * Este método obtiene mediante un get todos los cursos etapas guardados en base
	 * de datos para despues mostrarlos en el front en un select.
	 * 
	 * @return ResponseEntity<?> - Respuesta con las lista de cursos y etapas.
	 */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/cursoEtapa")
    public ResponseEntity<?> obtenerCursoEtapa() 
    {
        try 
        {
            // Lista usada para guardar los registros de la Tabla CursoEtapa
            List<CursoEtapa> listaCursoEtapa = new ArrayList<>();

            // Asignar los registros de la Tabla CursoEtapa
            listaCursoEtapa = this.iCursoEtapaRepository.findAll();

            // Si la lista esta vacia, lanzar excepcion
            if (listaCursoEtapa.isEmpty()) 
            {
                String mensajeError = "ERROR - Sin cursos y etapas en la base de datos";

                // Lanzar excepcion y mostrar log con mensaje diferente
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_CURSOS_ETAPAS_ENCONTRADOS, mensajeError);
            }

            // Devolver la lista
            log.info("INFO - Lista de los cursos etapas");
            return ResponseEntity.status(200).body(listaCursoEtapa);
        } 
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo cargar la lista";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para crear un nuevo grupo en el sistema basado en el curso y etapa
     * proporcionados.
     *
     * Este método asigna un nuevo grupo a un curso y etapa específicos,
     * asegurándose de que el nombre del grupo sea único
     * en función de la cantidad de veces que ya existe dicho curso y etapa en la
     * base de datos.
     * 
     * @param curso - El identificador del curso para el cual se está creando el
     *              grupo.
     * @param etapa - La etapa educativa asociada al curso para el cual se está
     *              creando el grupo.
     * @return ResponseEntity<?> - Un mensaje de éxito indicando que el grupo ha
     *         sido creado correctamente, o una excepción con el mensaje de error si
     *         ocurrió algún problema.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/grupos")
    public ResponseEntity<?> crearGrupo(@RequestHeader(value = "curso", required = true) Integer curso,
                                        @RequestHeader(value = "etapa", required = true) String etapa) 
    {
        try 
        {
            // Validamos y obtenemos el curso y etapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Numero de veces repetido el Curso Etapa en la BD
            int contador = this.iCursoEtapaGrupoRepository.cuentaCursoEtapaGruposCreados(curso, etapa);

            // Asignar la letra A
            char grupo = Constants.GRUPO_INICIAL ;

            // Asignar la letra según el numero de veces que este repetido en BD
            for (int i = 0; i < contador; i++) 
            {
                grupo++;
            }

            // Crear registro de Curso Etapa Grupo
            CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();

            // Crear campo de id con todos los campos
            IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(curso, etapa, grupo);

            // Asignar el id al registro de Curso Etapa
            cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);

            // Asignamos por defecto el horario matutino a true
            cursoEtapaGrupo.setHorarioMatutino(true);

            // Indicamos si es ESO o Bachillerato
            cursoEtapaGrupo.setEsoBachillerato(cursoEtapa.isEsoBachillerato());

            // Insertar en BD
            this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

            // Log de información antes de la respuesta
            log.info("INFO - Grupo creado correctamente para el curso: {} y etapa: {}", curso, etapa);

            // Devolver la respuesta indicando que el grupo ha sido creado correctamente
            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo crear el grupo";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }    

    /**
     * Endpoint para obtener los grupos asociados a un curso y etapa específicos.
     * 
     * Este método consulta los grupos disponibles para un curso y etapa
     * determinados.
     * Si no se encuentran grupos para la combinación de curso y etapa
     * proporcionados,
     * se lanza una excepción personalizada. En caso de un error general, se maneja
     * adecuadamente
     * la excepción y se devuelve un mensaje de error.
     * 
     * @param curso - El identificador del curso para el cual se desean obtener los
     *              grupos.
     * @param etapa - La etapa educativa asociada al curso para la cual se desean
     *              obtener los grupos.
     * @return ResponseEntity<?> - La lista de grupos encontrados o una excepción
     *         con el mensaje de error si no se encontraron grupos o si ocurre algún
     *         fallo durante el proceso.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/grupos")
    public ResponseEntity<?> obtenerGrupo(@RequestHeader(value = "curso", required = true) Integer curso,
                                          @RequestHeader(value = "etapa", required = true) String etapa) 
    {
        try 
        {
            // Obtener la lista de grupos según curso y etapa
            List<CursoEtapaGrupoDto> cursosEtapasGrupos = this.iCursoEtapaGrupoRepository.buscaCursoEtapaGruposCreados(curso, etapa);

            // Si la lista está vacía, lanzar una excepción
            if (cursosEtapasGrupos.isEmpty()) 
            {
                // Lanzar excepcion y mostrar log con mensaje de Error
                String mensajeError = "ERROR - No se encontraron grupos para el curso " + curso + " y etapa " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.CURSO_ETAPA_GRUPO_NO_ENCONTRADO, mensajeError);
            }

            // Log de información antes de la respuesta
            log.info("INFO - Se han encontrado los siguientes grupos para el curso: {} y etapa: {}", curso, etapa);

            // Devolver la lista de cursos, etapas y grupos encontrados
            return ResponseEntity.status(200).body(cursosEtapasGrupos);
        } 
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo encontrar el grupo";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para obtener la lista de alumnos pendientes por asignar o ya
     * asignados a un grupo específico de un curso y etapa.
     *
     * Este método recibe los parámetros del curso, la etapa y el grupo, y luego
     * recupera
     * una lista de alumnos pendientes y también asignados a ese grupo especifico,
     * si hay algún
     * error en el proceso, se captura la excepción y se devuelve un mensaje de
     * error adecuado.
     *
     * @param curso - El curso para el que se solicita la lista de alumnos.
     * @param etapa - La etapa para la cual se solicita la lista de alumnos.
     * @param grupo - El grupo específico dentro del curso y etapa que se está
     *              consultando.
     * @return ResponseEntity<?> - Respuesta con la lista de alumnos del grupo o
     *         pendientes de asignar, o una excepción personalizada si ocurre algún
     *         error durante la operación.
     */

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/gruposAlumnos")
    public ResponseEntity<?> obtenerAlumnosConGrupo(@RequestHeader(value = "curso", required = true) Integer curso,
                                                    @RequestHeader(value = "etapa", required = true) String etapa,
                                                    @RequestHeader(value = "grupo", required = true) Character grupo)
    {
        try
        {
            // Crear la lista de Alumnos a devolver
            List<Integer> idsDeAlumnosDelGrupo = this.iMatriculaRepository.encontrarIdAlumnoPorCursoEtapaYGrupo(curso, etapa, grupo);

            List<AlumnoDto2> alumnosEnGrupo = new ArrayList<>();

            for (Integer idAlumno : idsDeAlumnosDelGrupo)
            {
                AlumnoDto2 alumnoDto2 = new AlumnoDto2();
                Optional<Alumno> alumnoEncontrado = this.iAlumnoRepository.findById(idAlumno);
                if (alumnoEncontrado.isPresent())
                {
                    alumnoDto2.setNombre(alumnoEncontrado.get().getNombre());
                    alumnoDto2.setApellidos(alumnoEncontrado.get().getApellidos());
                    alumnoDto2.setGrupo(grupo);
                }
                alumnosEnGrupo.add(alumnoDto2);
            }

            // Si la lista esta vacía
            if (alumnosEnGrupo.isEmpty())
            {
                // Lanzar excepcion y mostrar log con mensaje de Error
                String mensajeError = "ERROR - Lista sin alumnos encontrados para el grupo " + grupo + " del curso " + curso + " y etapa " + etapa;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.SIN_ALUMNOS_ENCONTRADOS, mensajeError);
            }

            // Log de información antes de la respuesta
            log.info("INFO - Lista con nombres y apellidos de los alumnos asignados y pendientes de asignar");

            // Devolver la lista de Alumnos
            return ResponseEntity.status(200).body(alumnosEnGrupo);
        }
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo obtener la lista de alumnos";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }


    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/gruposAlumnosTotales")
    public ResponseEntity<?> obtenerTodosAlumnos(@RequestHeader(value = "curso", required = true) Integer curso,
            									 @RequestHeader(value = "etapa", required = true) String etapa)
    {
    	try 
    	{
    		List<AlumnoDto3> listaDatosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAlumnosByCursoEtapa(curso, etapa);
        	
        	if(listaDatosBrutoAlumnoMatriculas.isEmpty()) 
        	{
        		String mensajeError = "No se ha encontrado datos para ese curso y etapa";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(Constants.SIN_ALUMNOS_ENCONTRADOS, mensajeError);
        	}
    		return ResponseEntity.ok(listaDatosBrutoAlumnoMatriculas);
    	}
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo obtener el grupo de alumnos";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }


    /**
     * Endpoint para asignar una lista de alumnos a un grupo específico de un curso
     * y etapa.
     * 
     * Este método asigna los alumnos, que son recibidos como una lista de objetos
     * `AlumnoDto2`,
     * a un grupo específico dentro de un curso y etapa determinados. Si el alumno
     * ya existe en
     * la tabla `DatosBrutoAlumnoMatricula`, se transfiere el alumno al grupo
     * especificado y
     * luego se elimina de la tabla `DatosBrutoAlumnoMatricula`.
     * 
     * @param alumnos - Lista de objetos `AlumnoDto2` que contiene los datos de los
     *                alumnos a asignar.
     * @param curso   - El identificador del curso al que pertenecen los alumnos.
     * @param etapa   - La etapa educativa asociada al curso.
     * @param grupo   - El identificador del grupo (una letra que representa el
     *                grupo) al que se asignarán los alumnos.
     * @return ResponseEntity<?> - Respuesta con un mensaje de éxito si los alumnos
     *         se asignaron correctamente, o una excepción con el mensaje de error
     *         si ocurre un fallo durante el proceso.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/gruposAlumnos")
    public ResponseEntity<?> asignarAlumnos(@RequestBody List<AlumnoDto2> alumnos,
								            @RequestHeader(value = "curso", required = true) Integer curso,
								            @RequestHeader(value = "etapa", required = true) String etapa,
								            @RequestHeader(value = "grupo", required = true) Character grupo) 
    {
        try 
        {
            // Validamos y obtenemos el curso y etapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Por cada alumno proporcionado en la lista de entrada se buscan en el repo de DatosBrutosAlumnoMatricula
            for (AlumnoDto2 alumnoDatosBrutos : alumnos) 
            {

                // Buscar los registros del alumno en DatosBrutosAlumnoMatricula
                List<DatosBrutoAlumnoMatricula> datosBrutoAlumnoMatriculaAsignaturasOpt = 
                    this.iDatosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(alumnoDatosBrutos.getNombre(), alumnoDatosBrutos.getApellidos(),cursoEtapa);

                for (DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaAsignaturaOpt : datosBrutoAlumnoMatriculaAsignaturasOpt) 
                {
                    if(datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals("MATR") || datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals("PEND"))
                    {
                        // Registramos el alumno
                        Alumno alumno = this.asignarAlumnosRegistrarAlumno(curso, etapa, grupo, alumnoDatosBrutos.getNombre(), alumnoDatosBrutos.getApellidos());

                        // Registramos la asignatura
                        Asignatura asignatura = this.asignarAlumnosRegistrarAsignatura(curso, etapa, grupo, datosBrutoAlumnoMatriculaAsignaturaOpt.getAsignatura(), cursoEtapa.isEsoBachillerato());

                        // Registramos la matricula
                        this.asignarAlumnosRegistrarMatricula(datosBrutoAlumnoMatriculaAsignaturaOpt, alumno, asignatura);
                    }
                }
            }

            // Log de información antes de la respuesta
            log.info("INFO - Alumnos asignados correctamente al grupo {} para el curso {} y etapa {}", grupo, curso, etapa);

            // Devolver mensaje de OK
            return ResponseEntity.ok().build();
        } 
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo asignar los alumnos al grupo";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Endpoint para desasignar un alumno de un grupo específico.
     * 
     * Este método busca al alumno en la tabla `DatosBrutoAlumnoMatriculaGrupo`
     * utilizando
     * su nombre y apellidos. Si se encuentra al alumno, se transfiere el registro a
     * la
     * tabla `DatosBrutoAlumnoMatricula` y luego se elimina el registro en la tabla
     * `DatosBrutoAlumnoMatriculaGrupo`, desasignando así al alumno del grupo.
     * 
     * @param alumnoDto - El objeto `AlumnoDto` que contiene los datos del alumno a
     *               desasignar.
     * @return ResponseEntity<?> - Respuesta con un mensaje de éxito si el alumno
     *         fue desasignado correctamente, o una excepción con el mensaje de
     *         error si ocurre un fallo durante el proceso.
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/gruposAlumnos")
    public ResponseEntity<?> borrarAlumno(@RequestBody AlumnoDto2 alumnoDto)
    {
        try
        {
        alumnoService.borrarAlumno(alumnoDto);

        // Log de información antes de la respuesta
            log.info("INFO - Alumno desasignado correctamente");

            // Devolver mensaje de OK
            return ResponseEntity.ok().build();
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo borrar el alumno";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/turnoHorario")
    public ResponseEntity<?> actualizarTurnoHorario(@RequestHeader(value = "curso", required = true) Integer curso,
                                                    @RequestHeader(value = "etapa", required = true) String etapa,
                                                    @RequestHeader(value = "grupo", required = true) Character grupo,
                                                    @RequestHeader(value = "esHorarioMatutino", required = true) Boolean esHorarioMatutino) 
    {
        try
        {
            Optional<CursoEtapaGrupo> cursoEtapaGrupoOptional = this.iCursoEtapaGrupoRepository.findById(new IdCursoEtapaGrupo(curso, etapa, grupo)) ;

            if (!cursoEtapaGrupoOptional.isPresent())
            {
                String mensajeError = "ERROR en la actualización del turno horario - No se encontró el curso " + curso + " " + etapa + " " + grupo ;

                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.CURSO_ETAPA_GRUPO_NO_ENCONTRADO, mensajeError);
            }

            CursoEtapaGrupo cursoEtapaGrupo = cursoEtapaGrupoOptional.get() ;

            // Actualizamos el turno horario
            cursoEtapaGrupo.setHorarioMatutino(esHorarioMatutino) ;

            // Guardamos el curso etapa grupo
            this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo) ;

            // Log de información antes de la respuesta
            log.info("INFO - Turno horario actualizado correctamente {} {} {} {}", curso, etapa, grupo, esHorarioMatutino) ;

            // Devolvemos mensaje de OK
            return ResponseEntity.ok().build() ;
        }
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Devolver la excepción personalizada y el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo actualizar el turno horario";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }

    /**
     * Método para asignar alumnos y registrarlos en la tabla Alumno
     * 
     * @param datosBrutoAlumnoMatriculaAsignaturaOpt - El objeto DatosBrutoAlumnoMatricula que contiene los datos del alumno a asignar
     * @return Alumno - El alumno asignado y registrado en la tabla Alumno
     */
    private Alumno asignarAlumnosRegistrarAlumno(int curso, String etapa, Character grupo, String nombreAlumno, String apellidosAlumno)
    {
        // Creamos un nuevo Alumno
        Alumno alumno = null ;

        // Buscamos el alumno primero a partir de la tabla matricula
        Optional<Alumno> optionalAlumno = this.iMatriculaRepository.buscarAlumnoPorCursoEtapaNombreApellidos(curso, etapa, grupo, nombreAlumno, apellidosAlumno);

        // Si existe, lo asignamos
        if (optionalAlumno.isPresent())
        {
            alumno = optionalAlumno.get();
        }
        else
        {
            Optional<Alumno> optionalAlumnoA = this.iAlumnoRepository.findByNombreAndApellidos(nombreAlumno, apellidosAlumno);

            if(optionalAlumnoA.isPresent())
            {
                alumno = optionalAlumnoA.get();
            }
            else {
                // Creamos un nuevo Alumno
                alumno = new Alumno();

                // Asignar cada uno de los campos
                alumno.setNombre(nombreAlumno);
                alumno.setApellidos(apellidosAlumno);

                // Guardar el registro en la tabla Alumno
                this.iAlumnoRepository.saveAndFlush(alumno);
            }

        }

        return alumno ;
    }

    /**
     * Método para obtener la asignatura
     * 
     * @param curso - El identificador del curso para el cual se desea obtener la asignatura
     * @param etapa - La etapa educativa asociada al curso para el cual se desea obtener la asignatura
     * @param grupo - El grupo asociado al curso y etapa para el cual se desea obtener la asignatura
     * @param nombreAsignatura - El nombre de la asignatura para el cual se desea obtener la asignatura
     * @param horas - Las horas de la asignatura
     * @param esoBachillerato - Indica si es ESO o Bachillerato
     * @return Asignatura - La asignatura encontrada en la base de datos
     */
    private Asignatura asignarAlumnosRegistrarAsignatura(Integer curso, String etapa, Character grupo, String nombreAsignatura, Boolean esoBachillerato)
    {
        Asignatura asignatura = new Asignatura() ;

        // Primero buscamos la asignatura existente primero por su nombre, curso, etapa y grupo
        Optional<Asignatura> optionalAsignatura = this.iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(curso, etapa, nombreAsignatura, grupo);
        
        // Si existe, la asignamos
        if (optionalAsignatura.isPresent())
        {
            // Obtenemos la asignatura
            asignatura = optionalAsignatura.get();
        }
        else
        {
            // Buscamos la asignatura por su nombre, curso, etapa y grupo Z
            optionalAsignatura = this.iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(curso, etapa, nombreAsignatura, Constants.SIN_GRUPO_ASIGNADO);

            // Si existe, la asignamos
            if (optionalAsignatura.isPresent())
            {
                // Obtenemos la asignatura
                asignatura = optionalAsignatura.get() ;

                // La borramos
                this.iAsignaturaRepository.delete(asignatura);

                // Cambiamos el valor por el del grupo
                asignatura.getIdAsignatura().getCursoEtapaGrupo().getIdCursoEtapaGrupo().setGrupo(grupo) ;

                // Guardamos la asignatura
                this.iAsignaturaRepository.saveAndFlush(asignatura) ;
            }
            else // Si llegamos aquí, es porque no existe la asignatura
            {
                // Buscamos la asignatura para obtener las horas y los bloques
                Optional<HorasYBloquesDto> asignaturaExistente = this.iAsignaturaRepository.encontrarAsignaturaPorCursoEtapaNombre(curso, etapa, nombreAsignatura);

                // Creamos una instancia del la clave primaria de la asignatura
                IdAsignatura idAsignatura = new IdAsignatura();

                // Asignamos cada uno de los campos
                CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
                cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(curso, etapa, grupo));

                idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo);
                idAsignatura.setNombre(nombreAsignatura);

                // Indicamos si es ESO o Bachillerato
                asignatura.setEsoBachillerato(esoBachillerato) ;

                // Asignamos la clave primaria a la asignatura
                asignatura.setIdAsignatura(idAsignatura);

                // Asignamos las horas
                asignatura.setHoras(asignaturaExistente.get().getHoras());

                if(asignaturaExistente.get().getBloques() != null)
                {
                    // Creamos una instancia del bloque
                    Bloque bloque = new Bloque();
                    bloque.setId(asignaturaExistente.get().getBloques());

                    asignatura.setBloqueId(bloque);
                }

                // Guardamos la asignatura
                this.iAsignaturaRepository.saveAndFlush(asignatura) ;
            }
        }

        return asignatura ;
    }

    /**
     * Método para obtener la matricula
     * 
     * @param datosBrutoAlumnoMatriculaAsignaturaOpt - El objeto DatosBrutoAlumnoMatricula que contiene los datos del alumno a asignar
     * @param alumno - El alumno a asignar
     * @param asignatura - La asignatura a asignar
     */
    private void asignarAlumnosRegistrarMatricula(DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaAsignaturaOpt, Alumno alumno, Asignatura asignatura)
    {
        // Obtenemos la matricula
        IdMatricula idMatricula = new IdMatricula();
        idMatricula.setAsignatura(asignatura);
        idMatricula.setAlumno(alumno);

        Matricula matricula = new Matricula();
        matricula.setIdMatricula(idMatricula);

        datosBrutoAlumnoMatriculaAsignaturaOpt.setAsignado(true) ;

        this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(datosBrutoAlumnoMatriculaAsignaturaOpt) ;

        if(datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals("MATR") || 
           datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals("PEND"))
        { 
            // Guardar el registro en la tabla Matricula
            this.iMatriculaRepository.saveAndFlush(matricula);
        }
    }
}
