package com.canchalibre.canchalibre.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "disciplinas")
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 5)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String icono;

    @Column(name = "imagen_base", nullable = false, length = 30)
    private String imagenBase;

    private int orden;

    // ---- generado: getters y setters ----
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getImagenBase() {
        return imagenBase;
    }

    public void setImagenBase(String imagenBase) {
        this.imagenBase = imagenBase;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Disciplina d && id != null && Objects.equals(id, d.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
