package es.iesjandula.reaktor.school_manager_server.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import es.iesjandula.reaktor.school_manager_server.dtos.ValidadorDatosDto;
import es.iesjandula.reaktor.school_manager_server.services.ValidadorDatosService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/schoolManager/validadorDatos")
public class Paso8ValidadorDatosController
{
    @Autowired
    private ValidadorDatosService validadorDatosService ; 
    
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/errores")
    public ResponseEntity<?> devolverMensajesError()
    {
        try
        {
            // Realizamos una serie de validaciones de datos
            ValidadorDatosDto validadorDatosDto = this.validadorDatosService.validacionDatos() ;

            return ResponseEntity.ok().body(validadorDatosDto);
        }
        catch (Exception exception) 
        {
            // Manejo de excepciones generales
            String mensajeError = "ERROR - No se pudo actualizar el turno horario";

            log.error(mensajeError, exception) ;

            // Devolver la excepción personalizada con código genérico, el mensaje de error y la excepción general
            SchoolManagerServerException schoolManagerServerException =  new SchoolManagerServerException(Constants.ERROR_GENERICO, mensajeError, exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
        }
    }
}
