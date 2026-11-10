package com.canchalibre.canchalibre.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/** Formateo de montos, fechas y horas para las vistas Thymeleaf: ${@fmt.moneda(x)}. */
@Component("fmt")
public class Formato {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public String moneda(BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));
        return "₡" + df.format(monto);
    }

    /** IVA contenido en un precio que ya lo incluye, formateado como moneda. */
    public String ivaIncluido(BigDecimal monto, BigDecimal porcentaje) {
        if (monto == null || porcentaje == null) {
            return "";
        }
        return moneda(PrecioCalculator.ivaIncluido(monto, porcentaje));
    }

    public String hora(LocalTime hora) {
        return hora == null ? "" : hora.format(HORA);
    }

    public String rango(LocalTime inicio, LocalTime fin) {
        return hora(inicio) + " - " + hora(fin);
    }

    /** Ej.: "Sábado, 18 de octubre de 2025". */
    public String fechaLarga(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        Locale loc = LocaleContextHolder.getLocale();
        String patron = "en".equals(loc.getLanguage()) ? "EEEE, MMMM d, yyyy" : "EEEE, d 'de' MMMM 'de' yyyy";
        return capitalizar(fecha.format(DateTimeFormatter.ofPattern(patron, loc)));
    }

    /** Ej.: "18 oct 2025". */
    public String fechaCorta(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        Locale loc = LocaleContextHolder.getLocale();
        String patron = "en".equals(loc.getLanguage()) ? "MMM d, yyyy" : "d MMM yyyy";
        return fecha.format(DateTimeFormatter.ofPattern(patron, loc)).replace(".", "");
    }

    /** Ej.: "Mes de octubre 2025" para el mini calendario. */
    public String mesAnio(LocalDate fecha) {
        Locale loc = LocaleContextHolder.getLocale();
        return capitalizar(fecha.format(DateTimeFormatter.ofPattern("MMMM yyyy", loc)));
    }

    public String fechaHora(LocalDateTime fh) {
        if (fh == null) {
            return "";
        }
        return fechaCorta(fh.toLocalDate()) + " " + hora(fh.toLocalTime());
    }

    private String capitalizar(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
