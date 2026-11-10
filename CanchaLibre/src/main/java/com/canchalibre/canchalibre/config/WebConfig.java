package com.canchalibre.canchalibre.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/** Internacionalizacion (?lang=es|en), archivos locales subidos y tareas programadas. */
@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.local-dir:uploads}")
    private String directorioLocal;

    /** Idioma por defecto: espanol. Se guarda en la sesion al usar ?lang=en. */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("es"));
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
    }

    /** Sirve /uploads/** desde la carpeta local (solo se usa con app.storage.provider=local). */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(directorioLocal).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**").addResourceLocations(dir.toUri().toString());
    }
}
