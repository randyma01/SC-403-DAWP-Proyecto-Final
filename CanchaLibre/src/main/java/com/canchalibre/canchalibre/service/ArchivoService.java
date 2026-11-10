package com.canchalibre.canchalibre.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/** Guarda imagenes y comprobantes y devuelve la URL con la que se consultan. */
public interface ArchivoService {

    /**
     * @param archivo archivo recibido del formulario
     * @param carpeta subcarpeta logica (p.ej. "comprobantes", "espacios")
     * @param id      numero que forma parte del nombre del archivo (id de reserva, marca de tiempo, ...)
     * @return URL (absoluta en Firebase, relativa a la app en almacenamiento local)
     */
    String subir(MultipartFile archivo, String carpeta, long id) throws IOException;
}
