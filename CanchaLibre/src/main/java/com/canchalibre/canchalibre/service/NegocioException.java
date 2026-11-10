package com.canchalibre.canchalibre.service;

/**
 * Error de regla de negocio. Lleva una clave del archivo messages.properties (i18n) y sus argumentos,
 * de modo que el controlador pueda mostrar el mensaje en el idioma del usuario.
 */
public class NegocioException extends RuntimeException {

    private final String clave;
    private final transient Object[] argumentos;

    public NegocioException(String clave, Object... argumentos) {
        super(clave);
        this.clave = clave;
        this.argumentos = argumentos;
    }

    public String getClave() {
        return clave;
    }

    public Object[] getArgumentos() {
        return argumentos;
    }
}
