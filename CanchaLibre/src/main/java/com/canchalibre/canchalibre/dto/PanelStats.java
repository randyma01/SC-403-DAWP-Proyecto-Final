package com.canchalibre.canchalibre.dto;

public class PanelStats {

    private final long reservasHoy;
    private final long pendientes;
    private final long confirmadas;
    private final long canceladas;

    // ---- generado: constructor y getters ----
    public PanelStats(long reservasHoy, long pendientes, long confirmadas, long canceladas) {
        this.reservasHoy = reservasHoy;
        this.pendientes = pendientes;
        this.confirmadas = confirmadas;
        this.canceladas = canceladas;
    }

    public long getReservasHoy() {
        return reservasHoy;
    }

    public long getPendientes() {
        return pendientes;
    }

    public long getConfirmadas() {
        return confirmadas;
    }

    public long getCanceladas() {
        return canceladas;
    }

}
