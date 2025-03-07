package es.iesjandula.school_manager_server.interfaces;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;

@Configuration
public interface IGestorParseo {
	
	@Bean
	void parseaFichero(String nombreFichero) throws SchoolManagerServerException;

}
