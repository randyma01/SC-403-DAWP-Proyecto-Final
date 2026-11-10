package com.canchalibre.canchalibre.controller;

import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.domain.Reserva;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.security.SesionUsuario;
import com.canchalibre.canchalibre.service.NotificacionService;
import com.canchalibre.canchalibre.service.ReservaService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Panel personal del deportista: historial de reservas (HU-14) y notificaciones. */
@Controller
public class CuentaController {

    private final ReservaService reservaService;
    private final NotificacionService notificaciones;
    private final SesionUsuario sesion;

    public CuentaController(ReservaService reservaService, NotificacionService notificaciones, SesionUsuario sesion) {
        this.reservaService = reservaService;
        this.notificaciones = notificaciones;
        this.sesion = sesion;
    }

    @GetMapping("/mis-reservas")
    public String misReservas(@RequestParam(defaultValue = "todas") String tab,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                              Principal principal, Model model) {
        Usuario usuario = sesion.actual(principal);
        reservaService.procesarVencidas();
        List<Reserva> todas = reservaService.listarDelUsuario(usuario.getId());
        String texto = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<Reserva> filtradas = todas.stream()
                .filter(r -> coincideTab(r.getEstado(), tab))
                .filter(r -> fecha == null || r.getFecha().equals(fecha))
                .filter(r -> texto.isEmpty()
                        || r.getCodigo().toLowerCase(Locale.ROOT).contains(texto)
                        || r.getEspacio().getNombre().toLowerCase(Locale.ROOT).contains(texto)
                        || r.getEspacio().getComplejo().getNombre().toLowerCase(Locale.ROOT).contains(texto))
                .toList();
        // Proxima reserva activa (la mas cercana en el futuro)
        LocalDateTime ahora = LocalDateTime.now();
        Reserva proxima = todas.stream()
                .filter(r -> EstadoReserva.ACTIVAS.contains(r.getEstado()))
                .filter(r -> r.getFecha().atTime(r.getHoraInicio()).isAfter(ahora))
                .min((a, b) -> a.getFecha().atTime(a.getHoraInicio()).compareTo(b.getFecha().atTime(b.getHoraInicio())))
                .orElse(null);
        model.addAttribute("reservas", filtradas);
        model.addAttribute("tab", tab);
        model.addAttribute("q", q);
        model.addAttribute("fecha", fecha);
        model.addAttribute("proxima", proxima);
        return "cuenta/mis-reservas";
    }

    static boolean coincideTab(EstadoReserva e, String tab) {
        return switch (tab) {
            case "pendientes" -> e.isPendiente();
            case "confirmadas" -> e == EstadoReserva.CONFIRMADA || e == EstadoReserva.FINALIZADA;
            case "canceladas" -> e == EstadoReserva.CANCELADA || e == EstadoReserva.EXPIRADA || e == EstadoReserva.RECHAZADA;
            default -> true;
        };
    }

    @GetMapping("/cuenta/notificaciones")
    public String notificaciones(Principal principal, Model model) {
        Usuario usuario = sesion.actual(principal);
        model.addAttribute("lista", notificaciones.listar(usuario.getId()));
        notificaciones.marcarTodasLeidas(usuario.getId());
        return "cuenta/notificaciones";
    }
}
