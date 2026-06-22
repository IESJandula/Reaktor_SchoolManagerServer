-- ---------------------------------------------------------------------------------------------------------------------
-- Migración: ampliar la PRIMARY KEY de la tabla Espacio_Desdoble para incluir la columna asignatura.
-- ---------------------------------------------------------------------------------------------------------------------
-- Contexto:
--   La asignación de aulas de desdoble pasa a ser POR ASIGNATURA. La entidad EspacioDesdoble tiene ahora una PK
--   compuesta de 4 columnas: IdEspacioDesdoble = (cursoAcademico, nombre, bloque_id, asignatura), de modo que cada
--   asignatura de un bloque pueda tener su propia aula (a lo sumo una por asignatura).
--
--   En entornos con Hibernate ddl-auto=validate/update (p. ej. el VPS), la tabla física conserva la PK ANTIGUA de
--   3 columnas (cursoAcademico, nombre, bloque_id) y NO se altera automáticamente. Este script añade la columna
--   asignatura y reconstruye la PRIMARY KEY a 4 columnas.
--
-- Columnas reales en BBDD (estrategia de nombres de Spring: cursoAcademico -> curso_academico):
--   * curso_academico VARCHAR(9)
--   * nombre          VARCHAR(100)
--   * bloque_id       BIGINT
--   * asignatura      VARCHAR(100)
--
-- El script es IDEMPOTENTE y SEGURO: solo actúa si la tabla existe y la PK aún no incluye la columna asignatura. Puede
-- ejecutarse varias veces sin efecto adverso. Aplicar UNA vez en el entorno cuya BBDD conserva la PK antigua.
-- ---------------------------------------------------------------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS migrar_pk_espacio_desdoble_asignatura $$

