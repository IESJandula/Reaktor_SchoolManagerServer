package es.iesjandula.school_manager_server.parsers;

import java.util.Scanner;

import es.iesjandula.school_manager_server.models.CursoEtapa;
import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;

/**
 * Interfaz - IParseoDatosBrutos
 * -----------------------------------------------------------------------------------------------------------------
 * Define el contrato para el parseo de datos en bruto.
 * Esta interfaz especifica un método para procesar datos en bruto desde una fuente de entrada (por ejemplo, un archivo)
 * y extraer objetos relacionados con CursoEtapa, facilitando su integración en el sistema.
 * -----------------------------------------------------------------------------------------------------------------
 */
public interface IParseoDatosBrutos 
{
    /**
     * Parsea los datos proporcionados para extraer información significativa y actualizar un objeto de tipo {@link CursoEtapa}.
     *
     * @param scanner   						 - Una instancia de {@link Scanner} que contiene los datos en bruto a procesar.
     * @param cursoEtapa 						 - Una instancia de {@link CursoEtapa} que será actualizada con los datos extraídos.
     * @throws SchoolManagerServerException - Si ocurre algún error durante el proceso de parseo, 
     */
    public void parseoDatosBrutos(Scanner scanner, CursoEtapa cursoEtapa) throws SchoolManagerServerException;
}
