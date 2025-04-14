package es.iesjandula.reaktor.school_manager_server.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto2;
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto3;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.MatriculaDto;
import es.iesjandula.reaktor.school_manager_server.models.Alumno;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;
import es.iesjandula.reaktor.school_manager_server.models.Matricula;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IMatriculaRepository;
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
    private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    @Autowired
    private IAlumnoRepository iAlumnoRepository;
    
    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;
    
    @Autowired
    private IMatriculaRepository iMatriculaRepository;

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
                // Lanzar excepcion y mostrar log con mensaje diferente
                log.error("ERROR - Lista vacía");
                throw new SchoolManagerServerException(404, "ERROR - No se ha encontrado ningun curso");
            }

            // Devolver la lista
            log.info("INFO - Lista de los cursos etapas");
            return ResponseEntity.status(200).body(listaCursoEtapa);
        } 
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Manejo de excepciones personalizadas
            log.error(schoolManagerServerException.getBodyExceptionMessage().toString());

            // Devolver la excepción personalizada con código 1 y el mensaje de error
            return ResponseEntity.status(404).body(schoolManagerServerException);
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo cargar la lista";
            log.error(msgError, exception);

            // Devolver la excepción personalizada con código 1, el mensaje de error y la
            // excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException);
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
    public ResponseEntity<?> crearGrupo(
            @RequestHeader(value = "curso", required = true) Integer curso,
            @RequestHeader(value = "etapa", required = true) String etapa) 
    {
        try 
        {
            // Numero de veces repetido el Curso Etapa en la BD
            int contador = this.iCursoEtapaGrupoRepository.findCountByCursoAndEtapa(curso, etapa);

            // Asignar la letra A
            char grupo = Constants.GROUP;

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

            // Insertar en BD
            this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);

            // Log de información antes de la respuesta
            log.info("INFO - Grupo creado correctamente para el curso: {} y etapa: {}", curso, etapa);

            // Devolver la respuesta indicando que el grupo ha sido creado correctamente
            return ResponseEntity.ok().build();
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se ha podido crear el grupo";
            log.error(msgError, exception);

            // Devolver una excepción personalizada con código 1, el mensaje de error y la
            // excepcion general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
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
    public ResponseEntity<?> obtenerGrupo(
            @RequestHeader(value = "curso", required = true) Integer curso,
            @RequestHeader(value = "etapa", required = true) String etapa) 
    {
        try 
        {
            // Crear el objeto CursoEtapa con los parámetros recibidos
            CursoEtapa cursoEtapa = new CursoEtapa();
            IdCursoEtapa idCursoEtapa = new IdCursoEtapa(curso, etapa);
            cursoEtapa.setIdCursoEtapa(idCursoEtapa);

            // Obtener la lista de grupos según curso y etapa
            List<CursoEtapaGrupoDto> cursosEtapasGrupos = this.iCursoEtapaGrupoRepository.findGrupoByCursoAndEtapa(curso, etapa);

            // Si la lista está vacía, lanzar una excepción
            if (cursosEtapasGrupos.isEmpty()) 
            {
                // Lanzar excepcion y mostrar log con mensaje de Error
                String msgError = "ERROR - No se encontraron grupos para el curso {} y etapa {}";
                log.error(msgError, curso, etapa);
                throw new SchoolManagerServerException(1,
                        "ERROR - No se encontraron grupos para el curso y etapa especificados");
            }

            // Log de información antes de la respuesta
            log.info("INFO - Se han encontrado los siguientes grupos para el curso: {} y etapa: {}", curso, etapa);

            // Devolver la lista de cursos, etapas y grupos encontrados
            return ResponseEntity.status(200).body(cursosEtapasGrupos);
        } 
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Manejo de excepciones personalizadas
            log.error(schoolManagerServerException.getBodyExceptionMessage().toString());

            // Devolver la excepción personalizada con código 1 y el mensaje de error
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudieron buscar los grupos";
            log.error(msgError, exception);

            // Devolver una excepción personalizada con código 1, el mensaje de error y la
            // excepcion general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
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
    public ResponseEntity<?> obtenerAlumnosConGrupo(
            @RequestHeader(value = "curso", required = true) Integer curso,
            @RequestHeader(value = "etapa", required = true) String etapa,
            @RequestHeader(value = "grupo", required = true) Character grupo)
    {
        try
        {
            // Crear la lista de Alumnos a devolver
            List<Integer> idsDeAlumnosDelGrupo = this.iMatriculaRepository
                    .encontrarIdAlumnoPorCursoEtapaYGrupo(curso, etapa, grupo);

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
                String msgError = "ERROR - Lista sin alumnos encontrados";
                log.error(msgError);
                throw new SchoolManagerServerException(1, msgError);
            }

            // Log de información antes de la respuesta
            log.info("INFO - Lista con nombres y apellidos de los alumnos asignados y pendientes de asignar");

            // Devolver la lista de Alumnos
            return ResponseEntity.status(200).body(alumnosEnGrupo);
        }
        catch (SchoolManagerServerException schoolManagerServerException)
        {
            // Manejo de excepciones personalizadas
            log.error(schoolManagerServerException.getBodyExceptionMessage().toString());

            // Devolver la excepción personalizada con código 1 y el mensaje de error
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo obtener la lista de alumnos";
            log.error(msgError, exception);

            // Devolver una excepción personalizada con código 1, el mensaje de error y la
            // excepción general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
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
    			throw new SchoolManagerServerException(6, mensajeError);
        	}
    		return ResponseEntity.ok(listaDatosBrutoAlumnoMatriculas);
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{

    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    	
    }


    /**
     * Endpoint para asignar una lista de alumnos a un grupo específico de un curso
     * y etapa.
     * 
     * Este método asigna los alumnos, que son recibidos como una lista de objetos
     * `AlumnoDto`,
     * a un grupo específico dentro de un curso y etapa determinados. Si el alumno
     * ya existe en
     * la tabla `DatosBrutoAlumnoMatricula`, se transfiere el alumno al grupo
     * especificado y
     * luego se elimina de la tabla `DatosBrutoAlumnoMatricula`.
     * 
     * @param alumnos - Lista de objetos `AlumnoDto` que contiene los datos de los
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
            // Crear el objeto CursoEtapaGrupo con los parámetros recibidos
            IdCursoEtapa idCursoEtapa = new IdCursoEtapa(curso, etapa);
            CursoEtapa cursoEtapa = new CursoEtapa(idCursoEtapa);

            // Por cada alumno buscarlo en DatosBrutosAlumnoMatricula y añadirlos a
            // DatosBrutosAlumnoMatriculaGrupo
            for (AlumnoDto2 alumnoDatosBrutos : alumnos) 
            {
                if (alumnoDatosBrutos.getGrupo() != grupo && alumnoDatosBrutos.getGrupo() != null) 
                {
                    continue;
                }
                // Optional de DatosBrutoAlumnoMatriculaEntity
                List<DatosBrutoAlumnoMatricula> datosBrutoAlumnoMatriculaAsignaturasOpt;

                // Buscar los registros del alumno en DatosBrutosAlumnoMatricula
                datosBrutoAlumnoMatriculaAsignaturasOpt = this.iDatosBrutoAlumnoMatriculaRepository
                        .findByNombreAndApellidosAndCursoEtapa(alumnoDatosBrutos.getNombre(), alumnoDatosBrutos.getApellidos(),cursoEtapa);

                // Crear registro de la Tabla Alumno
                Alumno alumno = new Alumno();
                
                for (DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaAsignaturaOpt : datosBrutoAlumnoMatriculaAsignaturasOpt) 
                {
                    // Asignar cada uno de los campos
                    alumno.setNombre(datosBrutoAlumnoMatriculaAsignaturaOpt.getNombre());
                    alumno.setApellidos(datosBrutoAlumnoMatriculaAsignaturaOpt.getApellidos());
                    
                    // Guardar el registro en la tabla Alumno
                    this.iAlumnoRepository.saveAndFlush(alumno);
                    
                    // Crear registro de la Tabla Alumno
                    IdAsignatura idAsignatura = new IdAsignatura();
                    // Asignar cada uno de los campos
                    idAsignatura.setCurso(curso);
                    idAsignatura.setEtapa(etapa);
                    idAsignatura.setGrupo(grupo);
                    idAsignatura.setNombre(datosBrutoAlumnoMatriculaAsignaturaOpt.getAsignatura());

                    // Buscar la asignatura existente
                    Optional<Asignatura> optionalAsignatura = iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(curso, etapa, idAsignatura.getNombre(), grupo);
                    if (!optionalAsignatura.isPresent()) {
                        optionalAsignatura = iAsignaturaRepository.encontrarAsignaturaPorNombreYCursoYEtapaYGrupo(curso, etapa, idAsignatura.getNombre(), 'N');
                    }
                    Asignatura asignatura = new Asignatura();
                    
                    if(optionalAsignatura.isPresent())
                    {
                    	Asignatura exitente = optionalAsignatura.get();
                    	if(exitente.getIdAsignatura().getGrupo() == 'N') 
                    	{
                    		asignatura.setHoras(exitente.getHoras());
                            log.error(String.valueOf(optionalAsignatura.get().getHoras()));
                    		this.iAsignaturaRepository.delete(exitente);
                    		asignatura.setMatriculas(new ArrayList<>()); //Si no la creamos con los datos de arriba y un array vacio para evitar nulos
                    	}
                    	else
                    	{
                            asignatura.setHoras(exitente.getHoras());
                    		asignatura.setMatriculas(exitente.getMatriculas());
                    	}
                    	
                        asignatura.setIdAsignatura(idAsignatura);
                    } 
                    else 
                    {
                    	asignatura.setIdAsignatura(idAsignatura);
                        asignatura.setMatriculas(new ArrayList<>()); //Si no la creamos con los datos de arriba y un array vacio para evitar nulos
                    }

                    // Crear la matricula
                    IdMatricula idMatricula = new IdMatricula();
                    idMatricula.setAsignatura(asignatura);
                    idMatricula.setAlumno(alumno);

                    Matricula matricula = new Matricula();
                    matricula.setIdMatricula(idMatricula);

                    // Guardar la asignatura, lo que actualizará la colección de matriculas
                    this.iAsignaturaRepository.saveAndFlush(asignatura);

                    datosBrutoAlumnoMatriculaAsignaturaOpt.setAsignado(true);
                    this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(datosBrutoAlumnoMatriculaAsignaturaOpt);
                    
                    if(datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals("MATR") || datosBrutoAlumnoMatriculaAsignaturaOpt.getEstadoMatricula().equals("PEND")) {
                    	
                    	// Guardar el registro en la tabla Matricula
                    	this.iMatriculaRepository.saveAndFlush(matricula);
                    }
                }

            }

            // Log de información antes de la respuesta
            log.info("INFO - Alumnos asignados correctamente al grupo {} para el curso {} y etapa {}", grupo, curso,
                    etapa);

            // Devolver mensaje de OK
            return ResponseEntity.ok().build();
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudieron asignar los alumnos al grupo";
            log.error(msgError, exception);

            // Devolver una excepción personalizada con código 1, el mensaje de error y la
            // excepcion general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, exception);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
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
    @Modifying
    public ResponseEntity<?> borrarAlumno(@RequestBody AlumnoDto alumnoDto) 
    {
        try 
        {

            List<MatriculaDto> listaAlumnosABorrar = this.iMatriculaRepository.encontrarAlumnoPorNombreYApellidos(alumnoDto.getNombre(), alumnoDto.getApellidos());
            
            // Crear registro de la Tabla Alumno
            Alumno alumno = new Alumno();
            
            // Por cada asignatura del Alumno
            for(MatriculaDto alumnoABorrar : listaAlumnosABorrar) 
            {
            	
            	// Si no existe el registro
            	if(alumnoABorrar == null) 
            	{
            		// Lanzar excepcion y mostrar log con mensaje de Error
            		String mensajeError = "ERROR - No se encontraron los datos del alumno";
            		log.error(mensajeError);
                    throw new SchoolManagerServerException(1, mensajeError);
            	}
            	
            	alumno.setNombre(alumnoDto.getNombre());
            	alumno.setApellidos(alumnoDto.getApellidos());
            	
            	IdAsignatura idAsignatura = new IdAsignatura();
            	idAsignatura.setCurso(alumnoABorrar.getCurso());
            	idAsignatura.setEtapa(alumnoABorrar.getEtapa());
            	idAsignatura.setGrupo(alumnoABorrar.getGrupo());
            	idAsignatura.setNombre(alumnoABorrar.getNombreAsignatura());
            	
            	Asignatura asignatura = new Asignatura();
            	asignatura.setIdAsignatura(idAsignatura);
                asignatura.setHoras(alumnoABorrar.getHoras());

                IdMatricula idMatricula = new IdMatricula();
                idMatricula.setAlumno(alumno);
                idMatricula.setAsignatura(asignatura);

                Matricula matricula = new Matricula();
                matricula.setIdMatricula(idMatricula);

                IdCursoEtapa idCursoEtapa = new IdCursoEtapa(alumnoABorrar.getCurso(),alumnoABorrar.getEtapa());
                CursoEtapa cursoEtapa = new CursoEtapa(idCursoEtapa);

                List<Integer> listIdAlumno = this.iMatriculaRepository.encontrarIdAlumnoPorCursoEtapaGrupoYNombre(alumnoABorrar.getCurso(),alumnoABorrar.getEtapa(),alumnoABorrar.getGrupo(),alumnoABorrar.getNombreAlumno());

                for(Integer idAlumno : listIdAlumno)
                {

//            		Eliminar el registro en la tabla Asignatura
                    this.iMatriculaRepository.borrarPorTodo(alumnoABorrar.getCurso(),alumnoABorrar.getEtapa(), alumnoABorrar.getNombreAsignatura(),idAlumno);
                }

//            	Si es el ultimo alumno
                if(this.iMatriculaRepository.numeroAsignaturasPorNombre(idAsignatura.getNombre()) < 1)
            	{

                    // Eliminar el registro en la tabla Asignatura
                    this.iAsignaturaRepository.delete(asignatura);

                    idAsignatura.setGrupo('N');
                    asignatura.setIdAsignatura(idAsignatura);
                    asignatura.setHoras(alumnoABorrar.getHoras());
//            		Volvemos a crear la asignatura con el grupo a "null"
                    this.iAsignaturaRepository.saveAndFlush(asignatura);
            	}


                List<DatosBrutoAlumnoMatricula> datosBrutoAlumnoMatricula = this.iDatosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(alumnoABorrar.getNombreAlumno(),alumnoABorrar.getApellidosAlumno(),cursoEtapa);

                for(DatosBrutoAlumnoMatricula datosAlumnoBorrado : datosBrutoAlumnoMatricula) 
                {
                    datosAlumnoBorrado.setAsignado(false);
                }
                // Guardar el registro en la tabla DatosBrutoAlumnoMatricula
                this.iDatosBrutoAlumnoMatriculaRepository.saveAllAndFlush(datosBrutoAlumnoMatricula);

            	
            }

            this.iAlumnoRepository.deleteByNombreAndApellidos(alumno.getNombre(), alumno.getApellidos());
            
            
            // Log de información antes de la respuesta
            log.info("INFO - Alumno desasignado correctamente");

            // Devolver mensaje de OK
            return ResponseEntity.ok().build();
        } 
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Manejo de excepciones personalizadas
            log.error(schoolManagerServerException.getBodyExceptionMessage().toString());

            // Devolver la excepción personalizada con código 1 y el mensaje de error
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo desasignar el alumno del grupo";
            log.error(msgError, exception);

            // Devolver una excepción personalizada con código 1, el mensaje de error y la
            // excepcion general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(1, msgError, exception);
            
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
