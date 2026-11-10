package com.canchalibre.canchalibre.dto;

import com.canchalibre.canchalibre.domain.Insumo;

/** Insumo con las unidades realmente disponibles para la fecha/hora elegida (null = ilimitado). */
public class InsumoVista {

    private final Insumo insumo;
    private final Integer disponible;

    // ---- generado: constructor y getters ----
    public InsumoVista(Insumo insumo, Integer disponible) {
        this.insumo = insumo;
        this.disponible = disponible;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public Integer getDisponible() {
        return disponible;
    }


    public boolean isAgotado() {
        return disponible != null && disponible <= 0;
    }
}
