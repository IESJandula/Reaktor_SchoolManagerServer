package es.iesjandula.matriculas_horarios_server;

import java.io.File;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.matriculas_horarios_server.parsers.IParseoCursoEtapa;
import es.iesjandula.matriculas_horarios_server.parsers.IParseoDatosBrutos;
import es.iesjandula.matriculas_horarios_server.utils.Constants;

/**
 * Clase principal que inicia la aplicación Spring Boot.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase contiene el método {@link main} que se encarga de iniciar la aplicación. 
 * Implementa {@link CommandLineRunner} para ejecutar lógica al iniciar la aplicación.
 * -----------------------------------------------------------------------------------------------------------------
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages =
		{ "es.iesjandula" })
public class MatriculasHorariosServerApplication implements CommandLineRunner
{
	@Autowired
	IParseoCursoEtapa iParseoCursoEtapa;

	@Autowired
	IParseoDatosBrutos iParseoDatosBrutos;

	/**
	 * Método principal que se utiliza para arrancar la aplicación Spring Boot.
	 * -----------------------------------------------------------------------------------------------------------------
	 * Este método ejecuta la aplicación, utilizando {@link SpringApplication} para inicializar el contexto de la aplicación.
	 * -----------------------------------------------------------------------------------------------------------------
	 */
	public static void main(String[] args) 
	{
		// Iniciar la aplicación Spring Boot.
		SpringApplication.run(MatriculasHorariosServerApplication.class, args);
	}

	/**
	 * Método {@link run} que se ejecuta al arrancar la aplicación.
	 * -----------------------------------------------------------------------------------------------------------------
	 * Este método se ejecuta al iniciar la aplicación y se encarga de realizar el parseo de datos.
	 * En este caso, lee el archivo CSV "cursos_etapas.csv" y lo procesa utilizando el servicio de parseo correspondiente.
	 * -----------------------------------------------------------------------------------------------------------------
	 * @param args 		 - Argumentos pasados a la aplicación (no utilizados en este caso).
	 * @throws Exception - Si ocurre algún error durante la ejecución.
	 */
	@Transactional(readOnly = false)
	public void run(String... args) throws Exception 
	{
		// Parsear CSV CursoEtapa
		// Crear un archivo File a partir de la ruta de acceso especificada en las constantes.
		File file = new File(Constants.CSV_ROUTES + "cursos_etapas.csv");

		// Crear un objeto Scanner para leer el contenido del archivo.
		Scanner scanner = new Scanner(file);

		// Llamar al servicio para parsear el archivo de cursos y etapas.
		iParseoCursoEtapa.parseoCursoEtapa(scanner);
	}
}
