package es.iesjandula.school_manager_server.parsers;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.school_manager_server.models.CursoEtapa;
import es.iesjandula.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.school_manager_server.utils.Constants;
import es.iesjandula.school_manager_server.utils.MatriculasHorariosServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase de implementación - ParseoCursoEtapaImpl
 * -----------------------------------------------------------------------------------------------------------------
 * Implementa la interfaz {@link IParseoCursoEtapa} para procesar datos en bruto relacionados con 
 * la entidad {@link CursoEtapa}. Esta clase utiliza el repositorio {@link ICursoEtapaRepository} para 
 * persistir los datos extraídos.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Slf4j
@Service
public class ParseoCursoEtapaImpl implements IParseoCursoEtapa
{
    @Autowired
    private ICursoEtapaRepository iCursoEtapaRepository;

    /**
     * Implementación del método para parsear y persistir datos de "CursoEtapa".
     * 
     * @param scanner 							 - Una instancia de {@link Scanner} que contiene los datos en bruto en formato CSV.
     * @throws MatriculasHorariosServerException - Si ocurre algún error durante el parseo, como un formato de datos incorrecto.
     */
    @Override
    public void parseoCursoEtapa(Scanner scanner) throws MatriculasHorariosServerException 
    {   
        try 
        {
            // Salta la primera línea (encabezados del archivo CSV)
            scanner.nextLine();

            // Itera sobre las líneas restantes del archivo
            while (scanner.hasNextLine()) 
            {
                // Lee una línea completa del archivo
                String linea = scanner.nextLine();

                // Divide la línea en valores usando el delimitador definido (ej: coma o punto y coma)
                String[] valores = linea.split(Constants.CSV_DELIMITER);

                // Crea una nueva instancia de CursoEtapa
                CursoEtapa cursoEtapa = new CursoEtapa();

                // Extrae y convierte el valor del curso (columna 0)
                int curso = Integer.parseInt(valores[0]);

                // Extrae el valor de la etapa (columna 1)
                String etapa = valores[1];    

                // Crea un objeto compuesto IdCursoEtapa
                IdCursoEtapa idCursoEtapa = new IdCursoEtapa(curso, etapa);

                // Asocia el identificador al objeto CursoEtapa
                cursoEtapa.setIdCursoEtapa(idCursoEtapa);

                // Guarda y confirma el objeto en la base de datos
                this.iCursoEtapaRepository.saveAndFlush(cursoEtapa);
            }
        } 
        catch (Exception exception) 
        {
        	 // Captura cualquier excepción y lanza una excepción personalizada
        	MatriculasHorariosServerException matriculasHorariosServerException = new MatriculasHorariosServerException(1, "ERROR - Los datos de los cursos no han podido ser procesados", exception);
        	log.error(matriculasHorariosServerException.getBodyExceptionMessage().toString());
           
            
        }
        finally 
        {
             scanner.close();
        }
    }
}
