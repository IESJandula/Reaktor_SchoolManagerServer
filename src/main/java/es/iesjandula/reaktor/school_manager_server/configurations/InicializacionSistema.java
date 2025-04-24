package es.iesjandula.reaktor.school_manager_server.configurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import es.iesjandula.reaktor.base.resources_handler.ResourcesHandler;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerFile;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerJar;
import es.iesjandula.reaktor.base.utils.BaseException;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class InicializacionSistema
{
    @Autowired
    private ICursoEtapaRepository iCursoEtapaRepository ;
    
	@Autowired
	private IDepartamentoRepository iDepartamentoRepository ;
	
	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String modoDdl;

	/**
	 * Este método se encarga de inicializar el sistema ya sea en el entorno de desarrollo o ejecutando JAR
	 * @throws BaseException con un error
	 * @throws SchoolManagerServerException con un error
	 */
	@PostConstruct
	public void inicializarSistema() throws BaseException, SchoolManagerServerException
	{
		// Esta es la carpeta con las subcarpetas y configuraciones
	    ResourcesHandler schoolManagerServerConfig = this.getResourcesHandler(Constants.SCHOOL_MANAGER_SERVER_CONFIG);
	    
	    if (schoolManagerServerConfig != null)
	    {
	    	// Nombre de la carpeta destino
	    	File schoolManagerServerConfigExec = new File(Constants.SCHOOL_MANAGER_SERVER_CONFIG_EXEC) ;
	    	
	    	// Copiamos las plantillas (origen) al destino
	    	schoolManagerServerConfig.copyToDirectory(schoolManagerServerConfigExec) ;
	    }
		
		if (Constants.MODO_DDL_CREATE.equalsIgnoreCase(this.modoDdl))
		{
			// Parseamos los cursos y etapas
			this.cargarCursoEtapaDesdeCSVInternal() ;

			// Parseamos los departamentos
			this.cargarDepartamentosDesdeCSVInternal() ;
		}
	}
	
	/**
	 * 
	 * @param resourceFilePath con la carpeta origen que tiene las plantillas
	 * @return el manejador que crea la estructura
	 */
	private ResourcesHandler getResourcesHandler(String resourceFilePath)
	{
		ResourcesHandler outcome = null;

		URL baseDirSubfolderUrl = Thread.currentThread().getContextClassLoader().getResource(resourceFilePath);
		if (baseDirSubfolderUrl != null)
		{
			if (baseDirSubfolderUrl.getProtocol().equalsIgnoreCase("file"))
			{
				outcome = new ResourcesHandlerFile(baseDirSubfolderUrl);
			}
			else
			{
				outcome = new ResourcesHandlerJar(baseDirSubfolderUrl);
			}
		}
		
		return outcome;
	}
	
    /**
     * Carga cursos y etapas desde CSV - Internal
     * @throws SchoolManagerServerException excepción mientras se leían los cursos y etapas
     */
	private void cargarCursoEtapaDesdeCSVInternal() throws SchoolManagerServerException
	{
    	// Inicializamos la lista de cursos y etapas
        List<CursoEtapa> cursosEtapas = new ArrayList<CursoEtapa>() ;
        
        BufferedReader reader = null ;

        try
        {
            // Leer el archivo CSV desde la carpeta de recursos
            reader = new BufferedReader(new FileReader(ResourceUtils.getFile(Constants.FICHERO_CURSOS_ETAPAS), Charset.forName("UTF-8"))) ;
            
            // Nos saltamos la primera línea
            reader.readLine() ;

            // Leemos la segunda línea que ya tiene datos
            String linea = reader.readLine() ;
            
            while (linea != null)
            {
            	// Leemos la línea y la spliteamos
                String[] valores = linea.split(Constants.CSV_DELIMITER) ;

                // Crea una nueva instancia de CursoEtapa
                CursoEtapa cursoEtapa = new CursoEtapa();

                // Extrae y convierte el valor del curso (columna 0)
                int curso = Integer.parseInt(valores[0]);

                // Extrae el valor de la etapa (columna 1)
                String etapa = valores[1];    

                // Extrae el valor de la columna 2
                boolean esoBachillerato = Boolean.parseBoolean(valores[2]);

                // Creamos un objeto compuesto IdCursoEtapa
                IdCursoEtapa idCursoEtapa = new IdCursoEtapa(curso, etapa);

                // Asociamos el identificador al objeto CursoEtapa
                cursoEtapa.setIdCursoEtapa(idCursoEtapa);

                // Asociamos el valor de esoBachillerato al objeto CursoEtapa
                cursoEtapa.setEsoBachillerato(esoBachillerato);

    			// Añadimos a la lista
                cursosEtapas.add(cursoEtapa) ;
                
                // Leemos la siguiente línea
                linea = reader.readLine() ;
            }
        }
        catch (IOException ioException)
        {
			String errorString = "IOException mientras se leía línea de curso y etapa" ;
			
			log.error(errorString, ioException) ;
			throw new SchoolManagerServerException(Constants.ERR_CODE_PROCESANDO_CURSO_ETAPA, errorString, ioException) ;
        }
        finally
        {
        	this.cerrarFlujo(reader) ;
        }

        // Guardamos los cursos y etapas en la base de datos
        if (!cursosEtapas.isEmpty())
        {
            this.iCursoEtapaRepository.saveAllAndFlush(cursosEtapas) ;
        }
	}
	
    /**
     * Carga departamentos desde CSV - Internal
     * @throws SchoolManagerServerException excepción mientras se leían los departamentos
     */
	private void cargarDepartamentosDesdeCSVInternal() throws SchoolManagerServerException
	{
    	// Inicializamos la lista de departamentos
        List<Departamento> departamentos = new ArrayList<Departamento>() ;
        
        BufferedReader reader = null ;

        try
        {
            // Leer el archivo CSV desde la carpeta de recursos
            reader = new BufferedReader(new FileReader(ResourceUtils.getFile(Constants.FICHERO_DEPARTAMENTOS), Charset.forName("UTF-8"))) ;
            
            // Nos saltamos la primera línea
            reader.readLine() ;

            // Leemos la segunda línea que ya tiene datos
            String linea = reader.readLine() ;
            
            while (linea != null)
            {
            	// Leemos la línea y la spliteamos
                String[] valores = linea.split(Constants.CSV_DELIMITER) ;
                
                // Crea instancia de departamento
    			Departamento departamento = new Departamento();
    			
    			// Setea el departamento
    			departamento.setNombre(valores[0]);

    			// Añadimos a la lista
                departamentos.add(departamento) ;
                
                // Leemos la siguiente línea
                linea = reader.readLine() ;
            }
        }
        catch (IOException ioException)
        {
			String errorString = "IOException mientras se leía línea de departamento" ;
			
			log.error(errorString, ioException) ;
			throw new SchoolManagerServerException(Constants.ERR_CODE_PROCESANDO_CURSO_ETAPA, errorString, ioException) ;
        }
        finally
        {
        	this.cerrarFlujo(reader) ;
        }

        // Guardamos los departamentos en la base de datos
        if (!departamentos.isEmpty())
        {
            this.iDepartamentoRepository.saveAllAndFlush(departamentos) ;
        }
	}
	
	/**
	 * @param reader reader
	 * @throws SchoolManagerServerException excepción mientras se cerraba el reader
	 */
	private void cerrarFlujo(BufferedReader reader) throws SchoolManagerServerException
	{
		if (reader != null)
		{
		    try
		    {
		    	// Cierre del reader
				reader.close() ;
			}
		    catch (IOException ioException)
		    {
				String errorString = "IOException mientras se cerraba el reader" ;
				
				log.error(errorString, ioException) ;
				throw new SchoolManagerServerException(Constants.ERR_CODE_CIERRE_READER, errorString, ioException) ;
			}	
		}
	}
}
