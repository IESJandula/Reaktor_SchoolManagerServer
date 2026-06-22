package es.iesjandula.reaktor.school_manager_server.services.manager;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import es.iesjandula.reaktor.school_manager_server.dtos.AsignaturaHorasDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CargaHorasAsignaturasResultDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.CsvParserUtil;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ParseoCsvHorasAsignaturasService
{
    private static final String CABECERA_MATERIA = "Materia";
    private static final Pattern PATRON_MATERIA_HORAS = Pattern.compile("^(.*)\\s+\\((\\d+):(\\d{2})\\)$");

    @Autowired
    private IAsignaturaRepository iAsignaturaRepository;

    @Autowired
    private CursoEtapaService cursoEtapaService;

    @Autowired
    private CursoAcademicoResolver cursoAcademicoResolver;

    /**
     * Parsea un CSV de horas de asignaturas (columna Materia con formato "Nombre (H:MM)")
     * y persiste las horas en todas las filas de Asignatura del curso/etapa indicados.
     */
    @Transactional
    public CargaHorasAsignaturasResultDto cargarHorasDesdeCsv(MultipartFile archivoCsv, Integer curso, String etapa) throws SchoolManagerServerException
    {
        validarArchivoCsv(archivoCsv);

        this.cursoEtapaService.validarYObtenerCursoEtapa(curso, etapa);

        String cursoAcademico = this.cursoAcademicoResolver.resolver();

        List<AsignaturaHorasDto> asignaturasEnBd = this.iAsignaturaRepository.findNombreAndHorasByCursoEtapa(cursoAcademico, curso, etapa);
        if (asignaturasEnBd.isEmpty())
        {
            String mensajeError = "No se ha encontrado asignaturas con horas para '" + curso + " " + etapa + "'";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.ASIGNATURA_NO_ENCONTRADA, mensajeError);
        }

        String csvString = leerContenidoCsv(archivoCsv);
        Map<String, Integer> horasPorNombre = parsearHorasPorNombre(csvString);

        CargaHorasAsignaturasResultDto resultado = new CargaHorasAsignaturasResultDto();
        int actualizadas = 0;

        for (Map.Entry<String, Integer> entrada : horasPorNombre.entrySet())
        {
            String nombreAsignatura = entrada.getKey();
            Integer horas = entrada.getValue();

            List<Asignatura> asignaturas = this.iAsignaturaRepository.findNombreByCursoEtapaAndNombres(cursoAcademico, curso, etapa, nombreAsignatura);

            if (asignaturas.isEmpty())
            {
                resultado.getAsignaturasNoEncontradas().add(nombreAsignatura);
                continue;
            }

            for (Asignatura asignatura : asignaturas)
            {
                asignatura.setHoras(horas);
                this.iAsignaturaRepository.saveAndFlush(asignatura);
            }

            actualizadas++;
            resultado.getHorasAsignadas().add(new AsignaturaHorasDto(nombreAsignatura, horas));
        }

        resultado.setAsignaturasActualizadas(actualizadas);
        log.info("INFO - Horas cargadas desde CSV para {} {}: {} asignaturas actualizadas", curso, etapa, actualizadas);

        return resultado;
    }

    private void validarArchivoCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        if (archivoCsv == null || archivoCsv.isEmpty())
        {
            String mensajeError = "El archivo importado está vacío";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.ARCHIVO_VACIO, mensajeError);
        }

        String nombreArchivo = archivoCsv.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".csv"))
        {
            String mensajeError = "El archivo debe tener extensión .csv";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
        }
    }

    private String leerContenidoCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        try
        {
            Charset encoding = obtenerCodificacionArchivoCSV(archivoCsv);
            return new String(archivoCsv.getBytes(), encoding);
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo leer el fichero CSV de horas";
            log.error(mensajeError, exception);
            throw new SchoolManagerServerException(Constants.IO_EXCEPTION, mensajeError, exception);
        }
    }

    private Map<String, Integer> parsearHorasPorNombre(String csvString) throws SchoolManagerServerException
    {
        String[] lineas = csvString.split("\\R");
        if (lineas.length < 2)
        {
            String mensajeError = "El CSV de horas no contiene filas de datos";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
        }

        String[] cabecera = CsvParserUtil.parseLinea(lineas[0]);
        int indiceMateria = obtenerIndiceColumna(cabecera, CABECERA_MATERIA);

        Map<String, Integer> horasPorNombre = new LinkedHashMap<>();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            String[] campos = CsvParserUtil.parseLinea(linea);
            if (campos.length <= indiceMateria)
            {
                continue;
            }

            String materia = limpiarCampo(campos[indiceMateria]);
            if (materia.isEmpty())
            {
                continue;
            }

            MateriaHoras materiaHoras = extraerNombreYHoras(materia);
            horasPorNombre.put(normalizarNombreAsignatura(materiaHoras.nombre()), materiaHoras.horas());
        }

        if (horasPorNombre.isEmpty())
        {
            String mensajeError = "No se encontraron asignaturas válidas en la columna Materia del CSV";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
        }

        return horasPorNombre;
    }

    private int obtenerIndiceColumna(String[] cabecera, String nombreColumna) throws SchoolManagerServerException
    {
        for (int i = 0; i < cabecera.length; i++)
        {
            if (nombreColumna.equalsIgnoreCase(limpiarCampo(cabecera[i])))
            {
                return i;
            }
        }

        String mensajeError = "El CSV debe incluir la columna '" + nombreColumna + "'";
        log.error(mensajeError);
        throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
    }

    private MateriaHoras extraerNombreYHoras(String materia) throws SchoolManagerServerException
    {
        Matcher matcher = PATRON_MATERIA_HORAS.matcher(materia.trim());

        if (!matcher.matches())
        {
            String mensajeError = "Formato de materia no válido: '" + materia + "'. Se esperaba 'Nombre (H:MM)'";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
        }

        String nombre = matcher.group(1).trim();
        int horas = Integer.parseInt(matcher.group(2));

        if (horas < 0)
        {
            String mensajeError = "Las horas no pueden ser negativas para la materia '" + materia + "'";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
        }

        return new MateriaHoras(nombre, horas);
    }

    private String normalizarNombreAsignatura(String nombre)
    {
        return nombre.trim().toUpperCase();
    }

    private String limpiarCampo(String campo)
    {
        return CsvParserUtil.limpiarCampo(campo);
    }

    private Charset obtenerCodificacionArchivoCSV(MultipartFile archivoCsv) throws java.io.IOException
    {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(archivoCsv.getBytes());
        CharsetMatch match = detector.detect();
        return Charset.forName(match.getName());
    }

    private record MateriaHoras(String nombre, int horas)
    {
    }
}
