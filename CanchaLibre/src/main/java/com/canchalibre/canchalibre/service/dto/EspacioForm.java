package com.canchalibre.canchalibre.dto;

import com.canchalibre.canchalibre.domain.Superficie;
import com.canchalibre.canchalibre.domain.TipoTarifa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

public class EspacioForm {

    private Long id;

    @NotNull(message = "{validacion.requerido}")
    private Integer disciplinaId;

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 120, message = "{validacion.max}")
    private String nombre;

    private String descripcion;

    @NotNull(message = "{validacion.requerido}")
    private Superficie superficie = Superficie.CESPED_SINTETICO;

    private boolean techada;

    @NotNull(message = "{validacion.requerido}")
    @Min(value = 1, message = "{validacion.positivo}")
    private Integer capacidad = 10;

    @Size(max = 40, message = "{validacion.max}")
    private String medidas;

    private boolean iluminacion = true;

    private String reglamento;

    @NotNull(message = "{validacion.requerido}")
    private TipoTarifa tipoTarifa = TipoTarifa.POR_HORA;

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.01", message = "{validacion.positivo}")
    private BigDecimal tarifaDiurna;

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.00", message = "{validacion.nonegativo}")
    private BigDecimal recargoNocturno = BigDecimal.ZERO;

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.00", message = "{validacion.nonegativo}")
    private BigDecimal depositoGarantia = BigDecimal.ZERO;

    private boolean activo = true;

    /** Imagen opcional que se agrega a la galeria (se sube a Firebase Storage). */
    private MultipartFile imagen;

    // ---- generado: getters y setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(Integer disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Superficie getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Superficie superficie) {
        this.superficie = superficie;
    }

    public boolean isTechada() {
        return techada;
    }

    public void setTechada(boolean techada) {
        this.techada = techada;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getMedidas() {
        return medidas;
    }

    public void setMedidas(String medidas) {
        this.medidas = medidas;
    }

    public boolean isIluminacion() {
        return iluminacion;
    }

    public void setIluminacion(boolean iluminacion) {
        this.iluminacion = iluminacion;
    }

    public String getReglamento() {
        return reglamento;
    }

    public void setReglamento(String reglamento) {
        this.reglamento = reglamento;
    }

    public TipoTarifa getTipoTarifa() {
        return tipoTarifa;
    }

    public void setTipoTarifa(TipoTarifa tipoTarifa) {
        this.tipoTarifa = tipoTarifa;
    }

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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public MultipartFile getImagen() {
        return imagen;
    }

    public void setImagen(MultipartFile imagen) {
        this.imagen = imagen;
    }

}
