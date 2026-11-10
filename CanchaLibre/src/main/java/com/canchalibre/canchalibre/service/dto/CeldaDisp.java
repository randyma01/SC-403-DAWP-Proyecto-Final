package com.canchalibre.canchalibre.dto;

/** Celda de la cuadricula de disponibilidad. estado: DISPONIBLE, OCUPADO, APARTADO, MANTENIMIENTO o PASADO. */
public class CeldaDisp {

    private final Long espacioId;
    private final String estado;
    private final boolean seleccionable;

    // ---- generado: constructor y getters ----
    public CeldaDisp(Long espacioId, String estado, boolean seleccionable) {
        this.espacioId = espacioId;
        this.estado = estado;
        this.seleccionable = seleccionable;
    }

    public Long getEspacioId() {
        return espacioId;
    }

    public String getEstado() {
        return estado;
    }

    public boolean isSeleccionable() {
        return seleccionable;
    }

}
