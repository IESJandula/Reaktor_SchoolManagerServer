package es.iesjandula.school_manager_server.interfaces;


import java.util.Scanner;

import es.iesjandula.school_manager_server.utils.SchoolManagerServerException;


public interface IParseo {
	
	void parseaFichero(Scanner scanner) throws SchoolManagerServerException ;

}
