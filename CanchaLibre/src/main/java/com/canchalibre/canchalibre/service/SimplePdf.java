package com.canchalibre.canchalibre.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generador minimo de PDF de una pagina (texto, lineas y rectangulos) sin dependencias externas.
 * Usa las fuentes estandar Helvetica / Helvetica-Bold con codificacion WinAnsi (acentos y enie).
 */
final class SimplePdf {

    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private final StringBuilder contenido = new StringBuilder();

    static final float ANCHO = 595f;  // A4
    static final float ALTO = 842f;

    /** Rectangulo relleno con color RGB (0..1). */
    void rect(float x, float y, float w, float h, float r, float g, float b) {
        contenido.append(String.format(Locale.US, "%.3f %.3f %.3f rg %.2f %.2f %.2f %.2f re f\n", r, g, b, x, y, w, h));
    }

    void linea(float x1, float y1, float x2, float y2, float gris) {
        contenido.append(String.format(Locale.US, "%.3f G 0.7 w %.2f %.2f m %.2f %.2f l S\n", gris, x1, y1, x2, y2));
    }

    void texto(float x, float y, float tamano, boolean negrita, float r, float g, float b, String s) {
        contenido.append(String.format(Locale.US, "BT %.3f %.3f %.3f rg /%s %.1f Tf %.2f %.2f Td (%s) Tj ET\n",
                r, g, b, negrita ? "F2" : "F1", tamano, x, y, escapar(s)));
    }

    /** Texto alineado a la derecha respecto a xDerecha (aproximacion: 0.5 em por caracter). */
    void textoDerecha(float xDerecha, float y, float tamano, boolean negrita, float r, float g, float b, String s) {
        float ancho = s.length() * tamano * (negrita ? 0.56f : 0.52f);
        texto(xDerecha - ancho, y, tamano, negrita, r, g, b, s);
    }

    private static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch == '\\' || ch == '(' || ch == ')') {
                sb.append('\\').append(ch);
            } else if (ch < 32) {
                sb.append(' ');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    byte[] construir() {
        byte[] stream = contenido.toString().getBytes(WIN_ANSI);
        List<byte[]> objetos = new ArrayList<>();
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(WIN_ANSI));
        objetos.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(WIN_ANSI));
        objetos.add(("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + (int) ANCHO + " " + (int) ALTO + "] "
                + "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>").getBytes(WIN_ANSI));
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>".getBytes(WIN_ANSI));
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>".getBytes(WIN_ANSI));
        ByteArrayOutputStream cont = new ByteArrayOutputStream();
        cont.writeBytes(("<< /Length " + stream.length + " >>\nstream\n").getBytes(WIN_ANSI));
        cont.writeBytes(stream);
        cont.writeBytes("\nendstream".getBytes(WIN_ANSI));
        objetos.add(cont.toByteArray());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes("%PDF-1.4\n".getBytes(WIN_ANSI));
        int[] offsets = new int[objetos.size() + 1];
        for (int i = 0; i < objetos.size(); i++) {
            offsets[i + 1] = out.size();
            out.writeBytes(((i + 1) + " 0 obj\n").getBytes(WIN_ANSI));
            out.writeBytes(objetos.get(i));
            out.writeBytes("\nendobj\n".getBytes(WIN_ANSI));
        }
        int xref = out.size();
        StringBuilder sb = new StringBuilder("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        for (int i = 1; i <= objetos.size(); i++) {
            sb.append(String.format(Locale.US, "%010d 00000 n \n", offsets[i]));
        }
        sb.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF\n");
        out.writeBytes(sb.toString().getBytes(WIN_ANSI));
        return out.toByteArray();
    }
}
