package es.iesjandula.reaktor.school_manager_server.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Esta clase es la que habilita que una dirección IP remota pueda hacer
 * llamadas al backend
 * 
 * @author Pablo Ruiz
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "es.iesjandula" })
public class CORSConfig implements WebMvcConfigurer
{
	/** URL permitida de CORS */
	@Value("${reaktor.urlCors}")
	private String[] urlCors;

	/**
	 * @param registry información del Cors Registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry)
	{
		registry.addMapping("/**").allowedOrigins(urlCors).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH" , "OPTIONS")
				.allowedHeaders("*");
	}
}
