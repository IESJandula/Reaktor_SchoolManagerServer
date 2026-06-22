-- ---------------------------------------------------------------------------------------------------------------------
-- Migración: ampliar la PRIMARY KEY de la tabla Espacio_Desdoble para incluir bloque_id.
-- ---------------------------------------------------------------------------------------------------------------------
-- Contexto del bug (Duplicate entry '2025/2026-0.1' for key 'espacio_desdoble.PRIMARY'):
--   En una iteración anterior, EspacioDesdoble pasó a ser una entidad de RELACIÓN espacio ↔ bloque con PK compuesta
--   IdEspacioDesdoble = (cursoAcademico, nombre, bloque_id). Sin embargo, la tabla física en MySQL conserva la PK
--   ANTIGUA de 2 columnas (cursoAcademico, nombre). Hibernate con ddl-auto=update/validate NO altera las PK ya
--   existentes, por eso al insertar una segunda aula de desdoble para el mismo espacio en otro bloque (o la misma
--   aula con distinto bloque) colisiona con la PK de 2 columnas.
--
-- Solución: soltar la PK antigua y crear la PK compuesta de 3 columnas (cursoAcademico, nombre, bloque_id), de modo
-- que el mismo espacio pueda repetirse para N bloques sin violar la clave primaria.
--
-- Columnas reales en BBDD (la estrategia de nombres de Spring convierte cursoAcademico -> curso_academico):
--   * curso_academico VARCHAR(9)
--   * nombre          VARCHAR(100)
--   * bloque_id       BIGINT
--
-- El script es IDEMPOTENTE y SEGURO: solo actúa si la tabla existe y la PK aún no incluye bloque_id. Puede ejecutarse
-- varias veces sin efecto adverso. Aplicar UNA vez en el entorno cuya BBDD conserva la PK antigua (típicamente el VPS,
-- o el local si no se ha recreado el esquema).
-- ---------------------------------------------------------------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS migrar_pk_espacio_desdoble $$

CREATE PROCEDURE migrar_pk_espacio_desdoble()
BEGIN
    DECLARE v_tabla_existe       INT DEFAULT 0;
    DECLARE v_bloque_en_pk       INT DEFAULT 0;
    DECLARE v_tiene_pk           INT DEFAULT 0;

    -- 1) Comprobamos que la tabla existe (independiente de mayúsculas/minúsculas del nombre)
    SELECT COUNT(*) INTO v_tabla_existe
      FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE()
       AND LOWER(TABLE_NAME) = 'espacio_desdoble';

    IF v_tabla_existe > 0 THEN

        -- 2) ¿La PRIMARY KEY ya incluye la columna bloque_id? Si es así, no hay nada que migrar
        SELECT COUNT(*) INTO v_bloque_en_pk
          FROM information_schema.KEY_COLUMN_USAGE
         WHERE TABLE_SCHEMA = DATABASE()
           AND LOWER(TABLE_NAME) = 'espacio_desdoble'
           AND CONSTRAINT_NAME = 'PRIMARY'
           AND LOWER(COLUMN_NAME) = 'bloque_id';

        IF v_bloque_en_pk = 0 THEN

            -- 3) Limpiamos posibles filas residuales del modelo antiguo sin bloque (bloque_id NULL), que impedirían
            --    declarar bloque_id como NOT NULL / parte de la PK.
            DELETE FROM `Espacio_Desdoble` WHERE `bloque_id` IS NULL;

            -- 4) La columna bloque_id debe ser NOT NULL para formar parte de la PK.
            ALTER TABLE `Espacio_Desdoble` MODIFY `bloque_id` BIGINT NOT NULL;

            -- 5) Soltamos la PRIMARY KEY antigua (de 2 columnas) si existe.
            SELECT COUNT(*) INTO v_tiene_pk
              FROM information_schema.TABLE_CONSTRAINTS
             WHERE TABLE_SCHEMA = DATABASE()
               AND LOWER(TABLE_NAME) = 'espacio_desdoble'
               AND CONSTRAINT_TYPE = 'PRIMARY KEY';

            IF v_tiene_pk > 0 THEN
                ALTER TABLE `Espacio_Desdoble` DROP PRIMARY KEY;
            END IF;

            -- 6) Creamos la nueva PRIMARY KEY compuesta de 3 columnas.
            ALTER TABLE `Espacio_Desdoble` ADD PRIMARY KEY (`curso_academico`, `nombre`, `bloque_id`);

        END IF;

    END IF;
END $$

DELIMITER ;

CALL migrar_pk_espacio_desdoble();

DROP PROCEDURE IF EXISTS migrar_pk_espacio_desdoble;

-- ---------------------------------------------------------------------------------------------------------------------
-- Comprobación opcional: las tres columnas deben aparecer como parte de la PRIMARY KEY
-- ---------------------------------------------------------------------------------------------------------------------
-- SELECT COLUMN_NAME, ORDINAL_POSITION
--   FROM information_schema.KEY_COLUMN_USAGE
--  WHERE TABLE_SCHEMA = DATABASE()
--    AND LOWER(TABLE_NAME) = 'espacio_desdoble'
--    AND CONSTRAINT_NAME = 'PRIMARY'
--  ORDER BY ORDINAL_POSITION;
