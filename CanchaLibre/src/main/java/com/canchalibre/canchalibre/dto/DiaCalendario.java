package com.canchalibre.canchalibre.dto;

import java.time.LocalDate;

/** Dia del mini calendario mensual. fecha == null indica una celda vacia de relleno. */
public class DiaCalendario {

    private final LocalDate fecha;
    private final int dia;
    private final boolean deshabilitado;
    private final boolean seleccionado;
    private final boolean hoy;

    // ---- generado: constructor y getters ----
    public DiaCalendario(LocalDate fecha, int dia, boolean deshabilitado, boolean seleccionado, boolean hoy) {
        this.fecha = fecha;
        this.dia = dia;
        this.deshabilitado = deshabilitado;
        this.seleccionado = seleccionado;
        this.hoy = hoy;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getDia() {
        return dia;
    }

    public boolean isDeshabilitado() {
        return deshabilitado;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public boolean isHoy() {
        return hoy;
    }

}
