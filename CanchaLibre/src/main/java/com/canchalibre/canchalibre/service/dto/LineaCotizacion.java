package com.canchalibre.canchalibre.dto;

import com.canchalibre.canchalibre.domain.Insumo;
import java.math.BigDecimal;

public class LineaCotizacion {

    private final Insumo insumo;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    // ---- generado: constructor y getters ----
    public LineaCotizacion(Insumo insumo, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

}
