package com.canchalibre.canchalibre.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Almacenamiento en Firebase Storage (Google Cloud Storage). Activo con app.storage.provider=firebase. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "firebase", matchIfMissing = true)
public class FirebaseStorageService implements ArchivoService {

    @Value("${firebase.bucket.name}")
    private String bucketName;

    @Value("${firebase.storage.path}")
    private String storagePath;

    private final Storage storage;

    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    /** Compatibilidad con la configuracion inicial del proyecto. */
    public String uploadImage(MultipartFile localFile, String folder, Integer id) throws IOException {
        return subir(localFile, folder, id == null ? 0L : id.longValue());
    }

    @Override
    public String subir(MultipartFile archivo, String carpeta, long id) throws IOException {
        String fileName = "img" + getFormattedNumber(id) + extension(archivo.getOriginalFilename());
        String mimeType = archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream";

        BlobId blobId = BlobId.of(bucketName, storagePath + "/" + carpeta + "/" + fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType).build();
        storage.create(blobInfo, archivo.getBytes());

        // URL firmada valida por 5 anios (se guarda en la base de datos)
        return storage.signUrl(blobInfo, 1825, TimeUnit.DAYS).toString();
    }

    /** Extension en minusculas y solo alfanumerica (evita caracteres peligrosos en el nombre del objeto). */
    static String extension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return ext.matches("[a-z0-9]{1,5}") ? "." + ext : "";
    }

    /** Numero de 14 digitos con ceros a la izquierda. */
    private String getFormattedNumber(long id) {
        return String.format("%014d", id);
    }
}
