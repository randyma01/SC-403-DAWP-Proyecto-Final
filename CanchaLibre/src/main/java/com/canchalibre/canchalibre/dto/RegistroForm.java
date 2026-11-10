package com.canchalibre.canchalibre.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroForm {

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 80, message = "{validacion.max}")
    private String nombre;

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 100, message = "{validacion.max}")
    private String apellidos;

    @NotBlank(message = "{validacion.requerido}")
    @Email(message = "{validacion.email}")
    @Size(max = 150, message = "{validacion.max}")
    private String email;

    @Size(max = 20, message = "{validacion.max}")
    private String telefono;

    @NotBlank(message = "{validacion.requerido}")
    @Size(min = 8, max = 60, message = "{validacion.password}")
    private String password;

    @NotBlank(message = "{validacion.requerido}")
    private String confirmarPassword;

    // ---- generado: getters y setters ----
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }

}
