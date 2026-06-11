-- ---------------------------------------------------------------------------------------------------------------------
-- Migración: añadir ON DELETE CASCADE a las FK necesarias para el borrado en cascada de matrículas.
-- ---------------------------------------------------------------------------------------------------------------------
-- Pares (tabla -> tabla referenciada) afectados:
--   * Matricula                    -> Asignatura          (columnas: curso, etapa, grupo, nombre)
--   * Matricula                    -> Alumno              (columna : alumno_id)
--   * Asignatura                   -> Curso_Etapa_Grupo   (columnas: curso, etapa, grupo)
--   * Datos_Bruto_Alumno_Matricula -> Curso_Etapa         (columnas: curso_etapa_curso, curso_etapa_etapa)
--
-- El nombre de las FK lo genera Hibernate automáticamente (ej. FKxxxxxxx), por eso este script localiza dinámicamente
-- la FK existente entre cada par de tablas, la suelta y la recrea con ON DELETE CASCADE.
--
-- Aplicar UNA SOLA VEZ en el VPS (la base de datos en local con ddl-auto=update también puede beneficiarse, pero al
-- mantener el nombre lógico no debería volver a recrearlas).
-- ---------------------------------------------------------------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS recrear_fk_on_delete_cascade $$

-- Procedimiento auxiliar: localiza la FK actual entre dos tablas, la elimina y la recrea con ON DELETE CASCADE.
-- Parámetros:
--   p_tabla                : tabla con la FK (lado hijo)
--   p_tabla_referenciada   : tabla referenciada (lado padre)
--   p_columnas             : columnas de la FK en la tabla hija, separadas por coma y entre tildes invertidas (ej: '`curso`,`etapa`')
--   p_columnas_padre       : columnas referenciadas en la tabla padre, separadas por coma y entre tildes invertidas
--   p_nuevo_nombre         : nombre lógico que se usará para la nueva FK (ej: 'fk_matricula_alumno_cascade')
CREATE PROCEDURE recrear_fk_on_delete_cascade(
    IN p_tabla                VARCHAR(64),
    IN p_tabla_referenciada   VARCHAR(64),
    IN p_columnas             TEXT,
    IN p_columnas_padre       TEXT,
    IN p_nuevo_nombre         VARCHAR(64)
)
BEGIN
    DECLARE v_constraint_actual VARCHAR(64);

    -- Buscamos el nombre de la FK actual entre las dos tablas (cogemos la primera que aparezca)
    SELECT CONSTRAINT_NAME
      INTO v_constraint_actual
      FROM information_schema.KEY_COLUMN_USAGE
     WHERE TABLE_SCHEMA       = DATABASE()
       AND TABLE_NAME         = p_tabla
       AND REFERENCED_TABLE_NAME = p_tabla_referenciada
       AND ORDINAL_POSITION   = 1
     LIMIT 1;

    -- Si la encontramos, la soltamos
    IF v_constraint_actual IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE `', p_tabla, '` DROP FOREIGN KEY `', v_constraint_actual, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;

    -- Recreamos la FK con ON DELETE CASCADE
    SET @sql = CONCAT(
        'ALTER TABLE `', p_tabla, '` ',
        'ADD CONSTRAINT `', p_nuevo_nombre, '` ',
        'FOREIGN KEY (', p_columnas, ') ',
        'REFERENCES `', p_tabla_referenciada, '` (', p_columnas_padre, ') ',
        'ON DELETE CASCADE'
    );
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END $$

DELIMITER ;

-- ---------------------------------------------------------------------------------------------------------------------
-- Aplicación de la cascada en cada relación
-- ---------------------------------------------------------------------------------------------------------------------

-- Matricula(curso, etapa, grupo, nombre) -> Asignatura(curso, etapa, grupo, nombre)
CALL recrear_fk_on_delete_cascade(
    'Matricula',
    'Asignatura',
    '`curso`,`etapa`,`grupo`,`nombre`',
    '`curso`,`etapa`,`grupo`,`nombre`',
    'fk_matricula_asignatura_cascade'
);

-- Matricula(alumno_id) -> Alumno(id)
CALL recrear_fk_on_delete_cascade(
    'Matricula',
    'Alumno',
    '`alumno_id`',
    '`id`',
    'fk_matricula_alumno_cascade'
);

-- Asignatura(curso, etapa, grupo) -> Curso_Etapa_Grupo(curso, etapa, grupo)
CALL recrear_fk_on_delete_cascade(
    'Asignatura',
    'Curso_Etapa_Grupo',
    '`curso`,`etapa`,`grupo`',
    '`curso`,`etapa`,`grupo`',
    'fk_asignatura_cursoetapagrupo_cascade'
);

-- Datos_Bruto_Alumno_Matricula(curso_etapa_curso, curso_etapa_etapa) -> Curso_Etapa(curso, etapa)
-- Nota: los nombres curso_etapa_curso/curso_etapa_etapa los genera Hibernate por defecto al no haber @JoinColumn explícito.
CALL recrear_fk_on_delete_cascade(
    'Datos_Bruto_Alumno_Matricula',
    'Curso_Etapa',
    '`curso_etapa_curso`,`curso_etapa_etapa`',
    '`curso`,`etapa`',
    'fk_datosbruto_cursoetapa_cascade'
);

-- Limpieza: ya no necesitamos el procedimiento
DROP PROCEDURE IF EXISTS recrear_fk_on_delete_cascade;

-- ---------------------------------------------------------------------------------------------------------------------
-- Comprobación opcional: lanza esta consulta para verificar que las cuatro FKs están en CASCADE
-- ---------------------------------------------------------------------------------------------------------------------
-- SELECT TABLE_NAME, CONSTRAINT_NAME, DELETE_RULE
--   FROM information_schema.REFERENTIAL_CONSTRAINTS
--  WHERE CONSTRAINT_SCHEMA = DATABASE()
--    AND TABLE_NAME IN ('Matricula', 'Asignatura', 'Datos_Bruto_Alumno_Matricula');
