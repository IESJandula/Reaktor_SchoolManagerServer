package es.iesjandula.reaktor.school_manager_server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal que inicia la aplicación Spring Boot.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase contiene el método {@link main} que se encarga de iniciar la
 * aplicación.
 * Implementa {@link CommandLineRunner} para ejecutar lógica al iniciar la
 * aplicación.
 * -----------------------------------------------------------------------------------------------------------------
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "es.iesjandula" })
public class SchoolManagerServerApplication
{
	/**
	 * Método principal que se utiliza para arrancar la aplicación Spring Boot.
	 * -----------------------------------------------------------------------------------------------------------------
	 * Este método ejecuta la aplicación, utilizando {@link SpringApplication} para
	 * inicializar el contexto de la aplicación.
	 * -----------------------------------------------------------------------------------------------------------------
	 */
	public static void main(String[] args)
	{
		// Iniciar la aplicación Spring Boot.
		SpringApplication.run(SchoolManagerServerApplication.class, args);
	}
}
