package es.iesjandula.school_manager_server.rest;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import es.iesjandula.school_manager_server.dtos.AlumnoDto3;
import es.iesjandula.school_manager_server.repositories.IAlumnoRepository;
import es.iesjandula.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.school_manager_server.repositories.IMatriculaRepository;
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
import es.iesjandula.school_manager_server.dtos.CursoEtapaDto;
import es.iesjandula.school_manager_server.interfaces.IParseoDatosBrutos;
import es.iesjandula.school_manager_server.models.CursoEtapa;
import es.iesjandula.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/cargarMatriculas")
public class Paso1CargarMatriculaController 
{
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
            String archivoCsvReadable = new String(archivoCsv.getBytes());

            // Declarar Scanner para realizar lectura del fichero
            Scanner scanner = new Scanner(archivoCsvReadable);

            // Registro cursoEtapa
            CursoEtapa cursoEtapa = new CursoEtapa();
            IdCursoEtapa idCursoEtapa = new IdCursoEtapa();

            // Asignar los campos al id de cursoEtapa
            idCursoEtapa.setCurso(curso);
            idCursoEtapa.setEtapa(etapa);

            // Asignar id al registro cursoEtapa
            cursoEtapa.setIdCursoEtapa(idCursoEtapa);

            // Llamar al Service IParseoDatosBrutos para realizar parseo
            this.iParseoDatosBrutos.parseoDatosBrutos(scanner, cursoEtapa);

            log.info("INFO - Se ha enviado todo correctamente");

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
    public ResponseEntity<?> obtenerDatosMatriculas()
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
    		
    		IdCursoEtapa idCursoEtapa = new IdCursoEtapa();
    		idCursoEtapa.setCurso(curso);
    		idCursoEtapa.setEtapa(etapa);
    		
    		CursoEtapa cursoEtapa = new CursoEtapa();
    		cursoEtapa.setIdCursoEtapa(idCursoEtapa);

            List<AlumnoDto3> alumnoDto3 = this.iDatosBrutoAlumnoMatriculaRepository.findDistinctAlumnosByCursoEtapa(idCursoEtapa.getCurso(),idCursoEtapa.getEtapa());

    		this.iDatosBrutoAlumnoMatriculaRepository.deleteDistinctByCursoEtapa(cursoEtapa);
            this.iMatriculaRepository.borrarPorCursoYEtapa(idCursoEtapa.getCurso(), idCursoEtapa.getEtapa());
            for(AlumnoDto3 a : alumnoDto3){
                this.iAlumnoRepository.deleteByNombreAndApellidos(a.getNombre(), a.getApellidos());
            }

            this.iAsignaturaRepository.borrarPorCursoYEtapa(idCursoEtapa.getCurso(), idCursoEtapa.getEtapa());

    		return ResponseEntity.ok().build();
    	}
    	catch (SchoolManagerServerException schoolManagerServerException) 
    	{
    		
    		return ResponseEntity.status(404).body(schoolManagerServerException.getBodyExceptionMessage());
    	}
    	
    }

}
