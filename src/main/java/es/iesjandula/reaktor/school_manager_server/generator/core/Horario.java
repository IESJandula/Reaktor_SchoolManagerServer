package es.iesjandula.reaktor.school_manager_server.generator.core;

import java.util.Arrays;
import java.util.Objects;

import es.iesjandula.reaktor.school_manager_server.generator.models.Asignacion;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.CopiaEstructuras;
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

