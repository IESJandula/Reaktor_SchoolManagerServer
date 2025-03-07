package es.iesjandula.school_manager_server.iml;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.school_manager_server.interfaces.IParseoAsignaturas;
import es.iesjandula.school_manager_server.models.Asignatura;
import es.iesjandula.school_manager_server.models.Departamento;
import es.iesjandula.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.school_manager_server.utils.Constants;
import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ParseoAsignaturasIml implements IParseoAsignaturas 
{

	@Autowired
	private IAsignaturaRepository iAsignaturaRepository;
	
	@Autowired
	private IDepartamentoRepository iDepartamentoRepository;
	
	
	@Override
	public void parseaFichero(Scanner scanner) throws SchoolManagerServerException 
	{

		scanner.nextLine();

		while (scanner.hasNextLine()) 
		{

			String lineaDelFichero = scanner.nextLine().trim();
			
			String[] lineaDelFicheroTroceada = lineaDelFichero.split(Constants.CSV_DELIMITER);
			
			IdAsignatura idAsignatura = new IdAsignatura();
			idAsignatura.setCurso(Integer.valueOf(lineaDelFicheroTroceada[0]));
			idAsignatura.setEtapa(lineaDelFicheroTroceada[1]);
			idAsignatura.setGrupo(lineaDelFicheroTroceada[2]);
			idAsignatura.setNombre(lineaDelFicheroTroceada[3]);
			
			Optional<Departamento> optionalDepartamentoPropietario = this.iDepartamentoRepository.findById(lineaDelFicheroTroceada[4]);
			if(!optionalDepartamentoPropietario.isPresent()) 
			{
				String mensajeError = "No existe el departamento";
				log.error(mensajeError);
				throw new SchoolManagerServerException(2, mensajeError);
			}
			
			Optional<Departamento> optionalDepartamentoReceptor = this.iDepartamentoRepository.findById(lineaDelFicheroTroceada[5]);
			if(!optionalDepartamentoReceptor.isPresent()) 
			{
				String mensajeError = "No existe el departamento";
				log.error(mensajeError);
				throw new SchoolManagerServerException(3, mensajeError);
			}
			
			Asignatura asignatura= new Asignatura();
			asignatura.setId(idAsignatura);
			asignatura.setDepartamentoPropietario(optionalDepartamentoPropietario.get());
			asignatura.setDepartamentoReceptor(optionalDepartamentoReceptor.get());

			this.iAsignaturaRepository.saveAndFlush(asignatura);

		}
	}

}
