package es.iesjandula.reaktor.school_manager_server.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CsvParserUtilTest
{
    @Test
    void parseLinea_respetaComaDentroDeComillasEnNombreAsignatura()
    {
        String cabecera = "Alumno/a,Matemáticas,\"Educación Plástica, Visual y Audiovisual\",Lengua";

        String[] campos = CsvParserUtil.parseLinea(cabecera);

        assertEquals(4, campos.length);
        assertEquals("Alumno/a", campos[0]);
        assertEquals("Matemáticas", campos[1]);
        assertEquals("Educación Plástica, Visual y Audiovisual", CsvParserUtil.limpiarCampo(campos[2]));
        assertEquals("Lengua", campos[3]);
    }

    @Test
    void parseLinea_alineaFilaAlumnoConCabeceraConAsignaturaEntrecomillada()
    {
        String cabecera = "Alumno/a,Matemáticas,\"Educación Plástica, Visual y Audiovisual\"";
        String filaAlumno = "García,Juan,MATR,MATR";

        String[] camposCabecera = CsvParserUtil.parseLinea(cabecera);
        String[] camposAlumno = CsvParserUtil.parseLinea(filaAlumno);

        assertEquals(3, camposCabecera.length);
        assertEquals(4, camposAlumno.length);
        assertEquals("Educación Plástica, Visual y Audiovisual", CsvParserUtil.limpiarCampo(camposCabecera[2]));
        assertEquals("MATR", camposAlumno[3].trim());
    }

    @Test
    void parseLinea_respetaComaDentroDeComillasEnColumnaMateria()
    {
        String fila = "1,\"Educación Plástica, Visual y Audiovisual (3:00)\",3";

        String[] campos = CsvParserUtil.parseLinea(fila);

        assertEquals(3, campos.length);
        assertEquals("Educación Plástica, Visual y Audiovisual (3:00)", CsvParserUtil.limpiarCampo(campos[1]));
    }

    /**
     * Fila real exportada de Séneca: el campo "Alumno/a" viene entrecomillado en formato
     * "Apellidos, Nombre" y a continuación la "Unidad" (grupo) y las asignaturas (MATR/NO_MATR).
     * Verifica que el parser RFC4180 mantiene el alumno como un único campo, que la unidad NO se
     * confunde con el nombre, y que el alumno se separa correctamente en apellidos y nombre.
     */
    @Test
    void parseLinea_filaRealSeneca_separaApellidosNombreYUnidad()
    {
        String cabecera = "\"Alumno/a\",\"Unidad\",\"Educación Física\",\"Lengua Castellana y Literatura\",\"Inglés\"";
        String filaAlumno = "\"Almazán Solás, Jimena\",\"1ºESO A\",\"MATR\",\"MATR\",\"MATR\"";

        String[] camposCabecera = CsvParserUtil.parseLinea(cabecera);
        String[] camposAlumno = CsvParserUtil.parseLinea(filaAlumno);

        // La cabecera y la fila del alumno tienen el mismo número de columnas (alineadas)
        assertEquals(5, camposCabecera.length);
        assertEquals(5, camposAlumno.length);

        // El primer campo es el alumno completo (un único campo, la coma queda dentro de las comillas)
        String alumnoCompleto = CsvParserUtil.limpiarCampo(camposAlumno[0]);
        assertEquals("Almazán Solás, Jimena", alumnoCompleto);

        // El segundo campo es la Unidad (grupo), NO el nombre del alumno
        assertEquals("1ºESO A", CsvParserUtil.limpiarCampo(camposAlumno[1]));

        // Las asignaturas comienzan en el mismo índice que en la cabecera (índice 2 al existir "Unidad")
        assertEquals("Educación Física", CsvParserUtil.limpiarCampo(camposCabecera[2]));
        assertEquals("MATR", CsvParserUtil.limpiarCampo(camposAlumno[2]));

        // El alumno se separa por la primera coma: antes = apellidos, después = nombre
        int indicePrimeraComa = alumnoCompleto.indexOf(',');
        String apellidos = alumnoCompleto.substring(0, indicePrimeraComa).trim();
        String nombre = alumnoCompleto.substring(indicePrimeraComa + 1).trim();

        assertEquals("Almazán Solás", apellidos);
        assertEquals("Jimena", nombre);
    }
}
