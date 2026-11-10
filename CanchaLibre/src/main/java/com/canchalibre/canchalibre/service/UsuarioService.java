package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.Rol;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.dto.RegistroForm;
import com.canchalibre.canchalibre.repository.RolRepository;
import com.canchalibre.canchalibre.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro de usuarios y carga de credenciales para Spring Security. */
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepo, RolRepository rolRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo = rolRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = usuarioRepo.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new User(u.getEmail(), u.getPassword(), u.isActivo(), true, true, true,
                List.of(new SimpleGrantedAuthority(u.getRol().getNombre())));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepo.findByEmailIgnoreCase(email);
    }

    /** Registra un deportista (rol ROLE_USER). */
    @Transactional
    public Usuario registrar(RegistroForm form) {
        if (!form.getPassword().equals(form.getConfirmarPassword())) {
            throw new NegocioException("registro.error.password");
        }
        String email = form.getEmail().trim().toLowerCase();
        if (usuarioRepo.existsByEmailIgnoreCase(email)) {
            throw new NegocioException("registro.error.email");
        }
        Rol rol = rolRepo.findByNombre(Rol.USER).orElseThrow(() -> new NegocioException("registro.error.rol"));
        Usuario u = new Usuario();
        u.setRol(rol);
        u.setNombre(form.getNombre().trim());
        u.setApellidos(form.getApellidos().trim());
        u.setEmail(email);
        u.setTelefono(form.getTelefono() == null || form.getTelefono().isBlank() ? null : form.getTelefono().trim());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        return usuarioRepo.save(u);
    }
}
