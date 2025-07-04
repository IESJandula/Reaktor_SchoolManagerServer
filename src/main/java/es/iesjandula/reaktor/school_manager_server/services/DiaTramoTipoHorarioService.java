package es.iesjandula.reaktor.school_manager_server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.dtos.DiaDto;
import es.iesjandula.reaktor.school_manager_server.dtos.TramoDto;
import es.iesjandula.reaktor.school_manager_server.models.DiaTramoTipoHorario;
import es.iesjandula.reaktor.school_manager_server.repositories.IDiaTramoTipoHorarioRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiaTramoTipoHorarioService
{
    @Autowired
    private IDiaTramoTipoHorarioRepository diaTramoTipoHorarioRepository ;

    /**
     * Método que obtiene un día y tramo de tipo horario
     * @param dia - Día
     * @param tramo - Tramo
     * @param horarioMatutino - Horario matutino
     * @return - Día y tramo de tipo horario
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public DiaTramoTipoHorario obtenerDiaTramoTipoHorario(int dia, int tramo, Boolean horarioMatutino) throws SchoolManagerServerException
    {
        // Buscamos DiasTramosHorarios por IdDiasTramosTipoHorario
        Optional<DiaTramoTipoHorario> diasTramosTipoHorarioOptional = this.diaTramoTipoHorarioRepository.findByDiaAndTramoAndHorarioMatutino(dia, tramo, horarioMatutino) ;

        // Si no existe, lanzamos una excepción
        if (!diasTramosTipoHorarioOptional.isPresent())
        {
            String mensajeError = "El día " + dia + " y el tramo " + tramo + " no existe" ;

            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.DIA_TRAMO_NO_ENCONTRADO, mensajeError) ;
        }   

        return diasTramosTipoHorarioOptional.get() ;
    }

    /**
     * Método que obtiene los días de la semana
     * @return - Lista de días de la semana
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public List<String> obtenerDiasSemana() throws SchoolManagerServerException
    {
        // Devolvemos una lista de los días de la semana (distinct) de la BBDD
        Optional<List<DiaDto>> diasTramosTipoHorarioListOptional = this.diaTramoTipoHorarioRepository.obtenerDiasSemana() ;

        // Si no existe, lanzamos una excepción
        if (!diasTramosTipoHorarioListOptional.isPresent())
        {
            String mensajeError = "No se han encontrado días de la semana" ;
            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.DIAS_SEMANA_NO_ENCONTRADOS, mensajeError) ;
        }

        // Extrae solo los valores de texto de los DTOs y mantiene el orden
        return diasTramosTipoHorarioListOptional.get().stream()
                .sorted((d1, d2) -> Integer.compare(d1.getDia(), d2.getDia()))
                .map(DiaDto::getDiaDesc)
                .toList();
    }

    /**
     * Método que obtiene los tramos horarios
     * @return - Lista de tramos horarios
     * @throws SchoolManagerServerException - Excepción personalizada
     */
    public List<String> obtenerTramosHorarios() throws SchoolManagerServerException
    {
        // Devolvemos una lista de los tramos horarios (distinct) de la BBDD
        Optional<List<TramoDto>> tramosHorariosListOptional = this.diaTramoTipoHorarioRepository.obtenerTramosHorarios() ;

        // Si no existe, lanzamos una excepción
        if (!tramosHorariosListOptional.isPresent())
        {
            String mensajeError = "No se han encontrado tramos horarios" ;
            log.error(mensajeError) ;
            throw new SchoolManagerServerException(Constants.TRAMOS_HORARIOS_NO_ENCONTRADOS, mensajeError) ;
        }

        // Extrae solo los valores de texto de los DTOs y ordena por hora
        return tramosHorariosListOptional.get().stream()
                .sorted((t1, t2) -> compararHoras(t1.getTramoDesc(), t2.getTramoDesc()))
                .map(TramoDto::getTramoDesc)
                .toList();
    }

    /**
     * Método auxiliar para comparar horas en formato "HH:MM/HH:MM"
     * @param hora1 - Primera hora
     * @param hora2 - Segunda hora
     * @return - Comparación de las horas
     */
    private int compararHoras(String hora1, String hora2)
    {
        // Si alguna es "Sin Seleccionar", la ponemos al final
        if ("Sin Seleccionar".equals(hora1)) return 1;
        if ("Sin Seleccionar".equals(hora2)) return -1;
        
        try
        {
            // Extraemos la primera hora de cada tramo (antes del "/")
            String inicioHora1 = hora1.split("/")[0];
            String inicioHora2 = hora2.split("/")[0];
            
            // Convertimos a minutos para comparar
            int minutos1 = convertirHoraAMinutos(inicioHora1);
            int minutos2 = convertirHoraAMinutos(inicioHora2);
            
            return Integer.compare(minutos1, minutos2);
        }
        catch (Exception e)
        {
            // Si hay algún error en el parsing, ordenamos alfabéticamente
            return hora1.compareTo(hora2);
        }
    }

    /**
     * Método auxiliar para convertir hora en formato "HH:MM" a minutos
     * @param hora - Hora en formato "HH:MM"
     * @return - Minutos totales
     */
    private int convertirHoraAMinutos(String hora)
    {
        String[] partes = hora.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        return horas * 60 + minutos;
    }   
}
