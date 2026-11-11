-- =============================================================================
-- CanchaLibre - Base de datos COMPLETA (esquema + datos de prueba) en un solo archivo
-- Es la concatenacion de 01_schema.sql + 02_seed.sql
--
--   mysql --host=<host> --port=<puerto> --user=<usuario> -p --ssl-mode=REQUIRED \
--         --default-character-set=utf8mb4 defaultdb < canchalibre.sql
--
-- En MySQL local, cree antes la base:  CREATE DATABASE canchalibre CHARACTER SET utf8mb4;
-- =============================================================================

-- =============================================================================
-- CanchaLibre - Esquema de base de datos (MySQL 8.x / Aiven MySQL)
-- Curso SC-403 - Avance #2
--
-- * Compatible con Aiven: todas las tablas tienen PRIMARY KEY (Aiven lo exige,
--   sql_require_primary_key = ON) y NO se usa CREATE DATABASE (use "defaultdb").
-- * Es re-ejecutable: elimina y recrea las tablas (BORRA los datos existentes).
-- * Hibernate NO crea ni modifica tablas (spring.jpa.hibernate.ddl-auto=none).
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notificaciones;
DROP TABLE IF EXISTS transacciones;
DROP TABLE IF EXISTS reserva_insumos;
DROP TABLE IF EXISTS reservas;
DROP TABLE IF EXISTS bloques_horarios;
DROP TABLE IF EXISTS insumo_disciplinas;
DROP TABLE IF EXISTS insumos;
DROP TABLE IF EXISTS imagenes_espacio;
DROP TABLE IF EXISTS espacios;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS complejos;
DROP TABLE IF EXISTS disciplinas;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 1. roles  (ROLE_USER = deportista / ROLE_ADMIN = administrador de complejo)
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    id          INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(30)  NOT NULL,
    descripcion VARCHAR(120) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 2. disciplinas (futbol 5, padel, karting, atletismo, ...)
