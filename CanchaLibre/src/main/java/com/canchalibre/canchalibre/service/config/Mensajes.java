package com.canchalibre.canchalibre.config;

import com.canchalibre.canchalibre.service.NegocioException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/** Traduce las claves de NegocioException al idioma de la peticion actual. */
@Component
public class Mensajes {

    private final MessageSource messageSource;

    public Mensajes(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String de(NegocioException ex) {
        return messageSource.getMessage(ex.getClave(), ex.getArgumentos(), ex.getClave(), LocaleContextHolder.getLocale());
    }

    public String get(String clave, Object... args) {
        return messageSource.getMessage(clave, args, clave, LocaleContextHolder.getLocale());
    }
}
