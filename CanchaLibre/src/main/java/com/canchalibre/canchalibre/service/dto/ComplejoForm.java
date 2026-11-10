package com.canchalibre.canchalibre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public class ComplejoForm {

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 120, message = "{validacion.max}")
    private String nombre;

    private String descripcion;

    @Size(max = 255, message = "{validacion.max}")
    private String direccion;

    @Size(max = 20, message = "{validacion.max}")
    private String telefono;

    @NotNull(message = "{validacion.requerido}")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaApertura;

    @NotNull(message = "{validacion.requerido}")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaCierre;

    @NotNull(message = "{validacion.requerido}")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicioNocturno;

    private boolean parqueo;

    private boolean vestidores;

    private boolean soda;

    private boolean iluminacionNocturna;

    @Size(max = 80, message = "{validacion.max}")
    private String banco;

    @Size(max = 120, message = "{validacion.max}")
    private String titular;

    @Size(max = 40, message = "{validacion.max}")
    private String iban;

    // ---- generado: getters y setters ----
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

}
