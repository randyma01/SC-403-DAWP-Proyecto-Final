package com.canchalibre.canchalibre.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

/**
 * Reglas puras de calculo de precios (HU-05, HU-07). Sin dependencias de Spring para poder probarlas facilmente.
 * Los precios del catalogo YA incluyen IVA; el impuesto se muestra desglosado dentro del total.
 */
public final class PrecioCalculator {

    private PrecioCalculator() {
    }

    /** Un bloque es nocturno si empieza a partir de la hora de inicio del horario nocturno del complejo. */
    public static boolean esNocturno(LocalTime horaInicio, LocalTime inicioNocturno) {
        return !horaInicio.isBefore(inicioNocturno);
    }

    /** Tarifa de la pista para un bloque: tarifa diurna (+ recargo por iluminacion si es nocturno). */
    public static BigDecimal subtotalPista(BigDecimal tarifaDiurna, BigDecimal recargoNocturno, boolean nocturno) {
        return nocturno ? tarifaDiurna.add(recargoNocturno) : tarifaDiurna;
    }

    /** IVA contenido en un monto que ya lo incluye: total - total / (1 + iva%). */
    public static BigDecimal ivaIncluido(BigDecimal total, BigDecimal porcentajeIva) {
        BigDecimal divisor = BigDecimal.ONE.add(porcentajeIva.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
        BigDecimal base = total.divide(divisor, 2, RoundingMode.HALF_UP);
        return total.subtract(base).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtotalLinea(BigDecimal precioUnitario, int cantidad) {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }
}
