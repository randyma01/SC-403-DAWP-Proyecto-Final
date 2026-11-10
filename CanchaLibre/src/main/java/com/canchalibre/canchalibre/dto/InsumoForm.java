package com.canchalibre.canchalibre.dto;

import com.canchalibre.canchalibre.domain.CategoriaInsumo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InsumoForm {

    private Long id;

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 100, message = "{validacion.max}")
    private String nombre;

    @Size(max = 255, message = "{validacion.max}")
    private String descripcion;

    @NotNull(message = "{validacion.requerido}")
    private CategoriaInsumo categoria = CategoriaInsumo.EQUIPAMIENTO;

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.01", message = "{validacion.positivo}")
    private BigDecimal precio;

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 20, message = "{validacion.max}")
    private String unidad = "unidad";

    /** Vacio = inventario ilimitado. */
    @Min(value = 0, message = "{validacion.nonegativo}")
    private Integer stock;

    private boolean activo = true;

    /** Disciplinas a las que aplica; vacio = todas. */
    private List<Integer> disciplinaIds = new ArrayList<>();

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

    public CategoriaInsumo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaInsumo categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Integer> getDisciplinaIds() {
        return disciplinaIds;
    }

    public void setDisciplinaIds(List<Integer> disciplinaIds) {
        this.disciplinaIds = disciplinaIds;
    }

}
