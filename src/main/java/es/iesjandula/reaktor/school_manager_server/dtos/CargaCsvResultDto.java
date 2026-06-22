package es.iesjandula.reaktor.school_manager_server.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado de una carga por fichero CSV de la configuración básica (espacios, cursos y etapas, departamentos).
 * <p>
 * Resume cuántas filas se han procesado, cuántas se han creado realmente, cuántas se han omitido por ya existir
 * (la carga es idempotente y no falla ante duplicados) y los avisos/errores no bloqueantes de filas concretas.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CargaCsvResultDto
{
    /** Número de filas de datos procesadas (sin contar la cabecera). */
    private int procesados;

    /** Número de elementos creados nuevos. */
    private int creados;

    /** Número de elementos omitidos por ya existir (idempotencia ante duplicados). */
    private int omitidos;

    /** Avisos/errores no bloqueantes de filas concretas (formato inválido, valores vacíos, etc.). */
    private List<String> errores = new ArrayList<>();
}
