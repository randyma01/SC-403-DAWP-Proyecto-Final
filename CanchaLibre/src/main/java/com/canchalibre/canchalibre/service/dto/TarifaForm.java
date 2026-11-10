package com.canchalibre.canchalibre.dto;

import com.canchalibre.canchalibre.domain.TipoTarifa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Edicion rapida de tarifas de un espacio (HU-08). */
public class TarifaForm {

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.01", message = "{validacion.positivo}")
    private BigDecimal tarifaDiurna;

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.00", message = "{validacion.nonegativo}")
    private BigDecimal recargoNocturno;

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.00", message = "{validacion.nonegativo}")
    private BigDecimal depositoGarantia;

    @NotNull(message = "{validacion.requerido}")
    private TipoTarifa tipoTarifa;

    // ---- generado: getters y setters ----
    public BigDecimal getTarifaDiurna() {
        return tarifaDiurna;
    }

    public void setTarifaDiurna(BigDecimal tarifaDiurna) {
        this.tarifaDiurna = tarifaDiurna;
    }

    public BigDecimal getRecargoNocturno() {
        return recargoNocturno;
    }

    public void setRecargoNocturno(BigDecimal recargoNocturno) {
        this.recargoNocturno = recargoNocturno;
    }

    public BigDecimal getDepositoGarantia() {
        return depositoGarantia;
    }

    public void setDepositoGarantia(BigDecimal depositoGarantia) {
        this.depositoGarantia = depositoGarantia;
    }

    public TipoTarifa getTipoTarifa() {
        return tipoTarifa;
    }

    public void setTipoTarifa(TipoTarifa tipoTarifa) {
        this.tipoTarifa = tipoTarifa;
    }

}
