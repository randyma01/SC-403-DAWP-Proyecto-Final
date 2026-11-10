package com.canchalibre.canchalibre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

/** Bloqueo operativo (mantenimiento) de un rango de horas de un espacio. */
public class BloqueoForm {

    @NotNull(message = "{validacion.requerido}")
    private Long espacioId;

    @NotNull(message = "{validacion.requerido}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "{validacion.requerido}")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaDesde;

    @NotNull(message = "{validacion.requerido}")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaHasta;

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 200, message = "{validacion.max}")
    private String motivo;

    // ---- generado: getters y setters ----
    public Long getEspacioId() {
        return espacioId;
    }

    public void setEspacioId(Long espacioId) {
        this.espacioId = espacioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(LocalTime horaDesde) {
        this.horaDesde = horaDesde;
    }

    public LocalTime getHoraHasta() {
        return horaHasta;
    }

    public void setHoraHasta(LocalTime horaHasta) {
        this.horaHasta = horaHasta;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

}
