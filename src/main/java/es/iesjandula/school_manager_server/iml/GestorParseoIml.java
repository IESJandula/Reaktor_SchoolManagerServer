package es.iesjandula.school_manager_server.iml;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.school_manager_server.interfaces.IGestorParseo;
import es.iesjandula.school_manager_server.interfaces.IParseoAsignaturas;
import es.iesjandula.school_manager_server.interfaces.IParseoDepartamentos;
import es.iesjandula.school_manager_server.utils.Constants;
import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;

@Service
public class GestorParseoIml implements IGestorParseo
{
	@Autowired
	private IParseoDepartamentos parseoDepartamentos;
	
	@Autowired
	private IParseoAsignaturas parseoAsignaturas;

	@Override
	public void parseaFichero(String nombreFichero) throws SchoolManagerServerException 
	{

		switch (nombreFichero) 
		{
		case Constants.NOMBRE_FICHERO_DEPARTAMENTO: 
		{
			
			Scanner scannerDepartamento = this.abrirFichero(nombreFichero);
			this.parseoDepartamentos.parseaFichero(scannerDepartamento);
			
			scannerDepartamento.close();
			break;
		}
		case Constants.NOMBRE_FICHERO_ASIGNATURAS: 
		{
			
			Scanner scannerAsignaturas = this.abrirFichero(nombreFichero);
			this.parseoAsignaturas.parseaFichero(scannerAsignaturas);
			
			scannerAsignaturas.close();
			break;
		}
		default:
			throw new SchoolManagerServerException(1, "Fichero " + nombreFichero + " no encontrado");
		}
	}
	
	private Scanner abrirFichero(String nombreFichero) throws SchoolManagerServerException
	{
		try
		{
			// Get file from resource
			File fichero = this.getFileFromResource(nombreFichero) ;
			
			return new Scanner(fichero) ;
		}
		catch (FileNotFoundException fileNotFoundException)
		{
			throw new SchoolManagerServerException(5, "Fichero " + nombreFichero + " no encontrado!", fileNotFoundException) ;
		}
		catch (URISyntaxException uriSyntaxException)
		{
			throw new SchoolManagerServerException(6, "Fichero " + nombreFichero + " no encontrado!", uriSyntaxException) ;
		}
		
	}
	
	private File getFileFromResource(String nombreFichero) throws URISyntaxException
	{
        ClassLoader classLoader = getClass().getClassLoader() ;
        
        URL resource = classLoader.getResource(nombreFichero) ;
        
        if (resource == null)
        {
            throw new IllegalArgumentException("Fichero no encontrado! " + nombreFichero) ;
        }

        return new File(resource.toURI()) ;
    }

}
