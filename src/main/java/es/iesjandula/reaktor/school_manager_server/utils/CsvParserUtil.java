package es.iesjandula.reaktor.school_manager_server.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para parsear líneas CSV respetando campos entre comillas (RFC 4180):
 * las comas dentro de un campo entrecomillado no actúan como separador de columnas.
 */
public final class CsvParserUtil
{
    /** Codificación de respaldo (Excel en español) cuando el contenido no es UTF-8 válido. */
    private static final Charset CHARSET_RESPALDO = Charset.forName("windows-1252");

    /**
     * Constructor privado para evitar instanciación.
     */
    private CsvParserUtil()
    {
        // Vacío
    }

    /**
     * Decodifica el contenido de un CSV interpretando correctamente UTF-8 (que es lo habitual en estos ficheros) y
     * gestionando el BOM. Sustituye a la antigua detección automática de codificación (ICU CharsetDetector), que con
     * ficheros cortos confundía UTF-8 con ISO-8859-1/Windows-1252 y producía caracteres raros (mojibake), p. ej. la
     * "ó" de "Salón".
     * <p>
     * Estrategia: si hay BOM (UTF-8 o UTF-16) se respeta y se elimina. Si no hay BOM, se intenta decodificar como
     * UTF-8 estricto: si el contenido es UTF-8 válido (caso correcto en este módulo), se usa UTF-8; solo si NO es
     * UTF-8 válido se recurre a Windows-1252 como respaldo para no romper CSV antiguos exportados por Excel.
     *
     * @param bytes el contenido en bruto del fichero CSV.
     * @return el contenido decodificado como texto, sin BOM.
     */
    public static String decodificarContenido(byte[] bytes)
    {
        if (bytes == null || bytes.length == 0)
        {
            return "";
        }

        // BOM UTF-8 (EF BB BF)
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF)
        {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }

        // BOM UTF-16 LE (FF FE)
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE)
        {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }

        // BOM UTF-16 BE (FE FF)
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF)
        {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }

        // Sin BOM: intentamos UTF-8 estricto; si el contenido es UTF-8 válido lo usamos (caso correcto)
        CharsetDecoder decoderUtf8 = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try
        {
            return decoderUtf8.decode(ByteBuffer.wrap(bytes)).toString();
        }
        catch (CharacterCodingException characterCodingException)
        {
            // No es UTF-8 válido: respaldo a Windows-1252 (típico de exportaciones antiguas de Excel en español)
            return new String(bytes, CHARSET_RESPALDO);
        }
    }

    /**
     * 
     * @param linea La línea a parsear.
     * @return Un array de strings con los campos de la línea.
     */
    public static String[] parseLinea(String linea)
    {
        // Creamos una lista para almacenar los campos
        List<String> campos = new ArrayList<>();

        // Creamos un StringBuilder para almacenar el campo actual
        StringBuilder actual = new StringBuilder();

        // Creamos una variable para almacenar si estamos entre comillas
        boolean entreComillas = false;

        // Recorremos la línea caracter por caracter
        for (int i = 0; i < linea.length(); i++)
        {
            // Obtenemos el caracter actual
            char caracter = linea.charAt(i);

            // Si el caracter es una comilla, invertimos el estado de entreComillas
            if (caracter == '"')
            {
                entreComillas = !entreComillas;
            }
            // Si el caracter es una coma y no estamos entre comillas ...
            else if (caracter == ',' && !entreComillas)
            {
                // Añadimos el campo actual a la lista
                campos.add(actual.toString());

                // Reiniciamos el campo actual
                actual.setLength(0);
            }
            else
            {
                // Añadimos el caracter actual al campo actual
                actual.append(caracter);
            }
        }

        // Añadimos el campo actual a la lista
        campos.add(actual.toString());

        // Devolvemos el array de campos
        return campos.toArray(new String[0]);
    }

    public static String limpiarCampo(String campo)
    {
        return campo.trim().replace("\"", "");
    }
}
