package es.iesjandula.reaktor.school_manager_server.services.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ParseoCsvService
{
    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    /**
     * Implementación del método para parsear y persistir datos en bruto de alumnos y sus matrículas.
     * 
     * @param csvString    						 - Una instancia de {@link Scanner} con los datos de entrada en formato CSV.
     * @param cursoEtapa 						 - Un objeto {@link CursoEtapa} que representa el curso y etapa asociados a los datos.
     * @throws SchoolManagerServerException - Si ocurre algún error durante el parseo, como inconsistencias en los datos.
     */
    public void parseoDatosBrutos(String csvString, CursoEtapa cursoEtapa) throws SchoolManagerServerException
    {
        // Declaramos el Scanner para realizar la lectura del fichero
        Scanner scanner = null;

        try 
        {
            // Inicializamos el Scanner
            scanner = new Scanner(csvString);

            // Obtenemos la cabecera del CSV
            String cabecera = scanner.nextLine();

            // Obtenemos el índice de comienzo de las asignaturas
            int indiceAsignaturas = this.obtenerIndiceAsignaturas(cabecera);

            // Obtenemos la cabecera con solo las asignaturas
            String[] cabeceraSoloAsignaturas = this.obtenerCabeceraSoloAsignaturas(cabecera, indiceAsignaturas);
        
            // Mientras haya alumnos en el CSV
            while(scanner.hasNext())
            {
                // Obtenemos la línea completa del alumno
                String lineaAlumnoCompleta = scanner.nextLine();
                
                // Obtenemos el array con los valores de la línea completa del alumno
                String[] splitLineaAlumnoCompleta = lineaAlumnoCompleta.split(Constants.CSV_DELIMITER, -1);
                
                // Obtenemos los apellidos del alumno
                String apellidosAlumno = splitLineaAlumnoCompleta[0].trim().replace("\"", "") ;
                
                // Obtenemos el nombre del alumno
                String nombreAlumno    = splitLineaAlumnoCompleta[1].trim().replace("\"", "") ;
                
                // Inicializamos la lista de datos brutos de este alumno y sus matrículas
                List<DatosBrutoAlumnoMatricula> listaDatosBrutoAlumnoMatriculas = new ArrayList<DatosBrutoAlumnoMatricula>();

                // Array con valores de las asignaturas del alumno
                String[] alumnoSoloAsignaturas = this.obtenerAlumnoSoloAsignaturas(splitLineaAlumnoCompleta, indiceAsignaturas);

                // Recorremos el array de valores de las asignaturas
                for (int i = 0; i < alumnoSoloAsignaturas.length; i++)
                {
                    // Obtenemos a que campo de asignatura corresponde
                    String asignatura = cabeceraSoloAsignaturas[i].trim().replaceAll("[.\"]", "");

                    // Obtenemos el estado de la matrícula de esta asignatura
                    String estadoMatricula = alumnoSoloAsignaturas[i].trim();

                    // Parsea los datos brutos del alumno y sus matrículas
                    DatosBrutoAlumnoMatricula datosBrutoAlumnoMatricula = this.parseoDatosBrutosAlumnoAsignaturas(cursoEtapa, apellidosAlumno, nombreAlumno, asignatura, estadoMatricula);

                    // Añadimos el registro a la lista de datos brutos de este alumno y sus matrículas
                    listaDatosBrutoAlumnoMatriculas.add(datosBrutoAlumnoMatricula);
                }

                // Guardamos o actualizamos en la tabla -> DatosBrutoAlumnoMatricula
                this.iDatosBrutoAlumnoMatriculaRepository.saveAllAndFlush(listaDatosBrutoAlumnoMatriculas);
            }
        } 
        catch (Exception exception) 
        {
        	 // Captura cualquier excepción y lanza una excepción personalizada
        	SchoolManagerServerException matriculasHorariosServerException = new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, "ERROR - Los datos de los cursos no han podido ser procesados", exception);
        	log.error(matriculasHorariosServerException.getBodyExceptionMessage().toString());
        }
        finally 
        {
            // Si el Scanner no es null ...
            if (scanner != null)
            {
                // ... lo cerramos
                scanner.close();
            }
        }
    }

    /**
     * Obtiene el índice de comienzo de las asignaturas en el array de valores de los campos.
     * 
     * @param cabecera Una cadena de texto que representa la cabecera del CSV.
     * @return int El índice de comienzo de las asignaturas.
     */
    private int obtenerIndiceAsignaturas(String cabecera)
    {
        // Por defecto, el índice de comienzo de las asignaturas es 1
        int indiceAsignaturas = 1 ;

        // Si uno de los campos presentes es la "Unidad" (indica el curso, etapa y grupo),
        // esto significa que el CSV fue importado después de que se crearan los grupos. 
        // Si el campo "Unidad" no aparece (que es lo habitual), es porque el CSV se ha importado 
        // justo después de la matriculación, por lo que no se sabe aún los grupos, 
        // y el usuario tiene que crearlos.
        if(cabecera.contains("Unidad"))
        {
            indiceAsignaturas = 2 ;
        }

        // Devolvemos el índice de comienzo de las asignaturas
        return indiceAsignaturas ;
    }

    /**
     * Obtiene la cabecera con solo las asignaturas.
     * 
     * @param cabecera Una cadena de texto que representa la cabecera del CSV.
     * @param indiceAsignaturas Índice de comienzo de las asignaturas.
     * @return String[] La cabecera con solo las asignaturas.
     */
    private String[] obtenerCabeceraSoloAsignaturas(String cabecera, int indiceAsignaturas)
    {
        // Obtenemos el array con los valores de la cabecera
        String[] cabeceraArray = cabecera.split(Constants.CSV_DELIMITER);

        // Obtenemos el número de asignaturas
        int numeroAsignaturas = cabeceraArray.length - indiceAsignaturas;

        // Creamos un nuevo array con las asignaturas
        String[] cabeceraSoloAsignaturas = new String[numeroAsignaturas];

        // Copiamos en el nuevo array las asignaturas
        System.arraycopy(cabeceraArray, indiceAsignaturas, cabeceraSoloAsignaturas, 0, numeroAsignaturas);

        // Devolvemos la cabecera con solo las asignaturas
        return cabeceraSoloAsignaturas ;
    }

    /**
     * Obtiene el array de valores de las asignaturas del alumno.
     * 
     * @param splitLineaAlumnoCompleta Array con los valores del registro.
     * @param indiceAsignaturas Índice de comienzo de las asignaturas.
     * @return String[] Array de valores de las asignaturas del alumno.
     */
    private String[] obtenerAlumnoSoloAsignaturas(String[] splitLineaAlumnoCompleta, int indiceAsignaturas)
    {
        // Obtenemos el número de asignaturas
        int numeroAsignaturas = splitLineaAlumnoCompleta.length - indiceAsignaturas;

        // Creamos un nuevo array con las asignaturas
        String[] alumnoSoloAsignaturas = new String[numeroAsignaturas];

        // Copiamos en el nuevo array las asignaturas
        System.arraycopy(splitLineaAlumnoCompleta, indiceAsignaturas, alumnoSoloAsignaturas, 0, numeroAsignaturas);

        // Devolvemos el array de valores de las asignaturas del alumno
        return alumnoSoloAsignaturas ;
    }

    /**
     * Parsea los datos brutos de los alumnos y sus matrículas.
     * 
     * @param cursoEtapa Curso y etapa asociados a los datos.
     * @param apellidosAlumno Apellidos del alumno.
     * @param nombreAlumno Nombre del alumno.
     * @param asignatura Asignatura matriculada.
     * @param estadoMatricula Estado de la matrícula.
     * @return DatosBrutoAlumnoMatricula Registro de datos brutos de alumno y su matrícula.
     */
    private DatosBrutoAlumnoMatricula parseoDatosBrutosAlumnoAsignaturas(CursoEtapa cursoEtapa, String apellidosAlumno, String nombreAlumno, String asignatura, String estadoMatricula)
    {
        // Creamos un nuevo registro de datos brutos de alumno y su matrícula
        DatosBrutoAlumnoMatricula datosBrutoAlumnoMatricula = new DatosBrutoAlumnoMatricula();
        
        // Añadimos el curso y etapa al registro
        datosBrutoAlumnoMatricula.setCursoEtapa(cursoEtapa);
        
        // Añadimos el nombre del alumno al registro
        datosBrutoAlumnoMatricula.setNombre(nombreAlumno);

        // Añadimos los apellidos del alumno al registro
        datosBrutoAlumnoMatricula.setApellidos(apellidosAlumno);
        
        // Añadimos la asignatura matriculada al registro
        datosBrutoAlumnoMatricula.setAsignatura(asignatura.toUpperCase());
        
        // Por defecto, el estado de la matrícula es no matriculado
        datosBrutoAlumnoMatricula.setEstadoMatricula(Constants.ESTADO_NO_MATRICULADO);
        
        // Si el valor de la asignatura es vacío
        // se establece el estado de la matrícula con el valor de la asignatura
        if (!estadoMatricula.isEmpty()) 
        {
            datosBrutoAlumnoMatricula.setEstadoMatricula(estadoMatricula);
        }

        // Devolvemos el registro de datos brutos de alumno y su matrícula
        return datosBrutoAlumnoMatricula ;
    }
}
