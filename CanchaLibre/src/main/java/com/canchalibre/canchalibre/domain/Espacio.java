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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pista o cancha reservable dentro de un complejo. Las tarifas incluyen IVA. */
@Entity
@Table(name = "espacios")
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "complejo_id")
    private Complejo complejo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Superficie superficie;

    private boolean techada;

    private int capacidad = 10;

    @Column(length = 40)
    private String medidas;

    private boolean iluminacion = true;

    @Column(columnDefinition = "TEXT")
    private String reglamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarifa", nullable = false, length = 20)
    private TipoTarifa tipoTarifa = TipoTarifa.POR_HORA;

    @Column(name = "tarifa_diurna", nullable = false)
    private BigDecimal tarifaDiurna;

    @Column(name = "recargo_nocturno", nullable = false)
    private BigDecimal recargoNocturno = BigDecimal.ZERO;

    @Column(name = "deposito_garantia", nullable = false)
    private BigDecimal depositoGarantia = BigDecimal.ZERO;

    @Column(name = "imagen_url", length = 600)
    private String imagenUrl;

    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "espacio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ImagenEspacio> imagenes = new ArrayList<>();

    // ---- generado: getters y setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complejo getComplejo() {
        return complejo;
    }

    public void setComplejo(Complejo complejo) {
        this.complejo = complejo;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
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

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public List<ImagenEspacio> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenEspacio> imagenes) {
        this.imagenes = imagenes;
    }


    /** Tarifa nocturna = tarifa diurna + recargo por iluminacion. */
    public BigDecimal getTarifaNocturna() {
        return tarifaDiurna.add(recargoNocturno);
    }

    public boolean isRequiereDeposito() {
        return depositoGarantia != null && depositoGarantia.signum() > 0;
    }
}
