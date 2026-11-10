package com.canchalibre.canchalibre.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Autenticacion por formulario y autorizacion por roles:
 *  - Publico:  catalogo, detalle, disponibilidad, login y registro.
 *  - Autenticado: flujo de reserva, pago, "Mis reservas", notificaciones.
 *  - ROLE_ADMIN: panel /admin/**.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/reservar/**", "/reservas/**", "/mis-reservas/**", "/cuenta/**").authenticated()
                .anyRequest().permitAll())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(exitoLogin())
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }

    /** Tras iniciar sesion: vuelve a la pagina pedida; si no hay, el admin va a /admin y el deportista al inicio. */
    private AuthenticationSuccessHandler exitoLogin() {
        return (request, response, authentication) -> {
            SavedRequestAwareAuthenticationSuccessHandler destino = new SavedRequestAwareAuthenticationSuccessHandler();
            destino.setDefaultTargetUrl(esAdmin(authentication) ? "/admin" : "/");
            destino.onAuthenticationSuccess(request, response, authentication);
        };
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
