-- ---------------------------------------------------------------------------------------------------------------------
-- Migración: reasignar el "conjunto de trabajo" guardado como GLOBAL (curso_academico = '') al curso académico real
--            '2025/2026', que es donde el usuario creó realmente esos datos.
-- ---------------------------------------------------------------------------------------------------------------------
-- CONTEXTO
--   Antes del refactor "todo por curso académico", matrículas, cursos/etapas, grupos, asignaturas, etc. se guardaban
--   con curso_academico = '' (Constants.CURSO_ACADEMICO_GLOBAL). Tras el refactor, todas esas filas quedan huérfanas
--   porque las consultas filtran ahora por el año seleccionado. Este script las "adopta" para 2025/2026.
--
-- QUÉ SE MIGRA (curso_academico = ''  ->  '2025/2026'):
--   * curso_etapa.curso_academico                      (catálogo de curso/etapa: ver nota de COLISIONES)
--   * curso_etapa_grupo.curso_academico                (grupos docentes A / Sin grupo / Optativas...)
--   * asignatura.curso_academico                       (SOLO la columna propia, NO las FK a departamento)
--   * matricula.curso_academico
--   * datos_bruto_alumno_matricula.curso_academico
--   * impartir.asignatura_curso_academico              (0 filas hoy, incluido por idempotencia)
--   * generador_asignada_impartir.impartir_asignatura_curso_academico       (0 filas hoy)
--   * generador_restricciones_impartir.impartir_asignatura_curso_academico  (0 filas hoy)
--   * reduccion.curso_academico                        (0 filas hoy; FK a curso_etapa_grupo)
--
-- QUÉ **NO** SE TOCA (son catálogo GLOBAL legítimo y deben seguir con curso_academico = ''):
--   * departamento.curso_academico                                  (catálogo global de departamentos)
--   * asignatura.departamento_propietario_curso_academico           (FK -> departamento global)
--   * asignatura.departamento_receptor_curso_academico              (FK -> departamento global)
--   * profesor.departamento_curso_academico                         (FK -> departamento global)
--   * curso_academico (tabla de catálogo de años)                   (define los años; no es dato de trabajo)
--   * espacio_fijo / espacio_desdoble / espacio_sin_docencia / ocupa_espacio_desdoble
--                                                                   (los espacios ya se crearon por año = '2025/2026')
--
-- COLISIONES DE CLAVE PRIMARIA (IMPORTANTE)
--   El usuario YA había creado parte del catálogo directamente en 2025/2026 (p. ej. curso_etapa (1,'ESO') y (2,'ESO')).
--   Al intentar mover la fila heredada '' al mismo año, su PK colisionaría con la fila ya existente de 2025/2026.
--   Estrategia segura:
--     1) Primero se re-apuntan los HIJOS (asignatura, matricula, datos_bruto, ...) al año 2025/2026; al hacerlo pasan a
--        referenciar la fila PADRE de 2025/2026 que YA existe (mismo curso/etapa/grupo).
--     2) Para las filas PADRE heredadas '' se hace:
--          - UPDATE a '2025/2026' SOLO si NO existe ya su "gemela" en 2025/2026 (no hay colisión).
--          - DELETE de la fila '' SOLO si su gemela 2025/2026 YA existe (es un duplicado redundante del catálogo;
--            su contenido —p. ej. esoBachillerato— ya está representado por la fila de 2025/2026, así que no se pierde
--            información de trabajo). Esta es la ÚNICA operación de borrado, limitada a duplicados de catálogo padre.
--
-- REQUISITOS
--   * Debe existir la fila '2025/2026' en la tabla `curso_academico` (verificado: existe).
--   * Se desactiva temporalmente FOREIGN_KEY_CHECKS para poder reescribir claves padre/hijo sin problemas de orden.
--
-- IDEMPOTENCIA
--   * Todos los UPDATE/DELETE filtran por curso_academico = '' , de modo que reejecutar el script no produce cambios
--     adicionales una vez migrados los datos.
--
-- ENTORNOS
--   * VPS (ddl-auto=validate): aplicar UNA VEZ; los datos persisten.
--   * Local (ddl-auto=create): el esquema se recrea (y se vacía) en cada arranque del backend; este script solo tiene
--     sentido si la BBDD ya contiene los datos heredados con ''. Para conservar datos en local usar ddl-auto=update/validate.
-- ---------------------------------------------------------------------------------------------------------------------

