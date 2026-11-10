package com.canchalibre.canchalibre.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Instalacion deportiva (publica o privada) que agrupa varios espacios. */
@Entity
@Table(name = "complejos")
public class Complejo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 40)
    private String provincia;

    @Column(nullable = false, length = 60)
    private String canton;

    @Column(length = 255)
    private String direccion;

    private BigDecimal latitud;

    private BigDecimal longitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_administracion", nullable = false, length = 10)
    private TipoAdministracion tipoAdministracion;

    private boolean destacado;

    private boolean activo = true;

    @Column(name = "imagen_url", length = 600)
    private String imagenUrl;

    @Column(nullable = false)
    private BigDecimal calificacion = BigDecimal.ZERO;

    @Column(name = "num_resenas", nullable = false)
    private int numResenas;

    @Column(length = 20)
    private String telefono;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura = LocalTime.of(6, 0);

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre = LocalTime.of(23, 0);

    @Column(name = "hora_inicio_nocturno", nullable = false)
    private LocalTime horaInicioNocturno = LocalTime.of(18, 0);

    @Column(name = "porcentaje_iva", nullable = false)
    private BigDecimal porcentajeIva = new BigDecimal("13.00");

    private boolean parqueo;

    private boolean vestidores;

    private boolean soda;

    @Column(name = "iluminacion_nocturna")
    private boolean iluminacionNocturna;

    @Column(length = 80)
    private String banco;

    @Column(length = 120)
    private String titular;

    @Column(length = 40)
    private String iban;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    // ---- generado: getters y setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCanton() {
        return canton;
    }

    public void setCanton(String canton) {
        this.canton = canton;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public TipoAdministracion getTipoAdministracion() {
        return tipoAdministracion;
    }

    public void setTipoAdministracion(TipoAdministracion tipoAdministracion) {
        this.tipoAdministracion = tipoAdministracion;
    }

    public boolean isDestacado() {
        return destacado;
    }

    public void setDestacado(boolean destacado) {
        this.destacado = destacado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public BigDecimal getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(BigDecimal calificacion) {
        this.calificacion = calificacion;
    }

    public int getNumResenas() {
        return numResenas;
    }

    public void setNumResenas(int numResenas) {
        this.numResenas = numResenas;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public LocalTime getHoraInicioNocturno() {
        return horaInicioNocturno;
    }

    public void setHoraInicioNocturno(LocalTime horaInicioNocturno) {
        this.horaInicioNocturno = horaInicioNocturno;
    }

    public BigDecimal getPorcentajeIva() {
        return porcentajeIva;
    }

    public void setPorcentajeIva(BigDecimal porcentajeIva) {
        this.porcentajeIva = porcentajeIva;
    }

    public boolean isParqueo() {
        return parqueo;
    }

    public void setParqueo(boolean parqueo) {
        this.parqueo = parqueo;
    }

    public boolean isVestidores() {
        return vestidores;
    }

    public void setVestidores(boolean vestidores) {
        this.vestidores = vestidores;
    }

    public boolean isSoda() {
        return soda;
    }

    public void setSoda(boolean soda) {
        this.soda = soda;
    }

    public boolean isIluminacionNocturna() {
        return iluminacionNocturna;
    }

    public void setIluminacionNocturna(boolean iluminacionNocturna) {
        this.iluminacionNocturna = iluminacionNocturna;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

}
