package com.canchalibre.canchalibre.security;

import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.service.UsuarioService;
import java.security.Principal;
import org.springframework.stereotype.Component;

/** Obtiene la entidad Usuario del usuario autenticado. */
@Component
public class SesionUsuario {

    private final UsuarioService usuarioService;

    public SesionUsuario(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** @return el usuario o null si la peticion es anonima */
    public Usuario actual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioService.buscarPorEmail(principal.getName()).orElse(null);
    }
}
