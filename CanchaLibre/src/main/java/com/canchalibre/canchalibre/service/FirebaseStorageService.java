package com.canchalibre.canchalibre.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// @Service // Descomentar cuando se configure la conexión y credenciales de Firebase en StorageConfig
public class FirebaseStorageService {

    @Value("${firebase.bucket.name:}")
    private String bucketName;

    @Value("${firebase.storage.path:}")
    private String storagePath;

    // Inyección opcional o directa cuando se configure StorageConfig
    private final Storage storage;

    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    /**
     * Sube un archivo de imagen al almacenamiento de Firebase.
     */
    public String uploadImage(MultipartFile localFile, String folder, Integer id) throws IOException {
        String originalName = localFile.getOriginalFilename();
        String fileExtension = "";
        if (originalName != null && originalName.contains(".")) {
            fileExtension = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = "img" + getFormattedNumber(id) + fileExtension;
        File tempFile = convertToFile(localFile);

        try {
            return uploadToFirebase(tempFile, folder, fileName);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Convierte un MultipartFile a un archivo temporal en el servidor.
     */
    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload-", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }

    /**
     * Sube el archivo al almacenamiento de Firebase y genera una URL firmada válida por 5 años.
     */
    private String uploadToFirebase(File file, String folder, String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, storagePath + "/" + folder + "/" + fileName);
        String mimeType = Files.probeContentType(file.toPath());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType != null ? mimeType : "image/jpeg").build();

        storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        return storage.signUrl(blobInfo, 1825, TimeUnit.DAYS).toString();
    }

    /**
     * Genera un string numérico con formato de 14 dígitos con ceros a la izquierda.
     */
    private String getFormattedNumber(long id) {
        return String.format("%014d", id);
    }
}