START TRANSACTION;

SET @anyo := '2025/2026';

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================================================================
-- PASO 1: Re-apuntar los HIJOS al año 2025/2026 (pasan a referenciar las filas padre de 2025/2026).
-- =====================================================================================================================
UPDATE asignatura
   SET curso_academico = @anyo
 WHERE curso_academico = '';

UPDATE matricula
   SET curso_academico = @anyo
 WHERE curso_academico = '';

UPDATE datos_bruto_alumno_matricula
   SET curso_academico = @anyo
 WHERE curso_academico = '';

UPDATE impartir
   SET asignatura_curso_academico = @anyo
 WHERE asignatura_curso_academico = '';

UPDATE reduccion
   SET curso_academico = @anyo
 WHERE curso_academico = '';

UPDATE generador_asignada_impartir
   SET impartir_asignatura_curso_academico = @anyo
 WHERE impartir_asignatura_curso_academico = '';

UPDATE generador_restricciones_impartir
   SET impartir_asignatura_curso_academico = @anyo
 WHERE impartir_asignatura_curso_academico = '';

-- =====================================================================================================================
-- PASO 2: Tablas PADRE de catálogo (curso_etapa_grupo y curso_etapa), con manejo de colisiones de PK.
--          2a) UPDATE de las filas '' que NO colisionan (no existe gemela en 2025/2026).
--          2b) DELETE de las filas '' que SÍ colisionan (su gemela 2025/2026 ya existe -> duplicado redundante).
-- =====================================================================================================================

-- ---- curso_etapa_grupo ----
UPDATE curso_etapa_grupo ceg
   SET ceg.curso_academico = @anyo
 WHERE ceg.curso_academico = ''
   AND NOT EXISTS (
        SELECT 1 FROM (SELECT * FROM curso_etapa_grupo) g
         WHERE g.curso_academico = @anyo
           AND g.curso = ceg.curso
           AND g.etapa = ceg.etapa
           AND g.grupo = ceg.grupo
   );

DELETE ceg FROM curso_etapa_grupo ceg
 WHERE ceg.curso_academico = ''
   AND EXISTS (
        SELECT 1 FROM (SELECT * FROM curso_etapa_grupo) g
         WHERE g.curso_academico = @anyo
           AND g.curso = ceg.curso
           AND g.etapa = ceg.etapa
           AND g.grupo = ceg.grupo
   );

-- ---- curso_etapa ----
UPDATE curso_etapa ce
   SET ce.curso_academico = @anyo
 WHERE ce.curso_academico = ''
   AND NOT EXISTS (
        SELECT 1 FROM (SELECT * FROM curso_etapa) c
         WHERE c.curso_academico = @anyo
           AND c.curso = ce.curso
           AND c.etapa = ce.etapa
   );

DELETE ce FROM curso_etapa ce
 WHERE ce.curso_academico = ''
   AND EXISTS (
        SELECT 1 FROM (SELECT * FROM curso_etapa) c
         WHERE c.curso_academico = @anyo
           AND c.curso = ce.curso
           AND c.etapa = ce.etapa
   );

SET FOREIGN_KEY_CHECKS = 1;

COMMIT;

-- ---------------------------------------------------------------------------------------------------------------------
-- VERIFICACIÓN (ejecutar tras el COMMIT). Todas deben devolver 0 filas con '' salvo departamento (catálogo global = 1).
-- ---------------------------------------------------------------------------------------------------------------------
-- SELECT 'curso_etapa'       AS tabla, COUNT(*) AS filas_vacias FROM curso_etapa                        WHERE curso_academico = ''
-- UNION ALL SELECT 'curso_etapa_grupo', COUNT(*) FROM curso_etapa_grupo                                 WHERE curso_academico = ''
-- UNION ALL SELECT 'asignatura',        COUNT(*) FROM asignatura                                        WHERE curso_academico = ''
-- UNION ALL SELECT 'matricula',         COUNT(*) FROM matricula                                         WHERE curso_academico = ''
-- UNION ALL SELECT 'datos_bruto',       COUNT(*) FROM datos_bruto_alumno_matricula                      WHERE curso_academico = ''
-- UNION ALL SELECT 'impartir',          COUNT(*) FROM impartir                                          WHERE asignatura_curso_academico = ''
-- UNION ALL SELECT 'departamento(OK=1)',COUNT(*) FROM departamento                                      WHERE curso_academico = '';
