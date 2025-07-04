package es.iesjandula.reaktor.school_manager_server.configurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.iesjandula.reaktor.school_manager_server.models.Constantes;
import es.iesjandula.reaktor.school_manager_server.repositories.IConstantesRepository;
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
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDiaTramoTipoHorarioRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class InicializacionSistema
{
    @Autowired
    private ICursoEtapaRepository cursoEtapaRepository ;
    
	@Autowired
	private IDepartamentoRepository departamentoRepository ;
	
	@Autowired
	private IDiaTramoTipoHorarioRepository diaTramoTipoHorarioRepository ;

	@Autowired
	private IConstantesRepository iConstantesRepository;
	
	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String modoDdl;

	@Value("${" + Constants.PARAM_YAML_REINICIAR_CONSTANTES + "}")
	private Boolean reiniciarConstantes;

	@Value("${" + Constants.PARAM_YAML_SELECCION_HORARIOS_POR_CLAUSTRO + "}")
	private String seleccionHorariosPorClaustro;

	@Value("${" + Constants.PARAM_YAML_UMBRAL_MINIMO_SOLUCION + "}")
	private String umbralMinimoSolucion;

	@Value("${" + Constants.PARAM_YAML_UMBRAL_MINIMO_ERROR + "}")
	private String umbralMinimoError;

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

			// Parseamos los dias, tramos y tipo de horario
			this.cargarDiasTramosTipoHorarioDesdeCSVInternal() ;
		}

		if(Constants.MODO_INICIALIZAR_SISTEMA.equals(String.valueOf(this.reiniciarConstantes)))
		{
			this.inicializarSistemaConConstantes();
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
            this.cursoEtapaRepository.saveAllAndFlush(cursosEtapas) ;
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
            this.departamentoRepository.saveAllAndFlush(departamentos) ;
        }
	}
	
	/**
	 * Carga dias, tramos y tipo de horario desde CSV - Internal
	 * @throws SchoolManagerServerException excepción mientras se leían los dias, tramos y tipo de horario
	 */
	private void cargarDiasTramosTipoHorarioDesdeCSVInternal() throws SchoolManagerServerException
	{
		// Inicializamos la lista de dias, tramos y tipo de horario
		List<DiaTramoTipoHorario> diaTramoTipoHorarioList = new ArrayList<DiaTramoTipoHorario>() ;
		
		BufferedReader reader = null ;
		
		try
		{
			// Leer el archivo CSV desde la carpeta de recursos
			reader = new BufferedReader(new FileReader(ResourceUtils.getFile(Constants.FICHERO_DIAS_TRAMOS_TIPO_HORARIO), Charset.forName("UTF-8"))) ;
			
			// Nos saltamos la primera línea
			reader.readLine() ;	
			
			// Leemos la segunda línea que ya tiene datos
			String linea = reader.readLine() ;
			
			while (linea != null)
			{
				// Leemos la línea y la spliteamos
				String[] valores = linea.split(Constants.CSV_DELIMITER) ;

				// Obtenemos los valores de los dias y tramos en formato número y descriptivo, y el tipo de horario
				int dia 		   		= Integer.parseInt(valores[0]) ;
				String diasDesc    		= valores[1] ;
				int tramo 		   		= Integer.parseInt(valores[2]) ;
				String tramosDesc  		= valores[3] ;

				Boolean horarioMatutino = null ;
				if (valores.length > 4 && valores[4] != null)
				{
					horarioMatutino = Boolean.parseBoolean(valores[4]) ;
				}

				// Creamos un objeto DiaTramoTipoHorario
				DiaTramoTipoHorario diaTramoTipoHorario = new DiaTramoTipoHorario() ;

				// Asociamos los valores al objeto DiaTramoTipoHorario
				diaTramoTipoHorario.setDia(dia) ;
				diaTramoTipoHorario.setTramo(tramo) ;
				diaTramoTipoHorario.setDiaDesc(diasDesc) ;
				diaTramoTipoHorario.setTramoDesc(tramosDesc) ;
				diaTramoTipoHorario.setHorarioMatutino(horarioMatutino) ;

				// Añadimos a la lista
				diaTramoTipoHorarioList.add(diaTramoTipoHorario) ;

				// Leemos la siguiente línea
				linea = reader.readLine() ;
			}	

		}
		catch (IOException ioException)
		{
			String errorString = "IOException mientras se leía línea de dias, tramos y tipo de horario" ;
			
			log.error(errorString, ioException) ;
			throw new SchoolManagerServerException(Constants.ERR_CODE_PROCESANDO_CURSO_ETAPA, errorString, ioException) ;
		}
		finally
		{
			this.cerrarFlujo(reader) ;
		}

		// Guardamos los dias, tramos y tipo de horario en la base de datos
		if (!diaTramoTipoHorarioList.isEmpty())
		{
			this.diaTramoTipoHorarioRepository.saveAllAndFlush(diaTramoTipoHorarioList) ;
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

	/**
	 * Este método se encarga de inicializar el sistema con las constantes siempre
	 * que estemos creando la base de datos ya sea en el entorno de desarrollo o
	 * ejecutando JAR
	 */
	private void inicializarSistemaConConstantes()
	{
		this.cargarPropiedad(Constants.TABLA_CONST_SELECCION_HORARIOS_POR_CLAUSTRO, this.seleccionHorariosPorClaustro);
		this.cargarPropiedad(Constants.TABLA_CONST_UMBRAL_MINIMO_SOLUCION, this.umbralMinimoSolucion);
		this.cargarPropiedad(Constants.TABLA_CONST_UMBRAL_MINIMO_ERROR, this.umbralMinimoError);
	}

	private void cargarPropiedad(String key, String value)
	{
		// Verificamos si tiene algún valor
		Optional<Constantes> property = this.iConstantesRepository.findById(key);

		// Si está vacío, lo seteamos con el valor del YAML
		if (property.isEmpty())
		{
			Constantes constante = new Constantes();

			constante.setClave(key);
			constante.setValor(value);

			// Almacenamos la constante en BBDD
			this.iConstantesRepository.save(constante);
		}
	}
}
