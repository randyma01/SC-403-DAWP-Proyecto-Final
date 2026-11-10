package com.canchalibre.canchalibre.dto;

import java.math.BigDecimal;
import java.util.List;

/** Desglose de costos de una reserva (HU-05 y HU-07). Los precios del catalogo incluyen IVA. */
public class Cotizacion {

    private final boolean nocturno;
    private final BigDecimal tarifaDiurna;
    private final BigDecimal recargoNocturno;
    private final BigDecimal subtotalPista;
    private final List<LineaCotizacion> lineas;
    private final BigDecimal subtotalInsumos;
    private final BigDecimal porcentajeIva;
    private final BigDecimal impuesto;
    private final BigDecimal total;
    private final BigDecimal depositoGarantia;

    // ---- generado: constructor y getters ----
    public Cotizacion(boolean nocturno, BigDecimal tarifaDiurna, BigDecimal recargoNocturno, BigDecimal subtotalPista, List<LineaCotizacion> lineas, BigDecimal subtotalInsumos, BigDecimal porcentajeIva, BigDecimal impuesto, BigDecimal total, BigDecimal depositoGarantia) {
        this.nocturno = nocturno;
        this.tarifaDiurna = tarifaDiurna;
        this.recargoNocturno = recargoNocturno;
        this.subtotalPista = subtotalPista;
        this.lineas = lineas;
        this.subtotalInsumos = subtotalInsumos;
        this.porcentajeIva = porcentajeIva;
        this.impuesto = impuesto;
        this.total = total;
        this.depositoGarantia = depositoGarantia;
    }

    public boolean isNocturno() {
        return nocturno;
    }

    public BigDecimal getTarifaDiurna() {
        return tarifaDiurna;
    }

    public BigDecimal getRecargoNocturno() {
        return recargoNocturno;
    }

    public BigDecimal getSubtotalPista() {
        return subtotalPista;
    }

    public List<LineaCotizacion> getLineas() {
        return lineas;
    }

    public BigDecimal getSubtotalInsumos() {
        return subtotalInsumos;
    }

    public BigDecimal getPorcentajeIva() {
        return porcentajeIva;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getDepositoGarantia() {
        return depositoGarantia;
    }


    public boolean isRequiereDeposito() {
        return depositoGarantia != null && depositoGarantia.signum() > 0;
    }
}
