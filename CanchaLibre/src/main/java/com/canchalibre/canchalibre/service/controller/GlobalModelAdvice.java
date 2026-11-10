package com.canchalibre.canchalibre.controller;

import com.canchalibre.canchalibre.config.Mensajes;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.repository.ReservaRepository;
import com.canchalibre.canchalibre.security.SesionUsuario;
import com.canchalibre.canchalibre.service.NegocioException;
import com.canchalibre.canchalibre.service.NotificacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Datos disponibles en TODAS las vistas (usuario actual, notificaciones, ruta) y manejo de errores de negocio. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final SesionUsuario sesion;
    private final NotificacionService notificaciones;
    private final ReservaRepository reservaRepo;
    private final Mensajes mensajes;

    public GlobalModelAdvice(SesionUsuario sesion, NotificacionService notificaciones, ReservaRepository reservaRepo, Mensajes mensajes) {
        this.sesion = sesion;
        this.notificaciones = notificaciones;
        this.reservaRepo = reservaRepo;
        this.mensajes = mensajes;
    }

    @ModelAttribute
    public void datosGlobales(Model model, Principal principal, HttpServletRequest request) {
        model.addAttribute("uri", request.getRequestURI());
        Usuario u = sesion.actual(principal);
        model.addAttribute("usuarioActual", u);
        if (u != null) {
            model.addAttribute("notifNoLeidas", notificaciones.noLeidas(u.getId()));
            if (u.isAdmin() && u.getComplejo() != null) {
                model.addAttribute("adminPendientes",
                        reservaRepo.countByEspacioComplejoIdAndEstado(u.getComplejo().getId(), EstadoReserva.EN_VERIFICACION));
            }
        }
    }

    @ExceptionHandler(NegocioException.class)
    public String negocio(NegocioException ex, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("mensajeError", mensajes.de(ex));
        return "error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String archivoGrande(Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        model.addAttribute("mensajeError", mensajes.get("pago.error.tamano"));
        return "error";
    }
}
