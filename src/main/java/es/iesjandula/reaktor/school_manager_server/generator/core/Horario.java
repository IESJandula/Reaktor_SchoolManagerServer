package es.iesjandula.reaktor.school_manager_server.generator.core;

import java.util.Arrays;
import java.util.Objects;

import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.CopiaEstructuras;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class Horario implements Comparable<Horario>
{
    /** Parámetros para el horario */
	private HorarioParams horarioParams ;
	
	/** Matriz de asignaciones matutinas */
    private Asignacion[][] matrizAsignacionesMatutinas ;

    /** Matriz de asignaciones vespertinas */
    private Asignacion[][] matrizAsignacionesVespertinas ;
    
    /** Penalizaciones que se encuentran en esta solución */
    private int puntuacion ;

    /**
     * @param horarioParams parámetros para el horario
     */
    public Horario(HorarioParams horarioParams)
    {
    	this.horarioParams = horarioParams ;
    	
    	// Creamos una copia del estado actual del horario matutino
        if (horarioParams.getMatrizAsignacionesMatutinas() != null)
        {
            this.matrizAsignacionesMatutinas = CopiaEstructuras.copiarMatriz(horarioParams.getMatrizAsignacionesMatutinas()) ;
        }

        // Creamos una copia del estado actual del horario vespertino
        if (horarioParams.getMatrizAsignacionesVespertinas() != null)   
        {
            this.matrizAsignacionesVespertinas = CopiaEstructuras.copiarMatriz(horarioParams.getMatrizAsignacionesVespertinas()) ;
        }
    }
    
    /**
     * Método que calcula la puntuación de una solución
     * 
     * @throws SchoolManagerServerException - Excepción personalizada
     * @return la puntuación obtenida
     */
    public int calcularPuntuacion() throws SchoolManagerServerException
    {
        // Inicializamos la puntuación
        this.puntuacion = 0 ;

        // Obtenemos el número de cursos matutinos y vespertinos
        int numeroCursosMatutinos   = this.horarioParams.getNumeroCursosMatutinos() ;
        int numeroCursosVespertinos = this.horarioParams.getNumeroCursosVespertinos() ;
        int totalSesiones           = (numeroCursosMatutinos + numeroCursosVespertinos) * Constants.NUMERO_DIAS_SEMANA * Constants.NUMERO_TRAMOS_HORARIOS ;

    	// Calculamos la puntuación en función de la consecutividad de asignaturas o profesor
    	this.puntuacion = this.puntuacion + this.calcularPuntuacionConsecutividad(totalSesiones) ;

        log.info("Puntuacion obtenida: {}", this.puntuacion) ;
    	
    	return this.puntuacion ;
    }
    
    /**
     * Sumamos por cada consecutividad
     * 
     * @param totalSesiones número total de sesiones
     * @throws SchoolManagerServerException - Excepción personalizada
     * @return puntuación en función de la consecutividad de asignaturas o profesor
     */
    private int calcularPuntuacionConsecutividad(int totalSesiones) throws SchoolManagerServerException
    {
        int puntuacion = 0 ;

        if (this.matrizAsignacionesMatutinas != null)
        {
            // Calculamos la puntuación en función de la consecutividad en la matriz matutina
            puntuacion = puntuacion + this.calcularPuntuacionConsecutividadMatutina() ;
        }

        if (this.matrizAsignacionesVespertinas != null)
        {
            // Calculamos la puntuación en función de la consecutividad en la matriz vespertina
            puntuacion = puntuacion + this.calcularPuntuacionConsecutividadVespertina() ;
        }

        // Calculamos el porcentaje de sesiones consecutivas
        double porcentajeSesionesConsecutivas = (double) (puntuacion * 100) / totalSesiones ;

        // Guardamos la puntuación en la BBDD
        this.horarioParams.getGeneradorService().guardarGeneradorInstanciaSolucionInfoGeneral(this.horarioParams.getGeneradorInstancia(), Constants.SOL_INFO_SESIONES_CONSECUTIVAS, puntuacion, porcentajeSesionesConsecutivas) ;

        return puntuacion ;
    }
    
    /**
     * Sumamos por cada consecutividad matutina
     * 
     * @throws SchoolManagerServerException - Excepción personalizada
     * @return puntuación en función de la consecutividad matutina de asignaturas o profesor
     */
    private int calcularPuntuacionConsecutividadMatutina() throws SchoolManagerServerException
    {
        int puntuacion = 0 ;

    	for (int indiceCursoDia = 0 ; indiceCursoDia < this.matrizAsignacionesMatutinas.length ; indiceCursoDia++)
        {
            // Obtengo el número entre 0-5 que equivale al día
            int diaExacto = indiceCursoDia % Constants.NUMERO_DIAS_SEMANA ;
    		
            for (int indiceTramoHorario = 0 ; indiceTramoHorario < this.matrizAsignacionesMatutinas[indiceCursoDia].length ; indiceTramoHorario++)
            {
            	Asignacion asignacion = this.matrizAsignacionesMatutinas[indiceCursoDia][indiceTramoHorario] ;
            	
            	if (asignacion != null)
            	{
            		// Para todas las sesiones vemos si este profesor es el mismo que el anterior
                	for (Sesion sesion : asignacion.getListaSesiones())
                	{
		            	Profesor profesor = sesion.getProfesor() ;
		            	
		            	// Verificamos si hay otro tramo horario después de este
		            	if (indiceTramoHorario + 1 < this.matrizAsignacionesMatutinas[indiceCursoDia].length)
		            	{
                            // Buscamos en el tramo horario siguiente si el profesor tiene clase
                            int puntuacionSesionesConsecutivasProfesor = 
                                this.calcularPuntuacionSesionesConsecutivasProfesor(this.matrizAsignacionesMatutinas,
                                                                                    this.horarioParams.getNumeroCursosMatutinos(),
                                                                                    diaExacto,
                                                                                    indiceTramoHorario + 1,
                                                                                    profesor) ;

                            // Guardamos la puntuación en la BBDD
                            this.horarioParams.getGeneradorService().guardarGeneradorInstanciaSolucionInfoProfesor(this.horarioParams.getGeneradorInstancia(), profesor, Constants.SOL_INFO_SESIONES_CONSECUTIVAS, puntuacionSesionesConsecutivasProfesor) ;

                            // Sumamos la puntuación obtenida
		            		puntuacion = puntuacion + puntuacionSesionesConsecutivasProfesor ;
		            	}
                        else // Si entramos aquí, es porque es el último tramo horario de la matutina
                        {
                            // Buscamos en el tramo horario vespertino por si el profesor tiene clase la primera hora de la tarde
                            int puntuacionSesionesConsecutivasProfesorPrimeraHoraVespertina = 
                                this.calcularPuntuacionSesionesConsecutivasProfesorPrimeraHoraVespertina(diaExacto, profesor) ;

                            // Guardamos la puntuación en la BBDD
                            this.horarioParams.getGeneradorService().guardarGeneradorInstanciaSolucionInfoProfesor(this.horarioParams.getGeneradorInstancia(), profesor, Constants.SOL_INFO_SESIONES_CONSECUTIVAS, puntuacionSesionesConsecutivasProfesorPrimeraHoraVespertina) ;

                            // Sumamos la puntuación obtenida
                            puntuacion = puntuacion + puntuacionSesionesConsecutivasProfesorPrimeraHoraVespertina ;
                        }
                	}
            	}
            }
        }
        
        return puntuacion ;
    }

    /**
     * Sumamos por cada consecutividad vespertina
     * 
     * @throws SchoolManagerServerException - Excepción personalizada
     * @return puntuación en función de la consecutividad vespertina de asignaturas o profesor
     */
    private int calcularPuntuacionConsecutividadVespertina() throws SchoolManagerServerException
    {
        int puntuacion = 0 ;

    	for (int indiceCursoDia = 0 ; indiceCursoDia < this.matrizAsignacionesVespertinas.length ; indiceCursoDia++)
        {
            // Obtengo el número entre 0-5 que equivale al día
            int diaExacto = indiceCursoDia % Constants.NUMERO_DIAS_SEMANA ;
    		
            for (int indiceTramoHorario = 0 ; indiceTramoHorario < this.matrizAsignacionesVespertinas[indiceCursoDia].length ; indiceTramoHorario++)
            {
            	Asignacion asignacion = this.matrizAsignacionesVespertinas[indiceCursoDia][indiceTramoHorario] ;
            	
            	if (asignacion != null)
            	{
            		// Para todas las sesiones vemos si este profesor es el mismo que el anterior
                	for (Sesion sesion : asignacion.getListaSesiones())
                	{
		            	Profesor profesor = sesion.getProfesor() ;
		            	
		            	// Verificamos si hay otro tramo horario después de este
		            	if (indiceTramoHorario + 1 < this.matrizAsignacionesVespertinas[indiceCursoDia].length)
		            	{
                            // Buscamos en el tramo horario siguiente si el profesor tiene clase
                            int puntuacionSesionesConsecutivasProfesor = 
                                this.calcularPuntuacionSesionesConsecutivasProfesor(this.matrizAsignacionesVespertinas,
                                                                                             this.horarioParams.getNumeroCursosVespertinos(),
                                                                                             diaExacto,
                                                                                             indiceTramoHorario + 1,
                                                                                             profesor) ;

                            // Guardamos la puntuación en la BBDD
                            this.horarioParams.getGeneradorService().guardarGeneradorInstanciaSolucionInfoProfesor(this.horarioParams.getGeneradorInstancia(), profesor, Constants.SOL_INFO_SESIONES_CONSECUTIVAS, puntuacionSesionesConsecutivasProfesor) ;

                            // Sumamos la puntuación obtenida
                            puntuacion = puntuacion + puntuacionSesionesConsecutivasProfesor ;
		            	}
                	}
            	}
            }
        }
        
        return puntuacion ;
    }

    /**
     * 
     * @param matrizAsignaciones matriz de asignaciones
     * @param numeroCursos número de cursos
     * @param diaExacto día exacto
     * @param tramoHorario tramo horario para verificar si tiene clase el profesor
     * @param profesor profesor
     * @throws SchoolManagerServerException - Excepción personalizada
     * @return puntuación en función del número de sesiones consecutivas que tenga un profesor
     */
    private int calcularPuntuacionSesionesConsecutivasProfesor(Asignacion[][] matrizAsignaciones,
                                                               int numeroCursos,
    														   int diaExacto,
    														   int tramoHorario,
    														   Profesor profesor) throws SchoolManagerServerException
    {
    	int puntuacion = 0 ;
    	
    	int i = 0 ;
    	while (i < numeroCursos && puntuacion == 0)
    	{
        	// Vamos iterando por el mismo día pero en diferentes cursos, en el día de la semana concreto
        	int indiceCursoBusqueda = diaExacto + (i * Constants.NUMERO_DIAS_SEMANA) ;
    		
        	Asignacion asignacion = matrizAsignaciones[indiceCursoBusqueda][tramoHorario] ;
        	
        	// Si hay asignacion es porque al menos hay una sesion
        	if (asignacion != null)
        	{
        		// Para todas las sesiones vemos si este profesor es el mismo que el anterior
            	for (Sesion sesion : asignacion.getListaSesiones())
            	{
            		if (sesion.getProfesor().equals(profesor))
            		{
            			puntuacion = puntuacion + this.horarioParams.getFactorSesionesConsecutivasProfesor() ;
            		}
            	}
        	}
    		
    		i = i + 1 ;
    	}
    	
    	return puntuacion ;
	}

    /**
     * @param diaExacto día exacto
     * @param profesor profesor
     * @throws SchoolManagerServerException - Excepción personalizada
     * @return puntuación en función del número de sesiones consecutivas que tenga un profesor
     */
    private int calcularPuntuacionSesionesConsecutivasProfesorPrimeraHoraVespertina(int diaExacto, Profesor profesor) throws SchoolManagerServerException
    {
        int puntuacion = 0 ;

    	int i = 0 ;
    	while (i < this.horarioParams.getNumeroCursosVespertinos() && puntuacion == 0)
    	{
        	// Vamos iterando por el mismo día pero en diferentes cursos, en el día de la semana concreto
        	int indiceCursoBusqueda = diaExacto + (i * Constants.NUMERO_DIAS_SEMANA) ;
    		
            // Buscamos en las primeras horas de la tarde
        	Asignacion asignacion = this.matrizAsignacionesVespertinas[indiceCursoBusqueda][0] ;
        	
        	// Si hay asignacion es porque al menos hay una sesion
        	if (asignacion != null)
        	{
        		// Para todas las sesiones vemos si este profesor es el mismo que el anterior
            	for (Sesion sesion : asignacion.getListaSesiones())
            	{
            		if (sesion.getProfesor().equals(profesor))
            		{
            			puntuacion = puntuacion + this.horarioParams.getFactorSesionesConsecutivasProfesor() ;
            		}
            	}
        	}
    		
    		i = i + 1 ;
    	}
    	
    	return puntuacion ;        
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder() ;

        int anchoCeldas = 10 ; // Ancho de las celdas

        // Si solo hay horario matutino, se realiza solo sobre este
        if (this.matrizAsignacionesMatutinas != null && this.matrizAsignacionesVespertinas == null)
        {
            for (int j = 0; j < this.matrizAsignacionesMatutinas[0].length; j++)
            {
                this.toStringEnMatriz(this.matrizAsignacionesMatutinas, stringBuilder, j, anchoCeldas) ;

                stringBuilder.append("\n") ;
            }
        }
        else if (this.matrizAsignacionesMatutinas == null && this.matrizAsignacionesVespertinas != null)
        {
            for (int j = 0; j < this.matrizAsignacionesVespertinas[0].length; j++)
            {
                this.toStringEnMatriz(this.matrizAsignacionesVespertinas, stringBuilder, j, anchoCeldas) ;

                stringBuilder.append("\n") ;
            }
        }
        else if (this.matrizAsignacionesMatutinas != null && this.matrizAsignacionesVespertinas != null)
        {
            for (int j = 0; j < this.matrizAsignacionesMatutinas[0].length; j++)
            {
                // Construimos la primera línea del tramo horario para todos los cursos matutinos
                this.toStringEnMatriz(this.matrizAsignacionesMatutinas, stringBuilder, j, anchoCeldas) ;
    
                // Construimos la primera línea del tramo horario para todos los cursos vespertinos
                this.toStringEnMatriz(this.matrizAsignacionesVespertinas, stringBuilder, j, anchoCeldas) ;
    
                // Nos vamos al siguiente tramo horario
                stringBuilder.append("\n") ;            }
        }

        return stringBuilder.toString() ;
    }

    /**
     * @param matrizAsignaciones matriz de asignaciones
     * @param stringBuilder string builder
     * @param j tramo horario
     * @param anchoCeldas ancho de las celdas
     */
    private void toStringEnMatriz(Asignacion[][] matrizAsignaciones, StringBuilder stringBuilder, int j, int anchoCeldas)
    {
        for (int i = 0; i < matrizAsignaciones.length; i++)
        {
            boolean nuevoCurso = i % Constants.NUMERO_DIAS_SEMANA == 0 ;

            if (nuevoCurso)
            {
                stringBuilder.append("|") ;
            }

            if (matrizAsignaciones[i][j] != null)
            {
                // Alinea el texto con una longitud fija
                stringBuilder.append(String.format("%-" + anchoCeldas + "s", matrizAsignaciones[i][j].toString())) ;
            }
            else
            {
                // Inserta "null" alineado con una longitud fija
                stringBuilder.append(String.format("%-" + anchoCeldas + "s", "null")) ;
            }

            boolean esViernes = i % Constants.NUMERO_DIAS_SEMANA == Constants.NUMERO_DIAS_SEMANA - 1 ;

            if (!esViernes)
            {
                stringBuilder.append(", ") ;
            }
        }            
    }

    /**
     * Este método sirve para comparar dos soluciones basadas en su puntuación
     * 
     * @return el que tenga la puntuación menor
     */
    public int compareTo(Horario horario)
    {
        return Integer.compare(this.puntuacion, horario.puntuacion) ;
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true ;
        }
        else if (object == null || this.getClass() != object.getClass())
        {
            return false ;
        }
        
        Horario horario = (Horario) object ;
        
        return Arrays.deepEquals(this.matrizAsignacionesMatutinas, horario.getMatrizAsignacionesMatutinas()) &&
               Arrays.deepEquals(this.matrizAsignacionesVespertinas, horario.getMatrizAsignacionesVespertinas()) ;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(Arrays.deepHashCode(matrizAsignacionesMatutinas), Arrays.deepHashCode(matrizAsignacionesVespertinas), this.puntuacion) ;
    }
}

