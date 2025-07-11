package es.iesjandula.reaktor.school_manager_server.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.AllArgsConstructor;

/**
 * Clase personalizada de excepción que extiende {@link Exception}.
 * -----------------------------------------------------------------------------------------------------------------
 * Esta clase proporciona una estructura de excepción más rica con información adicional, como un código, un mensaje y un
 * posible stack trace asociado a una excepción original.
 * -----------------------------------------------------------------------------------------------------------------
 */
@AllArgsConstructor
@Getter
public class SchoolManagerServerException extends Exception
{
	/**
	 * SerialVersionUID utilizado para la serialización de la clase.
	 */
	private static final long serialVersionUID = 1L;
	
	// Código de la excepción.
	private int code;
	
	// Mensaje de la excepción.
	private String message;
	
	// Excepción original que puede ser asociada a esta excepción personalizada.
	private Exception exception;
	
	/**
	 * Constructor de la excepción con código y mensaje.
	 * -----------------------------------------------------------------------------------------------------------------
	 * Este constructor permite crear una excepción con un código de error y un mensaje específico.
	 * -----------------------------------------------------------------------------------------------------------------
	 * @param code - El código asociado a la excepción.
	 * @param message - El mensaje que se asociará con la excepción.
	 */
	public SchoolManagerServerException(int code, String message) 
	{
		this.code = code;
		this.message = message;
	}

	/**
	 * Método que devuelve un mapa con los detalles de la excepción.
	 * -----------------------------------------------------------------------------------------------------------------
	 * Este método crea un mapa con la información relevante de la excepción, como el código, el mensaje y el stack trace
	 * (si está disponible).
	 * -----------------------------------------------------------------------------------------------------------------
	 * @return Mapa con las claves "code", "message" y, si está presente, "exception".
	 */
	public Map<String,String> getBodyExceptionMessage()
	{
		// Creamos un mapa para almacenar los detalles de la excepción.
		Map<String,String> messageMap = new HashMap<String,String>() ;
		
		// Agregamos el código y el mensaje al mapa.
		messageMap.put("code", String.valueOf(this.code)) ;
		messageMap.put("message", this.message) ;
		
		// Si hay una excepción original, agregamos su stack trace al mapa.
		if (this.exception != null)
		{
			// Utilizamos la clase ExceptionUtils de Apache Commons Lang para obtener el stack trace.
			String stackTrace = ExceptionUtils.getStackTrace(this.exception) ;
			messageMap.put("exception", stackTrace) ;
		}
		
		// Devolvemos el mapa con los detalles de la excepción.
		return messageMap ;
	}	
}
