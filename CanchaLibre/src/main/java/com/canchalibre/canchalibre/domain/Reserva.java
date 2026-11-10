package com.canchalibre.canchalibre.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Reserva de un bloque horario. Guarda una "foto" de precios vigentes al momento de reservar (HU-08). */
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "espacio_id")
    private Espacio espacio;

    /** Bloque retenido; null cuando la reserva libero el bloque (cancelada, expirada o rechazada). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloque_id")
    private BloqueHorario bloque;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE_PAGO;

    private boolean nocturno;

    @Column(name = "tarifa_diurna", nullable = false)
    private BigDecimal tarifaDiurna;

    @Column(name = "recargo_nocturno", nullable = false)
    private BigDecimal recargoNocturno = BigDecimal.ZERO;

    @Column(name = "subtotal_pista", nullable = false)
    private BigDecimal subtotalPista;

    @Column(name = "subtotal_insumos", nullable = false)
    private BigDecimal subtotalInsumos = BigDecimal.ZERO;

    /** IVA contenido en el total (los precios del catalogo ya lo incluyen). */
    @Column(nullable = false)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "deposito_garantia", nullable = false)
    private BigDecimal depositoGarantia = BigDecimal.ZERO;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @Column(name = "referencia_bancaria", length = 60)
    private String referenciaBancaria;

    @Column(name = "comprobante_url", length = 700)
    private String comprobanteUrl;

    @Column(name = "comprobante_nombre", length = 200)
    private String comprobanteNombre;

    @Column(name = "comprobante_subido_en")
    private LocalDateTime comprobanteSubidoEn;

    @Column(name = "motivo_rechazo", length = 300)
    private String motivoRechazo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaInsumo> insumos = new ArrayList<>();

    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        if (creadoEn == null) {
            creadoEn = ahora;
        }
        actualizadoEn = ahora;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = LocalDateTime.now();
    }

    // ---- generado: getters y setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Espacio getEspacio() {
        return espacio;
    }

    public void setEspacio(Espacio espacio) {
        this.espacio = espacio;
    }

    public BloqueHorario getBloque() {
        return bloque;
    }

    public void setBloque(BloqueHorario bloque) {
        this.bloque = bloque;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public boolean isNocturno() {
        return nocturno;
    }

    public void setNocturno(boolean nocturno) {
        this.nocturno = nocturno;
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

    public BigDecimal getSubtotalPista() {
        return subtotalPista;
    }

    public void setSubtotalPista(BigDecimal subtotalPista) {
        this.subtotalPista = subtotalPista;
    }

    public BigDecimal getSubtotalInsumos() {
        return subtotalInsumos;
    }

    public void setSubtotalInsumos(BigDecimal subtotalInsumos) {
        this.subtotalInsumos = subtotalInsumos;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getDepositoGarantia() {
        return depositoGarantia;
    }

    public void setDepositoGarantia(BigDecimal depositoGarantia) {
        this.depositoGarantia = depositoGarantia;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(LocalDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public String getReferenciaBancaria() {
        return referenciaBancaria;
    }

    public void setReferenciaBancaria(String referenciaBancaria) {
        this.referenciaBancaria = referenciaBancaria;
    }

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }

    public String getComprobanteNombre() {
        return comprobanteNombre;
    }

    public void setComprobanteNombre(String comprobanteNombre) {
        this.comprobanteNombre = comprobanteNombre;
    }

    public LocalDateTime getComprobanteSubidoEn() {
        return comprobanteSubidoEn;
    }

    public void setComprobanteSubidoEn(LocalDateTime comprobanteSubidoEn) {
        this.comprobanteSubidoEn = comprobanteSubidoEn;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public List<ReservaInsumo> getInsumos() {
        return insumos;
    }

    public void setInsumos(List<ReservaInsumo> insumos) {
        this.insumos = insumos;
    }


    public boolean isPendiente() {
        return estado.isPendiente();
    }

    public boolean isCancelable() {
        return estado.isCancelable();
    }

    public boolean isTieneComprobante() {
        return comprobanteUrl != null && !comprobanteUrl.isBlank();
    }

    /** true si el comprobante es un PDF (para mostrar enlace en vez de miniatura). */
    public boolean isComprobanteEsPdf() {
        return comprobanteNombre != null && comprobanteNombre.toLowerCase().endsWith(".pdf");
    }

    /** Milisegundos epoch en los que vence la retencion, o 0 si no hay temporizador activo. */
    public long getExpiraEnMillis() {
        if (expiraEn == null) {
            return 0L;
        }
        return expiraEn.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