CREATE PROCEDURE migrar_pk_espacio_desdoble_asignatura()
BEGIN
    DECLARE v_tabla_existe       INT DEFAULT 0;
    DECLARE v_asig_en_pk         INT DEFAULT 0;
    DECLARE v_columna_existe     INT DEFAULT 0;
    DECLARE v_bloque_existe      INT DEFAULT 0;
    DECLARE v_tiene_pk           INT DEFAULT 0;
    DECLARE v_idx_bloque         INT DEFAULT 0;
    DECLARE v_idx_curso          INT DEFAULT 0;

    -- 1) Comprobamos que la tabla existe (independiente de mayúsculas/minúsculas del nombre)
    SELECT COUNT(*) INTO v_tabla_existe
      FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE()
       AND LOWER(TABLE_NAME) = 'espacio_desdoble';

    IF v_tabla_existe > 0 THEN

        -- 2) ¿La PRIMARY KEY ya incluye la columna asignatura? Si es así, no hay nada que migrar
        SELECT COUNT(*) INTO v_asig_en_pk
          FROM information_schema.KEY_COLUMN_USAGE
         WHERE TABLE_SCHEMA = DATABASE()
           AND LOWER(TABLE_NAME) = 'espacio_desdoble'
           AND CONSTRAINT_NAME = 'PRIMARY'
           AND LOWER(COLUMN_NAME) = 'asignatura';

        IF v_asig_en_pk = 0 THEN

            -- 3) Aseguramos que existe la columna asignatura (NOT NULL, por defecto '' para filas heredadas).
            SELECT COUNT(*) INTO v_columna_existe
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND LOWER(TABLE_NAME) = 'espacio_desdoble'
               AND LOWER(COLUMN_NAME) = 'asignatura';

            IF v_columna_existe = 0 THEN
                ALTER TABLE `Espacio_Desdoble` ADD COLUMN `asignatura` VARCHAR(100) NOT NULL DEFAULT '';
            ELSE
                UPDATE `Espacio_Desdoble` SET `asignatura` = '' WHERE `asignatura` IS NULL;
                ALTER TABLE `Espacio_Desdoble` MODIFY `asignatura` VARCHAR(100) NOT NULL DEFAULT '';
            END IF;

            -- 4) Aseguramos que existe la columna bloque_id NOT NULL (por si se aplica sobre el esquema antiguo de 2 cols).
            SELECT COUNT(*) INTO v_bloque_existe
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND LOWER(TABLE_NAME) = 'espacio_desdoble'
               AND LOWER(COLUMN_NAME) = 'bloque_id';

            IF v_bloque_existe = 0 THEN
                ALTER TABLE `Espacio_Desdoble` ADD COLUMN `bloque_id` BIGINT NOT NULL DEFAULT 0;
            ELSE
                DELETE FROM `Espacio_Desdoble` WHERE `bloque_id` IS NULL;
                ALTER TABLE `Espacio_Desdoble` MODIFY `bloque_id` BIGINT NOT NULL;
            END IF;

            -- 5) Las claves foráneas salientes (bloque_id -> bloque, curso_academico -> curso_academico) pueden
            --    estar usando la PRIMARY KEY como índice. Antes de soltar la PK, garantizamos índices propios sobre
            --    esas columnas para que MySQL no rechace el DROP PRIMARY KEY ("needed in a foreign key constraint").
            SELECT COUNT(*) INTO v_idx_bloque
              FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE()
               AND LOWER(TABLE_NAME) = 'espacio_desdoble'
               AND LOWER(COLUMN_NAME) = 'bloque_id'
               AND SEQ_IN_INDEX = 1
               AND INDEX_NAME <> 'PRIMARY';

            IF v_idx_bloque = 0 THEN
                ALTER TABLE `Espacio_Desdoble` ADD INDEX `idx_espdes_bloque_id` (`bloque_id`);
            END IF;

            SELECT COUNT(*) INTO v_idx_curso
              FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE()
               AND LOWER(TABLE_NAME) = 'espacio_desdoble'
               AND LOWER(COLUMN_NAME) = 'curso_academico'
               AND SEQ_IN_INDEX = 1
               AND INDEX_NAME <> 'PRIMARY';

            IF v_idx_curso = 0 THEN
                ALTER TABLE `Espacio_Desdoble` ADD INDEX `idx_espdes_curso_academico` (`curso_academico`);
            END IF;

            -- 6) Soltamos la PRIMARY KEY anterior (de 2 o 3 columnas) si existe.
            SELECT COUNT(*) INTO v_tiene_pk
              FROM information_schema.TABLE_CONSTRAINTS
             WHERE TABLE_SCHEMA = DATABASE()
               AND LOWER(TABLE_NAME) = 'espacio_desdoble'
               AND CONSTRAINT_TYPE = 'PRIMARY KEY';

            IF v_tiene_pk > 0 THEN
                ALTER TABLE `Espacio_Desdoble` DROP PRIMARY KEY;
            END IF;

            -- 7) Creamos la nueva PRIMARY KEY compuesta de 4 columnas.
            ALTER TABLE `Espacio_Desdoble` ADD PRIMARY KEY (`curso_academico`, `nombre`, `bloque_id`, `asignatura`);

            -- 8) Quitamos el DEFAULT '' de asignatura para alinear con el modelo JPA (la app siempre informa el valor).
            ALTER TABLE `Espacio_Desdoble` MODIFY `asignatura` VARCHAR(100) NOT NULL;

        END IF;

    END IF;
END $$

DELIMITER ;

CALL migrar_pk_espacio_desdoble_asignatura();

DROP PROCEDURE IF EXISTS migrar_pk_espacio_desdoble_asignatura;

-- ---------------------------------------------------------------------------------------------------------------------
-- Comprobación opcional: las cuatro columnas deben aparecer como parte de la PRIMARY KEY
-- ---------------------------------------------------------------------------------------------------------------------
-- SELECT COLUMN_NAME, ORDINAL_POSITION
--   FROM information_schema.KEY_COLUMN_USAGE
--  WHERE TABLE_SCHEMA = DATABASE()
--    AND LOWER(TABLE_NAME) = 'espacio_desdoble'
--    AND CONSTRAINT_NAME = 'PRIMARY'
--  ORDER BY ORDINAL_POSITION;
