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
