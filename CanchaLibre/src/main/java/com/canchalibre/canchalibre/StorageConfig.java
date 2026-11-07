package com.canchalibre.canchalibre;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class StorageConfig {

    @Value("${firebase.json.path}")
    private String jsonPath;

    @Value("${firebase.json.file}")
    private String jsonFile;

    @Bean
    public Storage storage() throws IOException {
        // En Render / producción los secretos se montan usualmente en /etc/secrets
        File secretFile = new File("/etc/secrets" + File.separator + jsonFile);
        InputStream inputStream = secretFile.exists()
                ? new FileInputStream(secretFile)
                : new ClassPathResource(jsonPath + File.separator + jsonFile).getInputStream();

        try (inputStream) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
            return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        }
    }
}