-- -----------------------------------------------------------------------------
CREATE TABLE disciplinas (
    id          INT          NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(5)   NOT NULL,          -- usado en el codigo de reserva (FUT, PAD, ...)
    nombre      VARCHAR(50)  NOT NULL,
    icono       VARCHAR(40)  NOT NULL,          -- clase Font Awesome
    imagen_base VARCHAR(30)  NOT NULL,          -- prefijo de la imagen placeholder
    orden       INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_disciplinas_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 3. complejos (instalaciones publicas o privadas)
-- -----------------------------------------------------------------------------
CREATE TABLE complejos (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(120)  NOT NULL,
    descripcion          TEXT          NULL,
    provincia            VARCHAR(40)   NOT NULL,
    canton               VARCHAR(60)   NOT NULL,
    direccion            VARCHAR(255)  NULL,
    latitud              DECIMAL(9,6)  NULL,
    longitud             DECIMAL(9,6)  NULL,
    tipo_administracion  VARCHAR(10)   NOT NULL,     -- PUBLICO (municipal) | PRIVADO
    destacado            TINYINT(1)    NOT NULL DEFAULT 0,
    activo               TINYINT(1)    NOT NULL DEFAULT 1,
    imagen_url           VARCHAR(600)  NULL,
    calificacion         DECIMAL(2,1)  NOT NULL DEFAULT 0.0,
    num_resenas          INT           NOT NULL DEFAULT 0,
    telefono             VARCHAR(20)   NULL,
    hora_apertura        TIME          NOT NULL DEFAULT '06:00:00',
    hora_cierre          TIME          NOT NULL DEFAULT '23:00:00',
    hora_inicio_nocturno TIME          NOT NULL DEFAULT '18:00:00',
    porcentaje_iva       DECIMAL(5,2)  NOT NULL DEFAULT 13.00,
    parqueo              TINYINT(1)    NOT NULL DEFAULT 0,
    vestidores           TINYINT(1)    NOT NULL DEFAULT 0,
    soda                 TINYINT(1)    NOT NULL DEFAULT 0,
    iluminacion_nocturna TINYINT(1)    NOT NULL DEFAULT 0,
    banco                VARCHAR(80)   NULL,         -- datos para la transferencia (HU-11)
    titular              VARCHAR(120)  NULL,
    iban                 VARCHAR(40)   NULL,
    creado_en            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_complejos_provincia (provincia),
    KEY idx_complejos_tipo (tipo_administracion),
    CONSTRAINT chk_complejos_tipo CHECK (tipo_administracion IN ('PUBLICO','PRIVADO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 4. usuarios (deportistas y administradores; el admin pertenece a un complejo)
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    rol_id      INT          NOT NULL,
    complejo_id BIGINT       NULL,
    nombre      VARCHAR(80)  NOT NULL,
    apellidos   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    password    VARCHAR(100) NOT NULL,              -- hash BCrypt
    telefono    VARCHAR(20)  NULL,
    activo      TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email),
    CONSTRAINT fk_usuarios_rol      FOREIGN KEY (rol_id)      REFERENCES roles (id),
    CONSTRAINT fk_usuarios_complejo FOREIGN KEY (complejo_id) REFERENCES complejos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 5. espacios (pistas y canchas de cada complejo, con su tarifa)
-- -----------------------------------------------------------------------------
CREATE TABLE espacios (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    complejo_id       BIGINT        NOT NULL,
    disciplina_id     INT           NOT NULL,
    nombre            VARCHAR(120)  NOT NULL,
    descripcion       TEXT          NULL,
    superficie        VARCHAR(25)   NOT NULL,        -- CESPED_SINTETICO, ASFALTO, ARCILLA, ...
    techada           TINYINT(1)    NOT NULL DEFAULT 0,
    capacidad         INT           NOT NULL DEFAULT 10,
    medidas           VARCHAR(40)   NULL,
    iluminacion       TINYINT(1)    NOT NULL DEFAULT 1,
    reglamento        TEXT          NULL,
    tipo_tarifa       VARCHAR(20)   NOT NULL DEFAULT 'POR_HORA',   -- POR_HORA | POR_TANDA | PAQUETE_GRUPAL
    tarifa_diurna     DECIMAL(12,2) NOT NULL,        -- IVA incluido
    recargo_nocturno  DECIMAL(12,2) NOT NULL DEFAULT 0.00,   -- iluminacion nocturna
    deposito_garantia DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    imagen_url        VARCHAR(600)  NULL,
    activo            TINYINT(1)    NOT NULL DEFAULT 1,
    creado_en         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_espacios_complejo (complejo_id),
    KEY idx_espacios_disciplina (disciplina_id),
    CONSTRAINT fk_espacios_complejo   FOREIGN KEY (complejo_id)   REFERENCES complejos (id),
    CONSTRAINT fk_espacios_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplinas (id),
    CONSTRAINT chk_espacios_tarifa CHECK (tarifa_diurna > 0 AND recargo_nocturno >= 0 AND deposito_garantia >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 6. imagenes_espacio (galeria de imagenes, HU-03)
-- -----------------------------------------------------------------------------
CREATE TABLE imagenes_espacio (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    espacio_id BIGINT       NOT NULL,
    url        VARCHAR(600) NOT NULL,
    orden      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_imagenes_espacio (espacio_id),
    CONSTRAINT fk_imagenes_espacio FOREIGN KEY (espacio_id) REFERENCES espacios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 7. insumos (equipamiento y servicios adicionales de cada complejo)
-- -----------------------------------------------------------------------------
CREATE TABLE insumos (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    complejo_id BIGINT        NOT NULL,
    nombre      VARCHAR(100)  NOT NULL,
    descripcion VARCHAR(255)  NULL,
    categoria   VARCHAR(15)   NOT NULL,              -- EQUIPAMIENTO | SERVICIO
    precio      DECIMAL(12,2) NOT NULL,              -- IVA incluido
    unidad      VARCHAR(20)   NOT NULL DEFAULT 'unidad',
    stock       INT           NULL,                  -- NULL = ilimitado
    imagen_url  VARCHAR(600)  NULL,
    activo      TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_insumos_complejo (complejo_id),
    CONSTRAINT fk_insumos_complejo FOREIGN KEY (complejo_id) REFERENCES complejos (id),
    CONSTRAINT chk_insumos_precio CHECK (precio > 0 AND (stock IS NULL OR stock >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 8. insumo_disciplinas (que insumos aplican a que disciplina, HU-06)
--    Un insumo SIN filas aqui aplica a todas las disciplinas.
-- -----------------------------------------------------------------------------
CREATE TABLE insumo_disciplinas (
    insumo_id     BIGINT NOT NULL,
    disciplina_id INT    NOT NULL,
    PRIMARY KEY (insumo_id, disciplina_id),
    CONSTRAINT fk_insdis_insumo     FOREIGN KEY (insumo_id)     REFERENCES insumos (id) ON DELETE CASCADE,
    CONSTRAINT fk_insdis_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplinas (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 9. bloques_horarios
--    Una fila existe solo cuando el bloque deja de estar libre (apartado, ocupado
--    o en mantenimiento). La restriccion UNIQUE impide el doble reservado
--    (overbooking) incluso con usuarios simultaneos (HU-09).
-- -----------------------------------------------------------------------------
CREATE TABLE bloques_horarios (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    espacio_id  BIGINT       NOT NULL,
    fecha       DATE         NOT NULL,
    hora_inicio TIME         NOT NULL,
    hora_fin    TIME         NOT NULL,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',  -- DISPONIBLE | APARTADO_TEMPORAL | OCUPADO | MANTENIMIENTO
    expira_en   DATETIME     NULL,                           -- solo para APARTADO_TEMPORAL
    motivo      VARCHAR(200) NULL,                           -- solo para MANTENIMIENTO
    PRIMARY KEY (id),
    UNIQUE KEY uk_bloque_espacio_fecha_hora (espacio_id, fecha, hora_inicio),
    KEY idx_bloques_estado (estado, expira_en),
    CONSTRAINT fk_bloques_espacio FOREIGN KEY (espacio_id) REFERENCES espacios (id),
    CONSTRAINT chk_bloques_estado CHECK (estado IN ('DISPONIBLE','APARTADO_TEMPORAL','OCUPADO','MANTENIMIENTO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 10. reservas (cabecera de la transaccion; guarda el precio vigente al reservar)
-- -----------------------------------------------------------------------------
CREATE TABLE reservas (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    codigo                VARCHAR(30)   NOT NULL,        -- p.ej. CLB-2026-FUT-00042
    usuario_id            BIGINT        NOT NULL,
    espacio_id            BIGINT        NOT NULL,
    bloque_id             BIGINT        NULL,            -- bloque retenido (NULL al liberarse)
    fecha                 DATE          NOT NULL,
    hora_inicio           TIME          NOT NULL,
    hora_fin              TIME          NOT NULL,
    estado                VARCHAR(20)   NOT NULL,        -- PENDIENTE_PAGO | EN_VERIFICACION | CONFIRMADA | RECHAZADA | EXPIRADA | CANCELADA | FINALIZADA
    nocturno              TINYINT(1)    NOT NULL DEFAULT 0,
    tarifa_diurna         DECIMAL(12,2) NOT NULL,
    recargo_nocturno      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    subtotal_pista        DECIMAL(12,2) NOT NULL,
    subtotal_insumos      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    impuesto              DECIMAL(12,2) NOT NULL DEFAULT 0.00,   -- IVA incluido en el total
    total                 DECIMAL(12,2) NOT NULL,
    deposito_garantia     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    expira_en             DATETIME      NULL,            -- limite de 15 min para el pago (HU-10)
    referencia_bancaria   VARCHAR(60)   NULL,
    comprobante_url       VARCHAR(700)  NULL,
    comprobante_nombre    VARCHAR(200)  NULL,
    comprobante_subido_en DATETIME      NULL,
    motivo_rechazo        VARCHAR(300)  NULL,
    creado_en             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservas_codigo (codigo),
    KEY idx_reservas_usuario (usuario_id, fecha),
    KEY idx_reservas_estado (estado, expira_en),
    KEY idx_reservas_espacio_fecha (espacio_id, fecha),
    CONSTRAINT fk_reservas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_reservas_espacio FOREIGN KEY (espacio_id) REFERENCES espacios (id),
    CONSTRAINT fk_reservas_bloque  FOREIGN KEY (bloque_id)  REFERENCES bloques_horarios (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 11. reserva_insumos (detalle de insumos de cada reserva)
-- -----------------------------------------------------------------------------
CREATE TABLE reserva_insumos (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    reserva_id      BIGINT        NOT NULL,
    insumo_id       BIGINT        NOT NULL,
    nombre_insumo   VARCHAR(100)  NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_resins_reserva (reserva_id),
    KEY idx_resins_insumo (insumo_id),
    CONSTRAINT fk_resins_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id) ON DELETE CASCADE,
    CONSTRAINT fk_resins_insumo  FOREIGN KEY (insumo_id)  REFERENCES insumos (id),
    CONSTRAINT chk_resins_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 12. transacciones (bitacora transaccional: cada cambio de estado/monto)
-- -----------------------------------------------------------------------------
CREATE TABLE transacciones (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    reserva_id      BIGINT        NOT NULL,
    usuario_id      BIGINT        NULL,                  -- quien ejecuto la accion (NULL = sistema)
    tipo            VARCHAR(25)   NOT NULL,              -- RESERVA_CREADA | PAGO_REGISTRADO | PAGO_APROBADO | PAGO_RECHAZADO | CANCELACION | EXPIRACION | FINALIZACION
    estado_anterior VARCHAR(20)   NULL,
    estado_nuevo    VARCHAR(20)   NOT NULL,
    monto           DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    detalle         VARCHAR(300)  NULL,
    creado_en       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_trans_reserva (reserva_id),
    KEY idx_trans_fecha (creado_en),
    CONSTRAINT fk_trans_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 13. notificaciones (avisos al usuario, p.ej. motivo de rechazo, HU-15)
-- -----------------------------------------------------------------------------
CREATE TABLE notificaciones (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT       NOT NULL,
    reserva_id BIGINT       NULL,
    titulo     VARCHAR(120) NOT NULL,
    mensaje    VARCHAR(500) NOT NULL,
    leida      TINYINT(1)   NOT NULL DEFAULT 0,
    creado_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notif_usuario (usuario_id, leida),
    CONSTRAINT fk_notif_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- CanchaLibre - Datos de prueba (seed)  |  Curso SC-403 - Avance #2
-- Ejecutar DESPUES de 01_schema.sql
--
-- Credenciales de demostracion
--   Administrador  admin@canchalibre.test           / Admin123!   (Sport Center Heredia)
--   Administrador  admin.padel@canchalibre.test     / Admin123!   (Padel Zone)
--   Administrador  admin.cartago@canchalibre.test   / Admin123!   (Complejo Municipal de Cartago)
--   Deportista     deportista@canchalibre.test      / Usuario123! (usuario demo con reservas en todos los estados)
--   Deportistas    javier@/maria@/carlos@/ana@/luis@/sofia@/diego@/valentina@canchalibre.test / Usuario123!
--
-- Las fechas son RELATIVAS al dia en que se carga el script (hora de Costa Rica, UTC-6).
-- =============================================================================
SET NAMES utf8mb4;
SET @ahora = UTC_TIMESTAMP() - INTERVAL 6 HOUR;
SET @hoy   = DATE(@ahora);

INSERT INTO roles (id, nombre, descripcion) VALUES
  (1, 'ROLE_USER', 'Deportista / usuario registrado'),
  (2, 'ROLE_ADMIN', 'Administrador o encargado de complejo');

INSERT INTO disciplinas (id, codigo, nombre, icono, imagen_base, orden) VALUES
  (1, 'FUT', 'Fútbol 5', 'fa-futbol', 'futbol', 1),
  (2, 'FUT', 'Fútbol 7', 'fa-futbol', 'futbol', 2),
  (3, 'FUT', 'Fútbol 11', 'fa-futbol', 'futbol', 3),
  (4, 'VOL', 'Voleibol', 'fa-volleyball', 'voleibol', 4),
  (5, 'TEN', 'Tenis', 'fa-table-tennis-paddle-ball', 'tenis', 5),
  (6, 'PAD', 'Pádel', 'fa-table-tennis-paddle-ball', 'padel', 6),
  (7, 'BAS', 'Básquetbol', 'fa-basketball', 'basquet', 7),
  (8, 'ATL', 'Atletismo', 'fa-person-running', 'atletismo', 8),
  (9, 'KAR', 'Karting', 'fa-flag-checkered', 'karting', 9);

INSERT INTO complejos (id, nombre, descripcion, provincia, canton, direccion, latitud, longitud, tipo_administracion, destacado, activo, imagen_url, calificacion, num_resenas, telefono, hora_apertura, hora_cierre, hora_inicio_nocturno, porcentaje_iva, parqueo, vestidores, soda, iluminacion_nocturna, banco, titular, iban) VALUES
  (1, 'Sport Center Heredia', 'Sport Center Heredia es un complejo deportivo privado ubicado en Heredia, Heredia. Cuenta con espacios para fútbol 5, fútbol 7 y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Heredia', 'Heredia', 'Heredia, 100 m norte del Mall Paseo de las Flores', 9.998, -84.117, 'PRIVADO', 1, 1, '/img/placeholders/futbol-1.svg', 4.8, 124, '2260-1100', '06:00:00', '23:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'Banco Nacional de Costa Rica', 'Sport Center Heredia S.A.', 'CR12 0152 0100 1234 5678 90'),
  (2, 'Padel Zone', 'Padel Zone es un complejo deportivo privado ubicado en Alajuelita, San José. Cuenta con espacios para pádel y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'San José', 'Alajuelita', 'Alajuelita centro, 200 m sur de la iglesia', 9.901, -84.103, 'PRIVADO', 0, 1, '/img/placeholders/padel-1.svg', 4.6, 98, '2254-3300', '07:00:00', '23:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'BAC Credomatic', 'Padel Zone Alajuelita S.R.L.', 'CR05 0102 0000 9876 5432 10'),
  (3, 'La Canchita SJ', 'La Canchita SJ es un complejo deportivo privado ubicado en San José, San José. Cuenta con espacios para fútbol 5 y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'San José', 'San José', 'San José, barrio Escalante, 150 m este del parque', 9.934, -84.068, 'PRIVADO', 0, 1, '/img/placeholders/futbol-1.svg', 4.7, 156, '2222-4400', '06:00:00', '23:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'Banco de Costa Rica', 'La Canchita SJ S.A.', 'CR33 0152 0200 1122 3344 55'),
  (4, 'Pista de Atletismo Municipal de Heredia', 'Pista de Atletismo Municipal de Heredia es un complejo deportivo municipal ubicado en Heredia, Heredia. Cuenta con espacios para atletismo y servicios como parqueo, vestidores, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Heredia', 'Heredia', 'Heredia, frente al Polideportivo Municipal', 9.9995, -84.1215, 'PUBLICO', 0, 1, '/img/placeholders/atletismo-2.svg', 4.2, 41, '2237-0010', '05:00:00', '21:00:00', '18:00:00', 13.00, 1, 1, 0, 1, 'Banco de Costa Rica', 'Municipalidad de Heredia', 'CR33 0152 0200 1122 3344 55'),
  (5, 'Go-Karts Pista Rápida', 'Go-Karts Pista Rápida es un complejo deportivo privado ubicado en Alajuela, Alajuela. Cuenta con espacios para karting y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Alajuela', 'Alajuela', 'Alajuela, 1 km oeste del aeropuerto', 10.011, -84.208, 'PRIVADO', 1, 1, '/img/placeholders/karting-1.svg', 4.9, 212, '2441-7788', '10:00:00', '22:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'BAC Credomatic', 'Pista Rápida Karting S.A.', 'CR05 0102 0000 9876 5432 10'),
  (6, 'Complejo Deportivo Municipal de Cartago', 'Complejo Deportivo Municipal de Cartago es un complejo deportivo municipal ubicado en Cartago, Cartago. Cuenta con espacios para básquetbol, fútbol 11, voleibol y servicios como parqueo, vestidores, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Cartago', 'Cartago', 'Cartago, 300 m sur de la Basílica', 9.864, -83.919, 'PUBLICO', 0, 1, '/img/placeholders/futbol-2.svg', 4.3, 77, '2551-2020', '06:00:00', '21:00:00', '18:00:00', 13.00, 1, 1, 0, 1, 'Banco de Costa Rica', 'Municipalidad de Cartago', 'CR33 0152 0200 1122 3344 55'),
  (7, 'Raquetas del Valle', 'Raquetas del Valle es un complejo deportivo privado ubicado en Escazú, San José. Cuenta con espacios para pádel, tenis y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'San José', 'Escazú', 'Escazú, San Rafael, costado norte del parque', 9.919, -84.14, 'PRIVADO', 0, 1, '/img/placeholders/tenis-2.svg', 4.5, 63, '2289-5050', '06:00:00', '22:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'Banco Nacional de Costa Rica', 'Raquetas del Valle S.A.', 'CR12 0152 0100 1234 5678 90'),
  (8, 'Arena Voley Beach Jacó', 'Arena Voley Beach Jacó es un complejo deportivo privado ubicado en Garabito, Puntarenas. Cuenta con espacios para voleibol y servicios como parqueo, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Puntarenas', 'Garabito', 'Jacó, 100 m de la playa, frente a la plaza', 9.615, -84.628, 'PRIVADO', 0, 1, '/img/placeholders/voleibol-3.svg', 4.4, 52, '2643-1212', '08:00:00', '21:00:00', '18:00:00', 13.00, 1, 0, 1, 1, 'BAC Credomatic', 'Arena Voley Jacó S.A.', 'CR05 0102 0000 9876 5432 10'),
  (9, 'Polideportivo Municipal de Liberia', 'Polideportivo Municipal de Liberia es un complejo deportivo municipal ubicado en Liberia, Guanacaste. Cuenta con espacios para básquetbol, fútbol 5 y servicios como parqueo, vestidores, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Guanacaste', 'Liberia', 'Liberia, 400 m norte del parque Mario Cañas', 10.635, -85.437, 'PUBLICO', 0, 1, '/img/placeholders/basquet-1.svg', 4.1, 38, '2666-3030', '06:00:00', '21:00:00', '18:00:00', 13.00, 1, 1, 0, 1, 'Banco de Costa Rica', 'Municipalidad de Liberia', 'CR33 0152 0200 1122 3344 55'),
  (10, 'Club Deportivo Escazú', 'Club Deportivo Escazú es un complejo deportivo privado ubicado en Escazú, San José. Cuenta con espacios para fútbol 7, tenis y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'San José', 'Escazú', 'Escazú centro, 50 m este de la Municipalidad', 9.918, -84.1395, 'PRIVADO', 0, 1, '/img/placeholders/futbol-1.svg', 4.6, 89, '2288-6060', '06:00:00', '23:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'Banco Nacional de Costa Rica', 'Club Deportivo Escazú S.A.', 'CR12 0152 0100 1234 5678 90'),
  (11, 'Complejo El Roble', 'Complejo El Roble es un complejo deportivo privado ubicado en San Ramón, Alajuela. Cuenta con espacios para fútbol 5 y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Alajuela', 'San Ramón', 'San Ramón, 1 km norte del Hospital', 10.088, -84.47, 'PRIVADO', 0, 1, '/img/placeholders/futbol-1.svg', 4.5, 70, '2445-7070', '06:00:00', '23:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'Banco de Costa Rica', 'Complejo El Roble S.A.', 'CR33 0152 0200 1122 3344 55'),
  (12, 'Estadio Comunal de San Pablo', 'Estadio Comunal de San Pablo es un complejo deportivo municipal ubicado en San Pablo, Heredia. Cuenta con espacios para atletismo, fútbol 11 y servicios como parqueo, vestidores, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Heredia', 'San Pablo', 'San Pablo de Heredia, costado oeste de la iglesia', 9.992, -84.096, 'PUBLICO', 0, 1, '/img/placeholders/futbol-3.svg', 4.0, 29, '2239-8080', '06:00:00', '21:00:00', '18:00:00', 13.00, 1, 1, 0, 1, 'Banco de Costa Rica', 'Municipalidad de San Pablo', 'CR33 0152 0200 1122 3344 55'),
  (13, 'Padel Club Santa Ana', 'Padel Club Santa Ana es un complejo deportivo privado ubicado en Santa Ana, San José. Cuenta con espacios para pádel y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'San José', 'Santa Ana', 'Santa Ana, Pozos, 200 m oeste del Forum', 9.938, -84.185, 'PRIVADO', 0, 1, '/img/placeholders/padel-1.svg', 4.7, 101, '2282-9090', '07:00:00', '23:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'BAC Credomatic', 'Padel Club Santa Ana S.A.', 'CR05 0102 0000 9876 5432 10'),
  (14, 'Karting Park Cartago', 'Karting Park Cartago es un complejo deportivo privado ubicado en Cartago, Cartago. Cuenta con espacios para karting y servicios como parqueo, vestidores, soda, iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'Cartago', 'Cartago', 'Cartago, Taras, 500 m este de la ruta 2', 9.883, -83.936, 'PRIVADO', 0, 1, '/img/placeholders/karting-1.svg', 4.6, 133, '2552-1414', '10:00:00', '22:00:00', '18:00:00', 13.00, 1, 1, 1, 1, 'Banco Nacional de Costa Rica', 'Karting Park Cartago S.A.', 'CR12 0152 0100 1234 5678 90'),
  (15, 'Multicancha Comunal de Curridabat', 'Multicancha Comunal de Curridabat es un complejo deportivo municipal ubicado en Curridabat, San José. Cuenta con espacios para básquetbol, fútbol 5 y servicios como iluminación nocturna. Reserve en línea, consulte la disponibilidad en tiempo real y confirme su espacio en minutos.', 'San José', 'Curridabat', 'Curridabat, Granadilla, frente a la escuela', 9.912, -84.036, 'PUBLICO', 0, 1, '/img/placeholders/basquet-1.svg', 3.9, 22, '2272-1818', '06:00:00', '21:00:00', '18:00:00', 13.00, 0, 0, 0, 1, 'Banco de Costa Rica', 'Municipalidad de Curridabat', 'CR33 0152 0200 1122 3344 55');

INSERT INTO usuarios (id, rol_id, complejo_id, nombre, apellidos, email, password, telefono, activo) VALUES
  (1, 2, 1, 'Moisés', 'Díaz Darcia', 'admin@canchalibre.test', '$2a$10$99fnPULQCvdbV03CSJbS1Ov08ypvL7dTRC8.BwRs9r/MPWOKUoT..', '8888-0001', 1),
  (2, 2, 2, 'Randy', 'Martínez Sandí', 'admin.padel@canchalibre.test', '$2a$10$99fnPULQCvdbV03CSJbS1Ov08ypvL7dTRC8.BwRs9r/MPWOKUoT..', '8888-0002', 1),
  (3, 2, 6, 'Isaac', 'Monge Barrantes', 'admin.cartago@canchalibre.test', '$2a$10$99fnPULQCvdbV03CSJbS1Ov08ypvL7dTRC8.BwRs9r/MPWOKUoT..', '8888-0003', 1),
  (4, 1, NULL, 'Deportista', 'Demo', 'deportista@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1000', 1),
  (5, 1, NULL, 'Javier', 'Vargas Solano', 'javier@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1001', 1),
  (6, 1, NULL, 'María', 'Torres Quesada', 'maria@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1002', 1),
  (7, 1, NULL, 'Carlos', 'Méndez Rojas', 'carlos@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1003', 1),
  (8, 1, NULL, 'Ana', 'Ramírez Chaves', 'ana@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1004', 1),
  (9, 1, NULL, 'Luis', 'Castillo Mora', 'luis@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1005', 1),
  (10, 1, NULL, 'Sofía', 'Jiménez Araya', 'sofia@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1006', 1),
  (11, 1, NULL, 'Diego', 'Alpízar Vega', 'diego@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1007', 1),
  (12, 1, NULL, 'Valentina', 'Mora Brenes', 'valentina@canchalibre.test', '$2a$10$o6YP3F1a8D8MYHKpy8Dp6uLH1R36GAMnRrQ.qFan91HPCulYZNVBS', '8888-1008', 1);

INSERT INTO espacios (id, complejo_id, disciplina_id, nombre, descripcion, superficie, techada, capacidad, medidas, iluminacion, reglamento, tipo_tarifa, tarifa_diurna, recargo_nocturno, deposito_garantia, imagen_url, activo) VALUES
  (1, 1, 1, 'Cancha de Fútbol 5', 'Cancha de Fútbol 5 en Sport Center Heredia: superficie de césped sintético, techada, capacidad para 10 personas, medidas 40 x 20 m. Ideal para fútbol 5.', 'CESPED_SINTETICO', 1, 10, '40 x 20 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 12000, 3000, 0, '/img/placeholders/futbol-1.svg', 1),
  (2, 1, 1, 'Cancha de Fútbol 5 Exterior', 'Cancha de Fútbol 5 Exterior en Sport Center Heredia: superficie de césped sintético, al aire libre, capacidad para 10 personas, medidas 40 x 20 m. Ideal para fútbol 5.', 'CESPED_SINTETICO', 0, 10, '40 x 20 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 11000, 3000, 0, '/img/placeholders/futbol-3.svg', 1),
  (3, 1, 2, 'Cancha de Fútbol 7', 'Cancha de Fútbol 7 en Sport Center Heredia: superficie de césped sintético, al aire libre, capacidad para 14 personas, medidas 60 x 40 m. Ideal para fútbol 7.', 'CESPED_SINTETICO', 0, 14, '60 x 40 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 18000, 4000, 0, '/img/placeholders/futbol-1.svg', 1),
  (4, 2, 6, 'Cancha de Pádel 1', 'Cancha de Pádel 1 en Padel Zone: superficie de césped sintético, al aire libre, capacidad para 4 personas, medidas 20 x 10 m. Ideal para pádel.', 'CESPED_SINTETICO', 0, 4, '20 x 10 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 10000, 2000, 0, '/img/placeholders/padel-1.svg', 1),
  (5, 2, 6, 'Cancha de Pádel 2', 'Cancha de Pádel 2 en Padel Zone: superficie de césped sintético, techada, capacidad para 4 personas, medidas 20 x 10 m. Ideal para pádel.', 'CESPED_SINTETICO', 1, 4, '20 x 10 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 11000, 2000, 0, '/img/placeholders/padel-2.svg', 1),
  (6, 2, 6, 'Cancha de Pádel 3', 'Cancha de Pádel 3 en Padel Zone: superficie de césped sintético, al aire libre, capacidad para 4 personas, medidas 20 x 10 m. Ideal para pádel.', 'CESPED_SINTETICO', 0, 4, '20 x 10 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 10000, 2000, 0, '/img/placeholders/padel-3.svg', 1),
  (7, 3, 1, 'Cancha de Fútbol 5 A', 'Cancha de Fútbol 5 A en La Canchita SJ: superficie de césped sintético, techada, capacidad para 10 personas, medidas 40 x 20 m. Ideal para fútbol 5.', 'CESPED_SINTETICO', 1, 10, '40 x 20 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 11000, 2500, 0, '/img/placeholders/futbol-1.svg', 1),
  (8, 3, 1, 'Cancha de Fútbol 5 B', 'Cancha de Fútbol 5 B en La Canchita SJ: superficie de césped sintético, al aire libre, capacidad para 10 personas, medidas 40 x 20 m. Ideal para fútbol 5.', 'CESPED_SINTETICO', 0, 10, '40 x 20 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 10500, 2500, 0, '/img/placeholders/futbol-3.svg', 1),
  (9, 4, 8, 'Pista de Atletismo 400 m (tanda individual)', 'Pista de Atletismo 400 m (tanda individual) en Pista de Atletismo Municipal de Heredia: superficie de pista de tartán, al aire libre, capacidad para 30 personas, medidas 400 m. Ideal para atletismo.', 'TARTAN', 0, 30, '400 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_TANDA', 3000, 0, 0, '/img/placeholders/atletismo-2.svg', 1),
  (10, 4, 8, 'Pista de Atletismo (paquete para clubes)', 'Pista de Atletismo (paquete para clubes) en Pista de Atletismo Municipal de Heredia: superficie de pista de tartán, al aire libre, capacidad para 40 personas, medidas 400 m. Ideal para atletismo.', 'TARTAN', 0, 40, '400 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'PAQUETE_GRUPAL', 25000, 0, 0, '/img/placeholders/atletismo-3.svg', 1),
  (11, 5, 9, 'Pista de Karting Principal', 'Pista de Karting Principal en Go-Karts Pista Rápida: superficie de asfalto, al aire libre, capacidad para 8 personas, medidas 600 m. Ideal para karting.', 'ASFALTO', 0, 8, '600 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_TANDA', 18000, 3000, 50000, '/img/placeholders/karting-1.svg', 1),
  (12, 5, 9, 'Pista de Karting - Paquete grupal (8 pilotos)', 'Pista de Karting - Paquete grupal (8 pilotos) en Go-Karts Pista Rápida: superficie de asfalto, al aire libre, capacidad para 8 personas, medidas 600 m. Ideal para karting.', 'ASFALTO', 0, 8, '600 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'PAQUETE_GRUPAL', 120000, 15000, 100000, '/img/placeholders/karting-3.svg', 1),
  (13, 6, 3, 'Cancha de Fútbol 11', 'Cancha de Fútbol 11 en Complejo Deportivo Municipal de Cartago: superficie de césped sintético, al aire libre, capacidad para 22 personas, medidas 100 x 60 m. Ideal para fútbol 11.', 'CESPED_SINTETICO', 0, 22, '100 x 60 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 45000, 8000, 0, '/img/placeholders/futbol-2.svg', 1),
  (14, 6, 7, 'Cancha de Básquetbol', 'Cancha de Básquetbol en Complejo Deportivo Municipal de Cartago: superficie de cemento, techada, capacidad para 10 personas, medidas 28 x 15 m. Ideal para básquetbol.', 'CEMENTO', 1, 10, '28 x 15 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 6000, 1500, 0, '/img/placeholders/basquet-1.svg', 1),
  (15, 6, 4, 'Cancha de Voleibol', 'Cancha de Voleibol en Complejo Deportivo Municipal de Cartago: superficie de cemento, techada, capacidad para 12 personas, medidas 18 x 9 m. Ideal para voleibol.', 'CEMENTO', 1, 12, '18 x 9 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 5000, 1500, 0, '/img/placeholders/voleibol-2.svg', 1),
  (16, 7, 5, 'Cancha de Tenis de Arcilla 1', 'Cancha de Tenis de Arcilla 1 en Raquetas del Valle: superficie de arcilla, al aire libre, capacidad para 4 personas, medidas 23.7 x 10.9 m. Ideal para tenis.', 'ARCILLA', 0, 4, '23.7 x 10.9 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 9000, 2000, 0, '/img/placeholders/tenis-2.svg', 1),
  (17, 7, 5, 'Cancha de Tenis de Arcilla 2', 'Cancha de Tenis de Arcilla 2 en Raquetas del Valle: superficie de arcilla, al aire libre, capacidad para 4 personas, medidas 23.7 x 10.9 m. Ideal para tenis.', 'ARCILLA', 0, 4, '23.7 x 10.9 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 9000, 2000, 0, '/img/placeholders/tenis-3.svg', 1),
  (18, 7, 6, 'Cancha de Pádel Cristal', 'Cancha de Pádel Cristal en Raquetas del Valle: superficie de césped sintético, techada, capacidad para 4 personas, medidas 20 x 10 m. Ideal para pádel.', 'CESPED_SINTETICO', 1, 4, '20 x 10 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 12000, 2500, 0, '/img/placeholders/padel-1.svg', 1),
  (19, 8, 4, 'Cancha de Voleibol de Playa 1', 'Cancha de Voleibol de Playa 1 en Arena Voley Beach Jacó: superficie de arena, al aire libre, capacidad para 8 personas, medidas 16 x 8 m. Ideal para voleibol.', 'ARENA', 0, 8, '16 x 8 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 6000, 2000, 0, '/img/placeholders/voleibol-3.svg', 1),
  (20, 8, 4, 'Cancha de Voleibol de Playa 2', 'Cancha de Voleibol de Playa 2 en Arena Voley Beach Jacó: superficie de arena, al aire libre, capacidad para 8 personas, medidas 16 x 8 m. Ideal para voleibol.', 'ARENA', 0, 8, '16 x 8 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 6000, 2000, 0, '/img/placeholders/voleibol-2.svg', 1),
  (21, 9, 7, 'Cancha de Básquetbol Techada', 'Cancha de Básquetbol Techada en Polideportivo Municipal de Liberia: superficie de cemento, techada, capacidad para 10 personas, medidas 28 x 15 m. Ideal para básquetbol.', 'CEMENTO', 1, 10, '28 x 15 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 5500, 1500, 0, '/img/placeholders/basquet-1.svg', 1),
  (22, 9, 1, 'Cancha de Fútbol 5', 'Cancha de Fútbol 5 en Polideportivo Municipal de Liberia: superficie de cemento, al aire libre, capacidad para 10 personas, medidas 38 x 18 m. Ideal para fútbol 5.', 'CEMENTO', 0, 10, '38 x 18 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 6500, 1500, 0, '/img/placeholders/futbol-2.svg', 1),
  (23, 10, 2, 'Cancha de Fútbol 7', 'Cancha de Fútbol 7 en Club Deportivo Escazú: superficie de césped sintético, al aire libre, capacidad para 14 personas, medidas 55 x 35 m. Ideal para fútbol 7.', 'CESPED_SINTETICO', 0, 14, '55 x 35 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 17000, 3500, 0, '/img/placeholders/futbol-1.svg', 1),
  (24, 10, 5, 'Cancha de Tenis', 'Cancha de Tenis en Club Deportivo Escazú: superficie de cemento, al aire libre, capacidad para 4 personas, medidas 23.7 x 10.9 m. Ideal para tenis.', 'CEMENTO', 0, 4, '23.7 x 10.9 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 8000, 2000, 0, '/img/placeholders/tenis-2.svg', 1),
  (25, 11, 1, 'Cancha de Fútbol 5', 'Cancha de Fútbol 5 en Complejo El Roble: superficie de césped sintético, techada, capacidad para 10 personas, medidas 40 x 20 m. Ideal para fútbol 5.', 'CESPED_SINTETICO', 1, 10, '40 x 20 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 12000, 2500, 0, '/img/placeholders/futbol-1.svg', 1),
  (26, 12, 3, 'Cancha de Fútbol 11', 'Cancha de Fútbol 11 en Estadio Comunal de San Pablo: superficie de césped sintético, al aire libre, capacidad para 22 personas, medidas 100 x 60 m. Ideal para fútbol 11.', 'CESPED_SINTETICO', 0, 22, '100 x 60 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 38000, 7000, 0, '/img/placeholders/futbol-3.svg', 1),
  (27, 12, 8, 'Pista de Atletismo Comunal', 'Pista de Atletismo Comunal en Estadio Comunal de San Pablo: superficie de pista de tartán, al aire libre, capacidad para 30 personas, medidas 300 m. Ideal para atletismo.', 'TARTAN', 0, 30, '300 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_TANDA', 2500, 0, 0, '/img/placeholders/atletismo-2.svg', 1),
  (28, 13, 6, 'Cancha de Pádel A', 'Cancha de Pádel A en Padel Club Santa Ana: superficie de césped sintético, al aire libre, capacidad para 4 personas, medidas 20 x 10 m. Ideal para pádel.', 'CESPED_SINTETICO', 0, 4, '20 x 10 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 10500, 2000, 0, '/img/placeholders/padel-1.svg', 1),
  (29, 13, 6, 'Cancha de Pádel B', 'Cancha de Pádel B en Padel Club Santa Ana: superficie de césped sintético, techada, capacidad para 4 personas, medidas 20 x 10 m. Ideal para pádel.', 'CESPED_SINTETICO', 1, 4, '20 x 10 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 10500, 2000, 0, '/img/placeholders/padel-2.svg', 1),
  (30, 14, 9, 'Pista de Karting Cartago', 'Pista de Karting Cartago en Karting Park Cartago: superficie de asfalto, al aire libre, capacidad para 8 personas, medidas 520 m. Ideal para karting.', 'ASFALTO', 0, 8, '520 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_TANDA', 16000, 2500, 40000, '/img/placeholders/karting-1.svg', 1),
  (31, 15, 7, 'Multicancha - Básquetbol', 'Multicancha - Básquetbol en Multicancha Comunal de Curridabat: superficie de cemento, al aire libre, capacidad para 10 personas, medidas 28 x 15 m. Ideal para básquetbol.', 'CEMENTO', 0, 10, '28 x 15 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 4000, 1000, 0, '/img/placeholders/basquet-1.svg', 1),
  (32, 15, 1, 'Multicancha - Fútbol 5', 'Multicancha - Fútbol 5 en Multicancha Comunal de Curridabat: superficie de asfalto, al aire libre, capacidad para 10 personas, medidas 38 x 18 m. Ideal para fútbol 5.', 'ASFALTO', 0, 10, '38 x 18 m', 1, 'Llegar 10 minutos antes del horario reservado. Uso obligatorio de calzado deportivo adecuado a la superficie. Prohibido el ingreso de bebidas alcohólicas y de fumar. El complejo no se responsabiliza por objetos personales.', 'POR_HORA', 4500, 1000, 0, '/img/placeholders/futbol-2.svg', 1);

INSERT INTO imagenes_espacio (espacio_id, url, orden) VALUES
  (1, '/img/placeholders/futbol-1.svg', 1),
  (1, '/img/placeholders/futbol-2.svg', 2),
  (1, '/img/placeholders/futbol-3.svg', 3),
  (2, '/img/placeholders/futbol-3.svg', 1),
  (2, '/img/placeholders/futbol-1.svg', 2),
  (2, '/img/placeholders/futbol-2.svg', 3),
  (3, '/img/placeholders/futbol-1.svg', 1),
  (3, '/img/placeholders/futbol-2.svg', 2),
  (3, '/img/placeholders/futbol-3.svg', 3),
  (4, '/img/placeholders/padel-1.svg', 1),
  (4, '/img/placeholders/padel-2.svg', 2),
  (4, '/img/placeholders/padel-3.svg', 3),
  (5, '/img/placeholders/padel-2.svg', 1),
  (5, '/img/placeholders/padel-3.svg', 2),
  (5, '/img/placeholders/padel-1.svg', 3),
  (6, '/img/placeholders/padel-3.svg', 1),
  (6, '/img/placeholders/padel-1.svg', 2),
  (6, '/img/placeholders/padel-2.svg', 3),
  (7, '/img/placeholders/futbol-1.svg', 1),
  (7, '/img/placeholders/futbol-2.svg', 2),
  (7, '/img/placeholders/futbol-3.svg', 3),
  (8, '/img/placeholders/futbol-3.svg', 1),
  (8, '/img/placeholders/futbol-1.svg', 2),
  (8, '/img/placeholders/futbol-2.svg', 3),
  (9, '/img/placeholders/atletismo-2.svg', 1),
  (9, '/img/placeholders/atletismo-3.svg', 2),
  (9, '/img/placeholders/atletismo-1.svg', 3),
  (10, '/img/placeholders/atletismo-3.svg', 1),
  (10, '/img/placeholders/atletismo-1.svg', 2),
  (10, '/img/placeholders/atletismo-2.svg', 3),
  (11, '/img/placeholders/karting-1.svg', 1),
  (11, '/img/placeholders/karting-2.svg', 2),
  (11, '/img/placeholders/karting-3.svg', 3),
  (12, '/img/placeholders/karting-3.svg', 1),
  (12, '/img/placeholders/karting-1.svg', 2),
  (12, '/img/placeholders/karting-2.svg', 3),
  (13, '/img/placeholders/futbol-2.svg', 1),
  (13, '/img/placeholders/futbol-3.svg', 2),
  (13, '/img/placeholders/futbol-1.svg', 3),
  (14, '/img/placeholders/basquet-1.svg', 1),
  (14, '/img/placeholders/basquet-2.svg', 2),
  (14, '/img/placeholders/basquet-3.svg', 3),
  (15, '/img/placeholders/voleibol-2.svg', 1),
  (15, '/img/placeholders/voleibol-3.svg', 2),
  (15, '/img/placeholders/voleibol-1.svg', 3),
  (16, '/img/placeholders/tenis-2.svg', 1),
  (16, '/img/placeholders/tenis-3.svg', 2),
  (16, '/img/placeholders/tenis-1.svg', 3),
  (17, '/img/placeholders/tenis-3.svg', 1),
  (17, '/img/placeholders/tenis-1.svg', 2),
  (17, '/img/placeholders/tenis-2.svg', 3),
  (18, '/img/placeholders/padel-1.svg', 1),
  (18, '/img/placeholders/padel-2.svg', 2),
  (18, '/img/placeholders/padel-3.svg', 3),
  (19, '/img/placeholders/voleibol-3.svg', 1),
  (19, '/img/placeholders/voleibol-1.svg', 2),
  (19, '/img/placeholders/voleibol-2.svg', 3),
  (20, '/img/placeholders/voleibol-2.svg', 1),
  (20, '/img/placeholders/voleibol-3.svg', 2),
  (20, '/img/placeholders/voleibol-1.svg', 3),
  (21, '/img/placeholders/basquet-1.svg', 1),
  (21, '/img/placeholders/basquet-2.svg', 2),
  (21, '/img/placeholders/basquet-3.svg', 3),
  (22, '/img/placeholders/futbol-2.svg', 1),
  (22, '/img/placeholders/futbol-3.svg', 2),
  (22, '/img/placeholders/futbol-1.svg', 3),
  (23, '/img/placeholders/futbol-1.svg', 1),
  (23, '/img/placeholders/futbol-2.svg', 2),
  (23, '/img/placeholders/futbol-3.svg', 3),
  (24, '/img/placeholders/tenis-2.svg', 1),
  (24, '/img/placeholders/tenis-3.svg', 2),
  (24, '/img/placeholders/tenis-1.svg', 3),
  (25, '/img/placeholders/futbol-1.svg', 1),
  (25, '/img/placeholders/futbol-2.svg', 2),
  (25, '/img/placeholders/futbol-3.svg', 3),
  (26, '/img/placeholders/futbol-3.svg', 1),
  (26, '/img/placeholders/futbol-1.svg', 2),
  (26, '/img/placeholders/futbol-2.svg', 3),
  (27, '/img/placeholders/atletismo-2.svg', 1),
  (27, '/img/placeholders/atletismo-3.svg', 2),
  (27, '/img/placeholders/atletismo-1.svg', 3),
  (28, '/img/placeholders/padel-1.svg', 1),
  (28, '/img/placeholders/padel-2.svg', 2),
  (28, '/img/placeholders/padel-3.svg', 3),
  (29, '/img/placeholders/padel-2.svg', 1),
  (29, '/img/placeholders/padel-3.svg', 2),
  (29, '/img/placeholders/padel-1.svg', 3),
  (30, '/img/placeholders/karting-1.svg', 1),
  (30, '/img/placeholders/karting-2.svg', 2),
  (30, '/img/placeholders/karting-3.svg', 3),
  (31, '/img/placeholders/basquet-1.svg', 1),
  (31, '/img/placeholders/basquet-2.svg', 2),
  (31, '/img/placeholders/basquet-3.svg', 3),
  (32, '/img/placeholders/futbol-2.svg', 1),
  (32, '/img/placeholders/futbol-3.svg', 2),
  (32, '/img/placeholders/futbol-1.svg', 3);

INSERT INTO insumos (id, complejo_id, nombre, descripcion, categoria, precio, unidad, stock, activo) VALUES
  (1, 1, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (2, 1, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (3, 1, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (4, 1, 'Arco portátil (par)', 'Par de arcos portátiles', 'EQUIPAMIENTO', 3000, 'par', 2, 1),
  (5, 1, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (6, 1, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (7, 1, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (8, 1, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (9, 2, 'Raquetas de pádel (par)', 'Par de raquetas de pádel', 'EQUIPAMIENTO', 2500, 'par', 8, 1),
  (10, 2, 'Bolas de pádel (tubo)', 'Tubo de 3 bolas', 'EQUIPAMIENTO', 2000, 'tubo', 10, 1),
  (11, 2, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (12, 2, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (13, 2, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (14, 3, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (15, 3, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (16, 3, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (17, 3, 'Arco portátil (par)', 'Par de arcos portátiles', 'EQUIPAMIENTO', 3000, 'par', 2, 1),
  (18, 3, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (19, 3, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (20, 3, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (21, 3, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (22, 4, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (23, 4, 'Cronómetro / medición de tiempos', 'Cronometraje de la sesión', 'EQUIPAMIENTO', 2000, 'unidad', 4, 1),
  (24, 4, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (25, 4, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (26, 5, 'Casco de karting', 'Casco integral (obligatorio)', 'EQUIPAMIENTO', 1500, 'unidad', 10, 1),
  (27, 5, 'Guantes y balaclava', 'Kit de protección', 'EQUIPAMIENTO', 1000, 'par', 10, 1),
  (28, 5, 'Cronómetro / medición de tiempos', 'Cronometraje de la sesión', 'EQUIPAMIENTO', 2000, 'unidad', 4, 1),
  (29, 5, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (30, 5, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (31, 5, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (32, 6, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (33, 6, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (34, 6, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (35, 6, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (36, 6, 'Balón de básquetbol', 'Balón oficial tamaño 7', 'EQUIPAMIENTO', 1200, 'unidad', 5, 1),
  (37, 6, 'Balón de voleibol', 'Balón oficial', 'EQUIPAMIENTO', 1200, 'unidad', 5, 1),
  (38, 6, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (39, 6, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (40, 7, 'Raquetas de pádel (par)', 'Par de raquetas de pádel', 'EQUIPAMIENTO', 2500, 'par', 8, 1),
  (41, 7, 'Bolas de pádel (tubo)', 'Tubo de 3 bolas', 'EQUIPAMIENTO', 2000, 'tubo', 10, 1),
  (42, 7, 'Raqueta de tenis', 'Raqueta de tenis adulto', 'EQUIPAMIENTO', 2000, 'unidad', 8, 1),
  (43, 7, 'Bolas de tenis (tubo)', 'Tubo de 3 bolas', 'EQUIPAMIENTO', 1800, 'tubo', 10, 1),
  (44, 7, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (45, 7, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (46, 7, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (47, 8, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (48, 8, 'Balón de voleibol', 'Balón oficial', 'EQUIPAMIENTO', 1200, 'unidad', 5, 1),
  (49, 8, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (50, 8, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (51, 9, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (52, 9, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (53, 9, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (54, 9, 'Arco portátil (par)', 'Par de arcos portátiles', 'EQUIPAMIENTO', 3000, 'par', 2, 1),
  (55, 9, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (56, 9, 'Balón de básquetbol', 'Balón oficial tamaño 7', 'EQUIPAMIENTO', 1200, 'unidad', 5, 1),
  (57, 9, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (58, 9, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (59, 10, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (60, 10, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (61, 10, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (62, 10, 'Arco portátil (par)', 'Par de arcos portátiles', 'EQUIPAMIENTO', 3000, 'par', 2, 1),
  (63, 10, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (64, 10, 'Raqueta de tenis', 'Raqueta de tenis adulto', 'EQUIPAMIENTO', 2000, 'unidad', 8, 1),
  (65, 10, 'Bolas de tenis (tubo)', 'Tubo de 3 bolas', 'EQUIPAMIENTO', 1800, 'tubo', 10, 1),
  (66, 10, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (67, 10, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (68, 10, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (69, 11, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (70, 11, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (71, 11, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (72, 11, 'Arco portátil (par)', 'Par de arcos portátiles', 'EQUIPAMIENTO', 3000, 'par', 2, 1),
  (73, 11, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (74, 11, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (75, 11, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (76, 11, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (77, 12, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (78, 12, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (79, 12, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (80, 12, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (81, 12, 'Cronómetro / medición de tiempos', 'Cronometraje de la sesión', 'EQUIPAMIENTO', 2000, 'unidad', 4, 1),
  (82, 12, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (83, 12, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (84, 13, 'Raquetas de pádel (par)', 'Par de raquetas de pádel', 'EQUIPAMIENTO', 2500, 'par', 8, 1),
  (85, 13, 'Bolas de pádel (tubo)', 'Tubo de 3 bolas', 'EQUIPAMIENTO', 2000, 'tubo', 10, 1),
  (86, 13, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (87, 13, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (88, 13, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (89, 14, 'Casco de karting', 'Casco integral (obligatorio)', 'EQUIPAMIENTO', 1500, 'unidad', 10, 1),
  (90, 14, 'Guantes y balaclava', 'Kit de protección', 'EQUIPAMIENTO', 1000, 'par', 10, 1),
  (91, 14, 'Cronómetro / medición de tiempos', 'Cronometraje de la sesión', 'EQUIPAMIENTO', 2000, 'unidad', 4, 1),
  (92, 14, 'Soda / Salón de eventos', 'Uso del salón y servicio de soda', 'SERVICIO', 5000, 'hora', NULL, 1),
  (93, 14, 'Vestidores', 'Acceso a vestidores y duchas', 'SERVICIO', 1000, 'unidad', NULL, 1),
  (94, 14, 'Parqueo adicional', 'Espacio de parqueo extra', 'SERVICIO', 1000, 'vehículo', 10, 1),
  (95, 15, 'Balón de fútbol', 'Balón oficial tamaño 5', 'EQUIPAMIENTO', 1000, 'unidad', 6, 1),
  (96, 15, 'Chalecos (set 10)', 'Set de 10 chalecos de colores', 'EQUIPAMIENTO', 2000, 'set', 4, 1),
  (97, 15, 'Conos de entrenamiento (set)', 'Set de 10 conos', 'EQUIPAMIENTO', 1500, 'set', 5, 1),
  (98, 15, 'Arco portátil (par)', 'Par de arcos portátiles', 'EQUIPAMIENTO', 3000, 'par', 2, 1),
  (99, 15, 'Silbato de árbitro', 'Silbato profesional', 'EQUIPAMIENTO', 1000, 'unidad', 3, 1),
  (100, 15, 'Balón de básquetbol', 'Balón oficial tamaño 7', 'EQUIPAMIENTO', 1200, 'unidad', 5, 1);

INSERT INTO insumo_disciplinas (insumo_id, disciplina_id) VALUES
  (1, 1),
  (1, 2),
  (2, 1),
  (2, 2),
  (3, 1),
  (3, 2),
  (4, 1),
  (4, 2),
  (5, 1),
  (5, 2),
  (9, 6),
  (10, 6),
  (14, 1),
  (15, 1),
  (16, 1),
  (17, 1),
  (18, 1),
  (22, 8),
  (23, 8),
  (26, 9),
  (27, 9),
  (28, 9),
  (32, 3),
  (33, 3),
  (34, 3),
  (35, 3),
  (35, 4),
  (35, 7),
  (36, 7),
  (37, 4),
  (40, 6),
  (41, 6),
  (42, 5),
  (43, 5),
  (47, 4),
  (48, 4),
  (51, 1),
  (52, 1),
  (53, 1),
  (54, 1),
  (55, 1),
  (55, 7),
  (56, 7),
  (59, 2),
  (60, 2),
  (61, 2),
  (62, 2),
  (63, 2),
  (64, 5),
  (65, 5),
  (69, 1),
  (70, 1),
  (71, 1),
  (72, 1),
  (73, 1),
  (77, 3),
  (78, 3),
  (79, 3),
  (79, 8),
  (80, 3),
  (81, 8),
  (84, 6),
  (85, 6),
  (89, 9),
  (90, 9),
  (91, 9),
  (95, 1),
  (96, 1),
  (97, 1),
  (98, 1),
  (99, 1),
  (99, 7),
  (100, 7);

INSERT INTO bloques_horarios (id, espacio_id, fecha, hora_inicio, hora_fin, estado, expira_en, motivo) VALUES
  (1, 1, @hoy + INTERVAL 1 DAY, '18:00:00', '19:00:00', 'OCUPADO', NULL, NULL),
  (2, 3, @hoy + INTERVAL 1 DAY, '20:00:00', '21:00:00', 'OCUPADO', NULL, NULL),
  (3, 1, @hoy + INTERVAL 2 DAY, '21:00:00', '22:00:00', 'OCUPADO', NULL, NULL),
  (4, 2, @hoy + INTERVAL 2 DAY, '19:00:00', '20:00:00', 'OCUPADO', NULL, NULL),
  (5, 3, @hoy + INTERVAL 3 DAY, '21:00:00', '22:00:00', 'OCUPADO', NULL, NULL),
  (6, 1, @hoy + INTERVAL -1 DAY, '19:00:00', '20:00:00', 'OCUPADO', NULL, NULL),
  (7, 1, @hoy + INTERVAL 4 DAY, '21:00:00', '22:00:00', 'OCUPADO', NULL, NULL),
  (8, 2, @hoy + INTERVAL 6 DAY, '19:00:00', '20:00:00', 'OCUPADO', NULL, NULL),
  (9, 5, @hoy + INTERVAL 3 DAY, '20:00:00', '21:00:00', 'OCUPADO', NULL, NULL),
  (10, 25, @hoy + INTERVAL -4 DAY, '18:00:00', '19:00:00', 'OCUPADO', NULL, NULL),
  (11, 1, @hoy + INTERVAL 1 DAY, '13:00:00', '14:00:00', 'MANTENIMIENTO', NULL, 'Mantenimiento de iluminación'),
  (12, 1, @hoy + INTERVAL 1 DAY, '14:00:00', '15:00:00', 'MANTENIMIENTO', NULL, 'Mantenimiento de iluminación'),
  (13, 2, @hoy + INTERVAL 1 DAY, '07:00:00', '08:00:00', 'MANTENIMIENTO', NULL, 'Corte y riego del césped'),
  (14, 4, @hoy + INTERVAL 2 DAY, '10:00:00', '11:00:00', 'MANTENIMIENTO', NULL, 'Cambio de malla'),
  (15, 3, @hoy + INTERVAL 2 DAY, '15:00:00', '16:00:00', 'MANTENIMIENTO', NULL, 'Demarcación de líneas');

INSERT INTO reservas (id, codigo, usuario_id, espacio_id, bloque_id, fecha, hora_inicio, hora_fin, estado, nocturno, tarifa_diurna, recargo_nocturno, subtotal_pista, subtotal_insumos, impuesto, total, deposito_garantia, expira_en, referencia_bancaria, comprobante_url, comprobante_nombre, comprobante_subido_en, motivo_rechazo, creado_en) VALUES
  (1, 'CLB-2026-FUT-00001', 5, 1, 1, @hoy + INTERVAL 1 DAY, '18:00:00', '19:00:00', 'EN_VERIFICACION', 1, 12000, 3000, 15000, 3500, 2128.32, 18500, 0, NULL, 'BN-784512', '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 4 HOUR, NULL, @ahora - INTERVAL 5 HOUR),
  (2, 'CLB-2026-FUT-00002', 6, 3, 2, @hoy + INTERVAL 1 DAY, '20:00:00', '21:00:00', 'CONFIRMADA', 1, 18000, 4000, 22000, 0, 2530.97, 22000, 0, NULL, NULL, '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 29 HOUR, NULL, @ahora - INTERVAL 30 HOUR),
  (3, 'CLB-2026-FUT-00003', 7, 1, 3, @hoy + INTERVAL 2 DAY, '21:00:00', '22:00:00', 'EN_VERIFICACION', 1, 12000, 3000, 15000, 0, 1725.66, 15000, 0, NULL, 'BN-784990', '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 3 HOUR, NULL, @ahora - INTERVAL 4 HOUR),
  (4, 'CLB-2026-FUT-00004', 8, 2, 4, @hoy + INTERVAL 2 DAY, '19:00:00', '20:00:00', 'CONFIRMADA', 1, 11000, 3000, 14000, 0, 1610.62, 14000, 0, NULL, NULL, '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 39 HOUR, NULL, @ahora - INTERVAL 40 HOUR),
  (5, 'CLB-2026-FUT-00005', 9, 1, NULL, @hoy + INTERVAL 3 DAY, '19:00:00', '20:00:00', 'CANCELADA', 1, 12000, 3000, 15000, 3500, 2128.32, 18500, 0, NULL, NULL, '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 49 HOUR, NULL, @ahora - INTERVAL 50 HOUR),
  (6, 'CLB-2026-FUT-00006', 10, 3, 5, @hoy + INTERVAL 3 DAY, '21:00:00', '22:00:00', 'CONFIRMADA', 1, 18000, 4000, 22000, 0, 2530.97, 22000, 0, NULL, NULL, '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 27 HOUR, NULL, @ahora - INTERVAL 28 HOUR),
  (7, 'CLB-2026-FUT-00007', 11, 1, 6, @hoy + INTERVAL -1 DAY, '19:00:00', '20:00:00', 'FINALIZADA', 1, 12000, 3000, 15000, 1000, 1840.71, 16000, 0, NULL, NULL, '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 89 HOUR, NULL, @ahora - INTERVAL 90 HOUR),
  (8, 'CLB-2026-FUT-00008', 12, 1, 7, @hoy + INTERVAL 4 DAY, '21:00:00', '22:00:00', 'EN_VERIFICACION', 1, 12000, 3000, 15000, 0, 1725.66, 15000, 0, NULL, 'BN-785120', '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 2 HOUR, NULL, @ahora - INTERVAL 3 HOUR),
  (9, 'CLB-2026-FUT-00009', 4, 2, 8, @hoy + INTERVAL 6 DAY, '19:00:00', '20:00:00', 'EN_VERIFICACION', 1, 11000, 3000, 14000, 3000, 1955.75, 17000, 0, NULL, 'BN-785300', '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 1 HOUR, NULL, @ahora - INTERVAL 2 HOUR),
  (10, 'CLB-2026-PAD-00010', 4, 5, 9, @hoy + INTERVAL 3 DAY, '20:00:00', '21:00:00', 'CONFIRMADA', 1, 11000, 2000, 13000, 4500, 2013.27, 17500, 0, NULL, NULL, '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 59 HOUR, NULL, @ahora - INTERVAL 60 HOUR),
  (11, 'CLB-2026-FUT-00011', 4, 7, NULL, @hoy + INTERVAL 5 DAY, '19:00:00', '20:00:00', 'CANCELADA', 1, 11000, 2500, 13500, 0, 1553.10, 13500, 0, NULL, NULL, NULL, NULL, NULL, NULL, @ahora - INTERVAL 70 HOUR),
  (12, 'CLB-2026-FUT-00012', 4, 25, 10, @hoy + INTERVAL -4 DAY, '18:00:00', '19:00:00', 'FINALIZADA', 1, 12000, 2500, 14500, 0, 1668.14, 14500, 0, NULL, NULL, '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 119 HOUR, NULL, @ahora - INTERVAL 120 HOUR),
  (13, 'CLB-2026-FUT-00013', 4, 3, NULL, @hoy + INTERVAL -1 DAY, '18:00:00', '19:00:00', 'EXPIRADA', 1, 18000, 4000, 22000, 0, 2530.97, 22000, 0, NULL, NULL, NULL, NULL, NULL, NULL, @ahora - INTERVAL 55 HOUR),
  (14, 'CLB-2026-PAD-00014', 4, 4, NULL, @hoy + INTERVAL 2 DAY, '19:00:00', '20:00:00', 'RECHAZADA', 1, 10000, 2000, 12000, 0, 1380.53, 12000, 0, NULL, 'BAC-99120', '/img/placeholders/comprobante.svg', 'comprobante_demo.jpg', @ahora - INTERVAL 19 HOUR, 'El monto transferido no coincide con el total de la reserva. Realice la transferencia por el monto exacto y vuelva a reservar.', @ahora - INTERVAL 20 HOUR);

INSERT INTO reserva_insumos (reserva_id, insumo_id, nombre_insumo, cantidad, precio_unitario, subtotal) VALUES
  (1, 1, 'Balón de fútbol', 2, 1000, 2000),
  (1, 3, 'Conos de entrenamiento (set)', 1, 1500, 1500),
  (5, 1, 'Balón de fútbol', 2, 1000, 2000),
  (5, 3, 'Conos de entrenamiento (set)', 1, 1500, 1500),
  (7, 7, 'Vestidores', 1, 1000, 1000),
  (9, 1, 'Balón de fútbol', 1, 1000, 1000),
  (9, 2, 'Chalecos (set 10)', 1, 2000, 2000),
  (10, 9, 'Raquetas de pádel (par)', 1, 2500, 2500),
  (10, 10, 'Bolas de pádel (tubo)', 1, 2000, 2000);

INSERT INTO transacciones (reserva_id, usuario_id, tipo, estado_anterior, estado_nuevo, monto, detalle, creado_en) VALUES
  (1, 5, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 18500, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 5 HOUR),
  (1, 5, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 18500, 'Comprobante de pago recibido', @ahora - INTERVAL 4 HOUR),
  (2, 6, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 22000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 30 HOUR),
  (2, 6, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 22000, 'Comprobante de pago recibido', @ahora - INTERVAL 29 HOUR),
  (2, 1, 'PAGO_APROBADO', 'EN_VERIFICACION', 'CONFIRMADA', 22000, 'Pago validado por el administrador', @ahora - INTERVAL 28 HOUR),
  (3, 7, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 15000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 4 HOUR),
  (3, 7, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 15000, 'Comprobante de pago recibido', @ahora - INTERVAL 3 HOUR),
  (4, 8, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 14000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 40 HOUR),
  (4, 8, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 14000, 'Comprobante de pago recibido', @ahora - INTERVAL 39 HOUR),
  (4, 1, 'PAGO_APROBADO', 'EN_VERIFICACION', 'CONFIRMADA', 14000, 'Pago validado por el administrador', @ahora - INTERVAL 38 HOUR),
  (5, 9, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 18500, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 50 HOUR),
  (5, 9, 'CANCELACION', 'PENDIENTE_PAGO', 'CANCELADA', 18500, 'Cancelada por el usuario', @ahora - INTERVAL 49 HOUR),
  (6, 10, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 22000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 28 HOUR),
  (6, 10, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 22000, 'Comprobante de pago recibido', @ahora - INTERVAL 27 HOUR),
  (6, 1, 'PAGO_APROBADO', 'EN_VERIFICACION', 'CONFIRMADA', 22000, 'Pago validado por el administrador', @ahora - INTERVAL 26 HOUR),
  (7, 11, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 16000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 90 HOUR),
  (7, 11, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 16000, 'Comprobante de pago recibido', @ahora - INTERVAL 89 HOUR),
  (7, 1, 'PAGO_APROBADO', 'EN_VERIFICACION', 'CONFIRMADA', 16000, 'Pago validado por el administrador', @ahora - INTERVAL 88 HOUR),
  (7, NULL, 'FINALIZACION', 'CONFIRMADA', 'FINALIZADA', 16000, 'Actividad finalizada', @ahora - INTERVAL 87 HOUR),
  (8, 12, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 15000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 3 HOUR),
  (8, 12, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 15000, 'Comprobante de pago recibido', @ahora - INTERVAL 2 HOUR),
  (9, 4, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 17000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 2 HOUR),
  (9, 4, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 17000, 'Comprobante de pago recibido', @ahora - INTERVAL 1 HOUR),
  (10, 4, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 17500, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 60 HOUR),
  (10, 4, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 17500, 'Comprobante de pago recibido', @ahora - INTERVAL 59 HOUR),
  (10, 2, 'PAGO_APROBADO', 'EN_VERIFICACION', 'CONFIRMADA', 17500, 'Pago validado por el administrador', @ahora - INTERVAL 58 HOUR),
  (11, 4, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 13500, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 70 HOUR),
  (11, 4, 'CANCELACION', 'PENDIENTE_PAGO', 'CANCELADA', 13500, 'Cancelada por el usuario', @ahora - INTERVAL 69 HOUR),
  (12, 4, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 14500, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 120 HOUR),
  (12, 4, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 14500, 'Comprobante de pago recibido', @ahora - INTERVAL 119 HOUR),
  (12, NULL, 'PAGO_APROBADO', 'EN_VERIFICACION', 'CONFIRMADA', 14500, 'Pago validado por el administrador', @ahora - INTERVAL 118 HOUR),
  (12, NULL, 'FINALIZACION', 'CONFIRMADA', 'FINALIZADA', 14500, 'Actividad finalizada', @ahora - INTERVAL 117 HOUR),
  (13, 4, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 22000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 55 HOUR),
  (13, NULL, 'EXPIRACION', 'PENDIENTE_PAGO', 'EXPIRADA', 0, 'Venció el tiempo límite de pago', @ahora - INTERVAL 54 HOUR),
  (14, 4, 'RESERVA_CREADA', NULL, 'PENDIENTE_PAGO', 12000, 'Reserva creada; bloque apartado por 15 minutos', @ahora - INTERVAL 20 HOUR),
  (14, 4, 'PAGO_REGISTRADO', 'PENDIENTE_PAGO', 'EN_VERIFICACION', 12000, 'Comprobante de pago recibido', @ahora - INTERVAL 19 HOUR),
  (14, 2, 'PAGO_RECHAZADO', 'EN_VERIFICACION', 'RECHAZADA', 12000, 'El monto transferido no coincide con el total de la reserva. Realice la transferencia por el monto exacto y vuelva a reservar.', @ahora - INTERVAL 18 HOUR);

INSERT INTO notificaciones (usuario_id, reserva_id, titulo, mensaje, leida, creado_en) VALUES
  (4, 14, 'Reserva rechazada', 'Su reserva CLB-2026-PAD-00014 fue rechazada: El monto transferido no coincide con el total de la reserva. Realice la transferencia por el monto exacto y vuelva a reservar.', 0, @ahora - INTERVAL 18 HOUR),
  (4, 10, 'Pago confirmado', 'Su pago fue validado. La reserva CLB-2026-PAD-00010 está confirmada.', 1, @ahora - INTERVAL 48 HOUR);
