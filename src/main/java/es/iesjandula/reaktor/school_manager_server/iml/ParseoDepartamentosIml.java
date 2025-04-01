package es.iesjandula.reaktor.school_manager_server.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.interfaces.IParseoDepartamentos;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;

@Service
public class ParseoDepartamentosIml implements IParseoDepartamentos
{
	@Autowired
	private IDepartamentoRepository iDepartamentoRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws SchoolManagerServerException 
	{

		scanner.nextLine();
		
		while(scanner.hasNextLine()) 
		{
			
			String lineaDelFichero = scanner.nextLine();
			
			String [] lineaDelFicheroTroceada = lineaDelFichero.split(Constants.CSV_DELIMITER);
			
			Departamento departamento = new Departamento();
			
			departamento.setNombre(lineaDelFicheroTroceada[0]);
			
			this.iDepartamentoRepository.saveAndFlush(departamento);
			
		}
	}

}
