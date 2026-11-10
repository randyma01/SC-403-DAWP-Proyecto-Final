package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.Reserva;
import com.canchalibre.canchalibre.domain.ReservaInsumo;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/** Comprobante digital de reserva en PDF (HU-13). */
@Service
public class ComprobantePdfService {

    private final Formato fmt;
    private final MessageSource mensajes;

    public ComprobantePdfService(Formato fmt, MessageSource mensajes) {
        this.fmt = fmt;
        this.mensajes = mensajes;
    }

    private String t(String clave, Object... args) {
        return mensajes.getMessage(clave, args, clave, LocaleContextHolder.getLocale());
    }

    /** La fuente PDF estandar no incluye el simbolo del colon: se usa el codigo ISO "CRC". */
    private String monto(BigDecimal m) {
        return "CRC " + new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US)).format(m);
    }

    public byte[] generar(Reserva r) {
        SimplePdf pdf = new SimplePdf();
        float izq = 50f;
        float der = SimplePdf.ANCHO - 50f;

        // Encabezado
        pdf.rect(0, SimplePdf.ALTO - 110, SimplePdf.ANCHO, 110, 0.016f, 0.149f, 0.125f);
        pdf.rect(0, SimplePdf.ALTO - 116, SimplePdf.ANCHO, 6, 0.816f, 0.941f, 0.298f);
        pdf.texto(izq, SimplePdf.ALTO - 58, 26, true, 1, 1, 1, "CANCHA LIBRE");
        pdf.texto(izq, SimplePdf.ALTO - 82, 11, false, 0.816f, 0.941f, 0.298f, t("pdf.titulo"));
        pdf.textoDerecha(der, SimplePdf.ALTO - 58, 10, false, 1, 1, 1, t("pdf.codigo"));
        pdf.textoDerecha(der, SimplePdf.ALTO - 80, 16, true, 1, 1, 1, r.getCodigo());

        float y = SimplePdf.ALTO - 160;
        pdf.texto(izq, y, 11, false, 0.4f, 0.4f, 0.4f, t("pdf.estado"));
        pdf.texto(izq + 80, y, 12, true, 0.016f, 0.149f, 0.125f, t("reserva.estado." + r.getEstado().name()));
        y -= 34;

        // Datos de la reserva
        String[][] filas = {
            {t("pdf.complejo"), r.getEspacio().getComplejo().getNombre()},
            {t("pdf.espacio"), r.getEspacio().getNombre()},
            {t("pdf.ubicacion"), r.getEspacio().getComplejo().getCanton() + ", " + r.getEspacio().getComplejo().getProvincia()},
            {t("pdf.fecha"), fmt.fechaLarga(r.getFecha())},
            {t("pdf.horario"), fmt.rango(r.getHoraInicio(), r.getHoraFin())},
            {t("pdf.cliente"), r.getUsuario().getNombreCompleto()},
            {t("pdf.correo"), r.getUsuario().getEmail()},
        };
        for (String[] f : filas) {
            pdf.texto(izq, y, 10, false, 0.4f, 0.4f, 0.4f, f[0]);
            pdf.texto(izq + 110, y, 11, true, 0.1f, 0.1f, 0.1f, f[1]);
            y -= 20;
        }
        y -= 8;
        pdf.linea(izq, y, der, y, 0.75f);
        y -= 24;

        // Desglose
        pdf.texto(izq, y, 13, true, 0.016f, 0.149f, 0.125f, t("pdf.desglose"));
        y -= 22;
        String lineaPista = r.isNocturno() ? t("pdf.pista.nocturna") : t("pdf.pista.diurna");
        pdf.texto(izq, y, 11, false, 0.1f, 0.1f, 0.1f, lineaPista);
        pdf.textoDerecha(der, y, 11, false, 0.1f, 0.1f, 0.1f, monto(r.getSubtotalPista()));
        y -= 18;
        for (ReservaInsumo ri : r.getInsumos()) {
            pdf.texto(izq, y, 11, false, 0.1f, 0.1f, 0.1f, ri.getNombreInsumo() + " x " + ri.getCantidad());
            pdf.textoDerecha(der, y, 11, false, 0.1f, 0.1f, 0.1f, monto(ri.getSubtotal()));
            y -= 18;
        }
        y -= 4;
        pdf.linea(izq, y, der, y, 0.75f);
        y -= 20;
        pdf.texto(izq, y, 10, false, 0.4f, 0.4f, 0.4f, t("pdf.iva.incluido", monto(r.getImpuesto())));
        y -= 30;
        pdf.rect(izq - 8, y - 10, der - izq + 16, 34, 0.902f, 0.973f, 0.8f);
        pdf.texto(izq, y + 2, 13, true, 0.016f, 0.149f, 0.125f, t("pdf.total"));
        pdf.textoDerecha(der, y + 2, 15, true, 0.016f, 0.149f, 0.125f, monto(r.getTotal()));
        y -= 40;

        if (r.getDepositoGarantia() != null && r.getDepositoGarantia().signum() > 0) {
            pdf.texto(izq, y, 10, true, 0.7f, 0.4f, 0.0f, t("pdf.deposito", monto(r.getDepositoGarantia())));
            y -= 20;
        }
        if (r.getReferenciaBancaria() != null) {
            pdf.texto(izq, y, 10, false, 0.4f, 0.4f, 0.4f, t("pdf.referencia") + ": " + r.getReferenciaBancaria());
            y -= 20;
        }

        // Pie
        pdf.linea(izq, 90, der, 90, 0.75f);
        pdf.texto(izq, 70, 9, false, 0.4f, 0.4f, 0.4f, t("pdf.pie.1"));
        pdf.texto(izq, 56, 9, false, 0.4f, 0.4f, 0.4f, t("pdf.pie.2"));
        return pdf.construir();
    }
}
