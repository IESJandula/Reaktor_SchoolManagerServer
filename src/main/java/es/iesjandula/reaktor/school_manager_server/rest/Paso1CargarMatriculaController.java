package es.iesjandula.reaktor.school_manager_server.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import es.iesjandula.reaktor.school_manager_server.models.*;
import es.iesjandula.reaktor.school_manager_server.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoDto3;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.DatosMatriculaDto;
import es.iesjandula.reaktor.school_manager_server.interfaces.IParseoDatosBrutos;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.services.CursoEtapaService;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/schoolManager/cargarMatriculas")
public class Paso1CargarMatriculaController 
{
	@Autowired
	private CursoEtapaService cursoEtapaService ;

    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    @Autowired
    private IParseoDatosBrutos iParseoDatosBrutos;

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private IMatriculaRepository iMatriculaRepository;

    @Autowired
    private IAlumnoRepository iAlumnoRepository;

	@Autowired
	private ICursoEtapaGrupoRepository iCursoEtapaGrupoRepository;

	@Autowired
	private IBloqueRepository iBloqueRepository;
    

    /**
     * Endpoint para cargar las matrículas a través de un archivo CSV.
     * 
     * Este método procesa un archivo CSV enviado como parte de una solicitud POST.
     * Si el archivo está vacío o hay un error durante el proceso, se lanzan
     * excepciones personalizadas.
     * 
     * @param archivoCsv - El archivo CSV que contiene las matrículas a procesar.
     * @param curso      - El identificador del curso al que se asignan las
     *                   matrículas.
     * @param etapa      - La etapa educativa (por ejemplo, "Primaria",
     *                   "Secundaria") asociada al curso.
     * @return ResponseEntity<?> - El mensaje de éxito o el detalle de un error
     *         ocurrido durante el procesamiento.
     * @throws IOException
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/matriculas", consumes = "multipart/form-data")
    public ResponseEntity<?> cargarMatriculas(
            @RequestParam(value = "csv", required = true) MultipartFile archivoCsv,
            @RequestHeader(value = "curso", required = true) Integer curso,
            @RequestHeader(value = "etapa", required = true) String etapa) 
    {
        try 
        {
            // Si el archivo esta vacio
            if (archivoCsv.isEmpty()) 
            {
                // Lanzar excepcion
                String msgError = "ERROR - El archivo importado está vacío";
                log.error(msgError);
                throw new SchoolManagerServerException(1, msgError);
            }

            // Convertir MultipartFile a String
            String archivoCsvReadable = new String(archivoCsv.getBytes(), StandardCharsets.UTF_8);

            // Declarar Scanner para realizar lectura del fichero
            Scanner scanner = new Scanner(archivoCsvReadable);

            // Obtenemos el cursoEtapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            // Llamar al Service IParseoDatosBrutos para realizar parseo
            this.iParseoDatosBrutos.parseoDatosBrutos(scanner, cursoEtapa);

            log.info("INFO - La matricula "+ curso + " - " + etapa + " se ha cargado correctamente");
            
            List<DatosBrutoAlumnoMatricula> listAsignaturas = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAsignaturaByCursoEtapa(cursoEtapa);
            
            if(listAsignaturas.isEmpty()) 
            {
            	String mensajeError = "No se ha asignaturas para ese curso y etapa";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(6, mensajeError);
            }

			CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
				
			cursoEtapaGrupo.setIdCursoEtapaGrupo(new IdCursoEtapaGrupo(curso, etapa, Constants.SIN_GRUPO_ASIGNADO));
			
			// Borramos el cursoEtapa si ya existen diferentes grupos
			this.iCursoEtapaGrupoRepository.borrarPorCursoEtapa(curso, etapa) ;
			
			// Guardamos el cursoEtapaGrupo
			this.iCursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo) ;
            
            for(DatosBrutoAlumnoMatricula datosAsignatura: listAsignaturas) 
            {            	
				IdAsignatura idAsignatura = new IdAsignatura();

            	idAsignatura.setCursoEtapaGrupo(cursoEtapaGrupo) ;
				idAsignatura.setNombre(datosAsignatura.getAsignatura());
            	
            	Asignatura asignatura = new Asignatura();
            	asignatura.setIdAsignatura(idAsignatura);
            	
            	this.iAsignaturaRepository.saveAndFlush(asignatura);
            }
            
            // Devolver OK informando que se ha insertado los registros
            return ResponseEntity.ok().build();
        } 
        catch (SchoolManagerServerException schoolManagerServerException) 
        {
            // Manejo de excepciones personalizadas
            log.error(schoolManagerServerException.getBodyExceptionMessage().toString());

            // Devolver la excepción personalizada con código 1 y el mensaje de error
            return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
        } 
        catch (IOException ioException) 
        {
            // Manejo de excepciones generales
            String msgError = "ERROR - No se pudo realizar la lectura del fichero";
            log.error(msgError, ioException);

            // Devolver una excepción personalizada con código 1, el mensaje de error y la
            // excepcion general
            SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(
                    1, msgError, ioException);
            return ResponseEntity.status(500).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
    
    /*Endpoint para que nos muestre los datos de los cursos que tienen matriculas*/
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/matriculas")
    public ResponseEntity<?> obtenerMatriculas()
    {
    	try 
    	{
    		List<CursoEtapaDto> listCursoEtapa = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosMatriculaPorEtapaYCurso();
    		
    		if(listCursoEtapa.isEmpty()) 
    		{
    			String mensajeError = "No se ha encontrado datos para ese curso y etapa";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(6, mensajeError);
    		}
    		
    		return ResponseEntity.ok(listCursoEtapa);
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{

    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    	
    }
    
    /* Endpoint que borrar la información asignada a las matriculas relacionada a un curso*/
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/matriculas")
    public ResponseEntity<?> borrarDatosMatriculas(@RequestHeader(value = "curso", required = true) Integer curso,
    											   @RequestHeader(value = "etapa", required = true) String etapa)
    {
    	try 
    	{
    		
    		List<CursoEtapaDto> listAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAlumnosMatriculaPorEtapaYCurso(curso, etapa);

    		if(listAlumnoMatriculas.isEmpty()) 
    		{
    			String mensajeError = "No se ha encontrado datos para ese curso y etapa";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(6, mensajeError);
    		}
    		
            // Obtenemos el cursoEtapa
            CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

            List<AlumnoDto3> alumnoDto3 = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAlumnosByCursoEtapa(curso, etapa);

    		this.iDatosBrutoAlumnoMatriculaRepository.deleteDistinctByCursoEtapa(cursoEtapa);
            this.iMatriculaRepository.borrarPorCursoYEtapa(curso, etapa);
            for(AlumnoDto3 a : alumnoDto3){
                this.iAlumnoRepository.deleteByNombreAndApellidos(a.getNombre(), a.getApellidos());
            }

			List<Long> bloques = this.iAsignaturaRepository.encontrarBloquePorCursoEtapa(curso, etapa);

			this.iAsignaturaRepository.borrarPorCursoYEtapa(curso, etapa);

			Bloque bloque = new Bloque();
			for (Long cantidadBloques : bloques) {
				bloque.setId(cantidadBloques);
				this.iBloqueRepository.delete(bloque);
			}


    		return ResponseEntity.ok().build();
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{
    		
    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    	
    }
    
    /*Endpoint para que nos muestre los datos de las matriculas según un curso y una etapa*/
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/datosMatriculas")
    public ResponseEntity<?> obtenerDatosMatriculas(@RequestHeader(value = "curso", required = true) Integer curso,
			   										@RequestHeader(value = "etapa", required = true) String etapa)
    {
    	try 
    	{
    		
    		List<DatosMatriculaDto> listDatosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarDatosMatriculaPorCursoYEtapa(curso, etapa);
    		
    		if(listDatosBrutoAlumnoMatriculas.isEmpty()) 
    		{
    			String mensajeError = "No se ha encontrado datos para ese curso y etapa";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(6, mensajeError);
    		}
    		
    		return ResponseEntity.ok(listDatosBrutoAlumnoMatriculas);
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{

    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    	
    }
    
    
    /*Endpoint para que nos muestre los datos de las matriculas según un curso y una etapa*/
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.PUT, value = "/datosMatriculas")
    public ResponseEntity<?> matricularAsignatura(@RequestHeader(value = "nombre") String nombre,
												  @RequestHeader(value = "apellidos") String apellidos,
												  @RequestHeader(value = "asignatura") String asignatura,
												  @RequestHeader(value = "curso") Integer curso,
												  @RequestHeader(value = "etapa") String etapa,
												  @RequestHeader(value = "estado") String estado)
    {
    	try 
    	{
    		
    		DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(nombre, apellidos, asignatura, curso, etapa);
    		
    		if(datosBrutoAlumnoMatriculas.getEstadoMatricula() == "MATR") 
    		{
    			String mensajeError = "Ya existe un alumno matriculado en esa asignatura";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(6, mensajeError);
    		}
    		
			// Obtenemos el cursoEtapa
			CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);
			
			datosBrutoAlumnoMatriculas.setNombre(nombre);
			datosBrutoAlumnoMatriculas.setNombre(nombre);
			datosBrutoAlumnoMatriculas.setApellidos(apellidos);
			datosBrutoAlumnoMatriculas.setAsignatura(asignatura);
			datosBrutoAlumnoMatriculas.setCursoEtapa(cursoEtapa);
			datosBrutoAlumnoMatriculas.setEstadoMatricula(estado);
    		
    		this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(datosBrutoAlumnoMatriculas);
    		
    		return ResponseEntity.ok().build();
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{
    		
    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    	
    }
    
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.POST, value = "/datosMatriculas")
    public ResponseEntity<?> matricularAlumno(@RequestHeader(value = "nombre") String nombre,
											  @RequestHeader(value = "apellidos") String apellidos,
											  @RequestHeader(value = "asignatura") String asignatura,
											  @RequestHeader(value = "curso") Integer curso,
											  @RequestHeader(value = "etapa") String etapa,
											  @RequestHeader(value = "estado") String estado)
    {
    	try 
    	{
    		
    		DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(nombre, apellidos, asignatura, curso, etapa);
    		
    		if(datosBrutoAlumnoMatriculas != null) 
    		{
    			String mensajeError = "Ya existe un alumno matriculado con ese nombre y esas asignaturas";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(6, mensajeError);
    		}
    		
			// Obtenemos el cursoEtapa
			CursoEtapa cursoEtapa = this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);
			
    		DatosBrutoAlumnoMatricula nuevosDatosBrutoAlumnoMatricula = new DatosBrutoAlumnoMatricula();
    		
    		nuevosDatosBrutoAlumnoMatricula.setNombre(nombre);
    		nuevosDatosBrutoAlumnoMatricula.setNombre(nombre);
    		nuevosDatosBrutoAlumnoMatricula.setApellidos(apellidos);
    		nuevosDatosBrutoAlumnoMatricula.setAsignatura(asignatura);
    		nuevosDatosBrutoAlumnoMatricula.setCursoEtapa(cursoEtapa);
    		nuevosDatosBrutoAlumnoMatricula.setEstadoMatricula(estado);
    		
    		this.iDatosBrutoAlumnoMatriculaRepository.saveAndFlush(nuevosDatosBrutoAlumnoMatricula);
    		
    		return ResponseEntity.ok().build();
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{
    		
    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    }

    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.DELETE, value = "/datosMatriculas")
    public ResponseEntity<?> desmatricularAlumno(@RequestHeader(value = "nombre") String nombre,
									    		 @RequestHeader(value = "apellidos") String apellidos,
									    		 @RequestHeader(value = "asignatura") String asignatura,
									    		 @RequestHeader(value = "curso") Integer curso,
									    		 @RequestHeader(value = "etapa") String etapa,
									    		 @RequestHeader(value = "estado") String estado)
    {
    	try 
    	{
    		
    		DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculas = this.iDatosBrutoAlumnoMatriculaRepository.encontrarAsignaturaPorNombreYApellidosYAsignaturaYCursoYEtapa(nombre, apellidos, asignatura, curso, etapa);
    		
    		if(datosBrutoAlumnoMatriculas == null) 
    		{
    			String mensajeError = "No existe un alumno matriculado con ese nombre y esas asignaturas";
    			
    			log.error(mensajeError);
    			throw new SchoolManagerServerException(6, mensajeError);
    		}
    		
    		IdCursoEtapa idCursoEtapa = new IdCursoEtapa();
    		idCursoEtapa.setCurso(curso);
    		idCursoEtapa.setEtapa(etapa);
    		
    		CursoEtapa cursoEtapa = new CursoEtapa();
    		cursoEtapa.setIdCursoEtapa(idCursoEtapa);
    		
    		DatosBrutoAlumnoMatricula datosBrutoAlumnoMatriculaABorrar = new DatosBrutoAlumnoMatricula();
    		
    		datosBrutoAlumnoMatriculaABorrar.setNombre(nombre);
    		datosBrutoAlumnoMatriculaABorrar.setApellidos(apellidos);
    		datosBrutoAlumnoMatriculaABorrar.setAsignatura(asignatura);
    		datosBrutoAlumnoMatriculaABorrar.setCursoEtapa(cursoEtapa);
    		datosBrutoAlumnoMatriculaABorrar.setEstadoMatricula(estado);
    		datosBrutoAlumnoMatriculaABorrar.setAsignado(false);
    		
    		this.iDatosBrutoAlumnoMatriculaRepository.deleteByNombreAndApellidosAndAsignaturaAndEstadoMatriculaAndCursoEtapa(datosBrutoAlumnoMatriculaABorrar.getNombre(),
    				datosBrutoAlumnoMatriculaABorrar.getApellidos(), datosBrutoAlumnoMatriculaABorrar.getAsignatura(), datosBrutoAlumnoMatriculaABorrar.getEstadoMatricula(), cursoEtapa);
    		
    		return ResponseEntity.ok().build();
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{
    		
    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    }
    
}
