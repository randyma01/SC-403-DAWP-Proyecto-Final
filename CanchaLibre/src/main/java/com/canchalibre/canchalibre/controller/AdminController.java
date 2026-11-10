package com.canchalibre.canchalibre.controller;

import com.canchalibre.canchalibre.config.Mensajes;
import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.domain.Reserva;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.security.SesionUsuario;
import com.canchalibre.canchalibre.service.AdminService;
import com.canchalibre.canchalibre.service.NegocioException;
import com.canchalibre.canchalibre.service.ReservaService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Panel del administrador: reservas, pagos y validacion de comprobantes (HU-15). Solo ROLE_ADMIN. */
@Controller
public class AdminController {

    private static final int TAMANO_PAGINA = 8;

    private final ReservaService reservaService;
    private final AdminService adminService;
    private final SesionUsuario sesion;
    private final Mensajes mensajes;

    public AdminController(ReservaService reservaService, AdminService adminService, SesionUsuario sesion, Mensajes mensajes) {
        this.reservaService = reservaService;
        this.adminService = adminService;
        this.sesion = sesion;
        this.mensajes = mensajes;
    }

    @GetMapping("/admin")
    public String panel(@RequestParam(required = false) String q,
                        @RequestParam(required = false) EstadoReserva estado,
                        @RequestParam(required = false) Long espacioId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                        @RequestParam(required = false) Long sel,
                        @RequestParam(defaultValue = "0") int pagina,
                        Principal principal, Model model) {
        Usuario admin = sesion.actual(principal);
        Complejo complejo = adminService.complejoDe(admin);
        reservaService.procesarVencidas();

        Page<Reserva> reservas = reservaService.buscarParaAdmin(complejo.getId(), q, estado, espacioId, fecha, pagina, TAMANO_PAGINA);
        Reserva seleccionada = null;
        if (sel != null) {
            try {
                seleccionada = reservaService.obtenerParaAdmin(admin, sel);
            } catch (NegocioException ignorada) {
                seleccionada = null;
            }
        }
        if (seleccionada == null && !reservas.isEmpty()) {
            seleccionada = reservas.getContent().get(0);
        }
        model.addAttribute("complejo", complejo);
        model.addAttribute("stats", reservaService.estadisticas(complejo.getId()));
        model.addAttribute("reservas", reservas);
        model.addAttribute("seleccionada", seleccionada);
        model.addAttribute("espacios", adminService.listarEspacios(admin));
        model.addAttribute("estados", EstadoReserva.values());
        model.addAttribute("q", q);
        model.addAttribute("estadoSel", estado == null ? "" : estado.name());
        model.addAttribute("espacioSel", espacioId);
        model.addAttribute("fecha", fecha);
        model.addAttribute("hoy", LocalDate.now());
        return "admin/panel";
    }

    @PostMapping("/admin/reservas/{id}/aprobar")
    public String aprobar(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            Reserva r = reservaService.aprobar(sesion.actual(principal), id);
            ra.addFlashAttribute("exito", mensajes.get("admin.exito.aprobada", r.getCodigo()));
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
        }
        return "redirect:/admin?sel=" + id;
    }

    @PostMapping("/admin/reservas/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestParam(required = false) String motivo,
                           Principal principal, RedirectAttributes ra) {
        try {
            Reserva r = reservaService.rechazar(sesion.actual(principal), id, motivo);
            ra.addFlashAttribute("exito", mensajes.get("admin.exito.rechazada", r.getCodigo()));
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
        }
        return "redirect:/admin?sel=" + id;
    }
}
