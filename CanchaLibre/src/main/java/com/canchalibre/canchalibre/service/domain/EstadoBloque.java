package com.canchalibre.canchalibre.domain;

/** Estado persistido de un bloque horario. Un bloque sin fila en la tabla se considera DISPONIBLE. */
public enum EstadoBloque {
    DISPONIBLE, APARTADO_TEMPORAL, OCUPADO, MANTENIMIENTO
}
