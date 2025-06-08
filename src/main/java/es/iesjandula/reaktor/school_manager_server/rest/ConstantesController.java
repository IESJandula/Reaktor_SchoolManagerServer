package es.iesjandula.reaktor.school_manager_server.rest;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.school_manager_server.dtos.DtoConstantes;
import es.iesjandula.reaktor.school_manager_server.models.Constantes;
import es.iesjandula.reaktor.school_manager_server.repositories.IConstantesRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping(value = "/schoolManager/constants", produces = { "application/json" })
@RestController
@Log4j2
public class ConstantesController
{
	@Autowired
	private IConstantesRepository iConstantesRepository;

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> actualizarConstantes()
	{
		try
		{
			List<DtoConstantes> dtoConstantesList = this.iConstantesRepository.encontrarTodoComoDto();

			return ResponseEntity.ok(dtoConstantesList);
		}
		catch (Exception exception)
		{
			String mensajeError = "Excepción genérica al obtener las costantes";
			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.CONSTANTE_NO_ENCONTRADA,
					mensajeError, exception);

			log.error(mensajeError, schoolManagerServerException);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> actualizarConstantes(@RequestBody(required = true) List<DtoConstantes> dtoConstantesList)
	{
		try
		{
			for (DtoConstantes dtoConstantes : dtoConstantesList)
			{
				Constantes constantes = new Constantes(dtoConstantes.getClave(), dtoConstantes.getValor());

				this.iConstantesRepository.saveAndFlush(constantes);
			}

			return ResponseEntity.ok().build();
		}
		catch (Exception exception)
		{
			String mensajeError = "Excepción genérica al obtener las costantes";
			SchoolManagerServerException schoolManagerServerException = new SchoolManagerServerException(Constants.CONSTANTE_NO_ENCONTRADA,
					mensajeError, exception);

			log.error(mensajeError);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(schoolManagerServerException.getBodyExceptionMessage());
		}
	}
}
