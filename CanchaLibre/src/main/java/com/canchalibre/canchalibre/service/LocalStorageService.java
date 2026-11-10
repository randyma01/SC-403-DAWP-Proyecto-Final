package com.canchalibre.canchalibre.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Almacenamiento en disco (carpeta ./uploads) para desarrollo sin credenciales de Firebase. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local")
public class LocalStorageService implements ArchivoService {

    @Value("${app.storage.local-dir:uploads}")
    private String directorio;

    @Override
    public String subir(MultipartFile archivo, String carpeta, long id) throws IOException {
        Path destino = Paths.get(directorio, carpeta).toAbsolutePath().normalize();
        Files.createDirectories(destino);
        String nombre = "img" + String.format("%014d", id) + FirebaseStorageService.extension(archivo.getOriginalFilename());
        Files.write(destino.resolve(nombre), archivo.getBytes());
        return "/uploads/" + carpeta + "/" + nombre;
    }
}
