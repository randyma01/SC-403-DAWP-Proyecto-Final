package com.canchalibre.canchalibre.domain;

import java.util.List;

/** Ciclo de vida de una reserva (HU-09 a HU-15). */
public enum EstadoReserva {
    PENDIENTE_PAGO("pendiente"),
    EN_VERIFICACION("pendiente"),
    CONFIRMADA("confirmada"),
    RECHAZADA("rechazada"),
    EXPIRADA("gris"),
    CANCELADA("gris"),
    FINALIZADA("finalizada");

    /** Estados que cuentan para el inventario de insumos y para el calendario. */
    public static final List<EstadoReserva> ACTIVAS = List.of(PENDIENTE_PAGO, EN_VERIFICACION, CONFIRMADA);

    private final String css;

    EstadoReserva(String css) {
        this.css = css;
    }

    /** Sufijo de la clase CSS del badge: estado-{css}. */
    public String getCss() {
        return css;
    }

    /** Una reserva pendiente puede descartarse voluntariamente (HU-12). */
    public boolean isCancelable() {
        return this == PENDIENTE_PAGO || this == EN_VERIFICACION;
    }

    public boolean isPendiente() {
        return this == PENDIENTE_PAGO || this == EN_VERIFICACION;
    }
}
