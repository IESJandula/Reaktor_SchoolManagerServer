package es.iesjandula.reaktor.school_manager_server.parseos;

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

/**
 * Clase de implementación - ParseoDatosBrutosImpl
 * -----------------------------------------------------------------------------------------------------------------
 * Implementa la interfaz {@link IParseoDatosBrutos} para procesar datos en bruto relacionados con 
 * alumnos y sus matrículas. Utiliza el repositorio {@link IDatosBrutoAlumnoMatriculaRepository} para 
 * almacenar la información procesada en la base de datos.
 * -----------------------------------------------------------------------------------------------------------------
 */
@Slf4j
@Service
public class ParseoDatosBrutosImpl implements IParseoDatosBrutos
{
    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository iDatosBrutoAlumnoMatriculaRepository;

    /**
     * Implementación del método para parsear y persistir datos en bruto de alumnos y sus matrículas.
     * 
     * @param scanner    						 - Una instancia de {@link Scanner} con los datos de entrada en formato CSV.
     * @param cursoEtapa 						 - Un objeto {@link CursoEtapa} que representa el curso y etapa asociados a los datos.
     * @throws SchoolManagerServerException - Si ocurre algún error durante el parseo, como inconsistencias en los datos.
     */
    @Override
    public void parseoDatosBrutos(Scanner scanner, CursoEtapa cursoEtapa) throws SchoolManagerServerException 
    {
        try 
        {
            // Linea de campos -> alumno, lengua, mates, ingles
            String lineaCampos = scanner.nextLine();
        
            // Array con valores de los campos
            String[] valoresCampos = lineaCampos.split(Constants.CSV_DELIMITER, -1);
        
            while(scanner.hasNext())
            {
                // Linea de registro -> Martinez Guerbos, Pablo, MATR, MATR, , ,
                String lineaRegistro = scanner.nextLine();
                
                // Array con valores del registro
                String[] valoresRegistro = lineaRegistro.split(Constants.CSV_DELIMITER, -1);
                
                // Apellidos del Alumno -> Martinez Guerbos
                String apellidosAlumno = valoresRegistro[0].trim().replace("\"", "") ;
                
                // Nombre del Alumno -> Pablo
                String nombreAlumno = valoresRegistro[1].trim().replace("\"", "") ;
                
                List<DatosBrutoAlumnoMatricula> listaDatosBrutoAlumnoMatriculas = new ArrayList<DatosBrutoAlumnoMatricula>();
                
                for (int i = 1; i < valoresCampos.length; i++)
                {
                    // Si tiene algun valor el campo de registro de la asignatura 
                    if(i + 1 < valoresRegistro.length)
                    {
                        // Crear registro
                        DatosBrutoAlumnoMatricula datosBrutoAlumnoMatricula = new DatosBrutoAlumnoMatricula();
                        
                        // Añadir apellidos del alumno al registro -> Martinez Guerbos
                        datosBrutoAlumnoMatricula.setApellidos(apellidosAlumno);
                        
                        // Añadir nombre del alumno al registro -> Pablo
                        datosBrutoAlumnoMatricula.setNombre(nombreAlumno);
                    	
                        // Obtener a que campo de asignatura corresponde
                        String asignatura = valoresCampos[i].trim().replaceAll("[.\"]", "");
                        
                        // Añadir asignatura matriculada al registro -> LENGUA
                        datosBrutoAlumnoMatricula.setAsignatura(asignatura.toUpperCase());
                        
                        // Añadir curso al registro -> 2DAM
                        datosBrutoAlumnoMatricula.setCursoEtapa(cursoEtapa);
                        
                        if(valoresRegistro[i + 1].trim().equals("")) 
                        {
                        	datosBrutoAlumnoMatricula.setEstadoMatricula(Constants.ESTADO_NO_MATRICULADO);;
                        }
                        else 
                        {
                        	datosBrutoAlumnoMatricula.setEstadoMatricula(valoresRegistro[i + 1]);
                        }
                        listaDatosBrutoAlumnoMatriculas.add(datosBrutoAlumnoMatricula);
                    }
                }
                // Guardar o actualizar en la tabla -> DatosBrutoAlumnoMatricula
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
                scanner.close();
        }
    }
}
