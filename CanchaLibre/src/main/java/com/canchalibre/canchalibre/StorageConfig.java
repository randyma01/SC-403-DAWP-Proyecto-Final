package com.canchalibre.canchalibre;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/** Cliente de Google Cloud Storage; solo se crea si app.storage.provider=firebase (por defecto). */
@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "firebase", matchIfMissing = true)
public class StorageConfig {

    @Value("${firebase.json.path}")
    private String jsonPath;

    @Value("${firebase.json.file}")
    private String jsonFile;

    @Bean
    public Storage storage() throws IOException {
        if (jsonFile == null || jsonFile.isBlank()) {
            throw new IllegalStateException("Firebase no está configurado: defina FIREBASE_JSON_FILE y FIREBASE_BUCKET_NAME "
                    + "en secrets.properties, o use STORAGE_PROVIDER=local para desarrollo.");
        }
        // En Render / producción los secretos se montan usualmente en /etc/secrets
        File secretFile = new File("/etc/secrets" + File.separator + jsonFile);
        InputStream inputStream = secretFile.exists()
                ? new FileInputStream(secretFile)
                : new ClassPathResource(jsonPath + "/" + jsonFile).getInputStream();

        try (inputStream) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
            return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        }
    }
}
