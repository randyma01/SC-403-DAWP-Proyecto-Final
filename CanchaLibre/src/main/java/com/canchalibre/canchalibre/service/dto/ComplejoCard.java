package com.canchalibre.canchalibre.dto;

import com.canchalibre.canchalibre.domain.Complejo;
import java.math.BigDecimal;
import java.util.List;

/** Tarjeta del catalogo (HU-01): complejo + disciplinas que alberga + precio "desde". */
public class ComplejoCard {

    private final Complejo complejo;
    private final List<String> disciplinas;
    private final BigDecimal precioDesde;
    private final String imagenUrl;

    // ---- generado: constructor y getters ----
    public ComplejoCard(Complejo complejo, List<String> disciplinas, BigDecimal precioDesde, String imagenUrl) {
        this.complejo = complejo;
        this.disciplinas = disciplinas;
        this.precioDesde = precioDesde;
        this.imagenUrl = imagenUrl;
    }

    public Complejo getComplejo() {
        return complejo;
    }

    public List<String> getDisciplinas() {
        return disciplinas;
    }

    public BigDecimal getPrecioDesde() {
        return precioDesde;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

}
