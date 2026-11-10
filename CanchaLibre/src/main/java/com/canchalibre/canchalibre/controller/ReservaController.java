package com.canchalibre.canchalibre.controller;

import com.canchalibre.canchalibre.config.Mensajes;
import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.domain.Reserva;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.dto.Cotizacion;
import com.canchalibre.canchalibre.dto.InsumoVista;
import com.canchalibre.canchalibre.security.SesionUsuario;
import com.canchalibre.canchalibre.service.CatalogoService;
import com.canchalibre.canchalibre.service.ComprobantePdfService;
import com.canchalibre.canchalibre.service.CotizacionService;
import com.canchalibre.canchalibre.service.DisponibilidadService;
import com.canchalibre.canchalibre.service.NegocioException;
import com.canchalibre.canchalibre.service.ReservaService;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Flujo de reserva en 4 pasos (prototipo): 1 Espacio, 2 Fecha y hora, 3 Insumos y cotizacion, 4 Reserva y pago.
 * HU-04, HU-06, HU-07, HU-09, HU-10, HU-11, HU-12, HU-13.
 */
@Controller
public class ReservaController {

    private final CatalogoService catalogo;
    private final DisponibilidadService disponibilidad;
    private final CotizacionService cotizacionService;
    private final ReservaService reservaService;
    private final ComprobantePdfService pdfService;
    private final SesionUsuario sesion;
    private final Mensajes mensajes;

    public ReservaController(CatalogoService catalogo, DisponibilidadService disponibilidad, CotizacionService cotizacionService,
                             ReservaService reservaService, ComprobantePdfService pdfService, SesionUsuario sesion, Mensajes mensajes) {
        this.catalogo = catalogo;
        this.disponibilidad = disponibilidad;
        this.cotizacionService = cotizacionService;
        this.reservaService = reservaService;
        this.pdfService = pdfService;
        this.sesion = sesion;
        this.mensajes = mensajes;
    }

    // ------------------------------------------------------------ Paso 2: fecha y hora (publico)

    @GetMapping("/complejos/{id}/disponibilidad")
    public String disponibilidad(@PathVariable Long id,
                                 @RequestParam(required = false) Long espacio,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                 Model model) {
        Complejo complejo = catalogo.obtenerComplejo(id);
        List<Espacio> espacios = catalogo.espaciosActivos(id);
        if (espacios.isEmpty()) {
            throw new NegocioException("catalogo.error.espacio");
        }
        Espacio seleccionado = espacios.stream().filter(e -> e.getId().equals(espacio)).findFirst().orElse(espacios.get(0));
        LocalDate hoy = LocalDate.now();
        LocalDate f = fecha == null || fecha.isBefore(hoy) ? hoy : fecha;
        YearMonth mes = YearMonth.from(f);
        YearMonth mesActual = YearMonth.from(hoy);

        model.addAttribute("complejo", complejo);
        model.addAttribute("espacios", espacios);
        model.addAttribute("espacio", seleccionado);
        model.addAttribute("fecha", f);
        model.addAttribute("mes", mes.atDay(1));
        model.addAttribute("dias", disponibilidad.calendario(mes, f));
        model.addAttribute("filas", disponibilidad.cuadricula(complejo, espacios, f));
        // enlaces de navegacion entre meses (no se puede ir a meses pasados)
        YearMonth anterior = mes.minusMonths(1);
        model.addAttribute("mesAnterior", anterior.isBefore(mesActual) ? null : (anterior.equals(mesActual) ? hoy : anterior.atDay(1)));
        model.addAttribute("mesSiguiente", mes.plusMonths(1).atDay(1));
        return "reserva/disponibilidad";
    }

    // ------------------------------------------------------------ Paso 3: insumos y cotizacion (requiere sesion)

    @GetMapping("/reservar/insumos")
    public String insumos(@RequestParam Long espacioId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                          @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                          Model model, RedirectAttributes ra) {
        Espacio espacio = catalogo.obtenerEspacio(espacioId);
        if (!disponibilidad.estaDisponible(espacio, fecha, hora)) {
            ra.addFlashAttribute("error", mensajes.get("reserva.error.ocupado"));
            return "redirect:/complejos/" + espacio.getComplejo().getId() + "/disponibilidad?espacio=" + espacioId + "&fecha=" + fecha;
        }
        List<InsumoVista> todos = cotizacionService.insumosDisponibles(espacio, fecha, hora);
        List<InsumoVista> equipamiento = new ArrayList<>();
        List<InsumoVista> servicios = new ArrayList<>();
        for (InsumoVista v : todos) {
            (v.getInsumo().getCategoria().name().equals("EQUIPAMIENTO") ? equipamiento : servicios).add(v);
        }
        Cotizacion base = cotizacionService.cotizar(espacio, hora, List.of());
        model.addAttribute("espacio", espacio);
        model.addAttribute("complejo", espacio.getComplejo());
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", hora);
        model.addAttribute("horaFin", hora.plusHours(1));
        model.addAttribute("equipamiento", equipamiento);
        model.addAttribute("servicios", servicios);
        model.addAttribute("base", base);
        return "reserva/insumos";
    }

    // ------------------------------------------------------------ HU-09: reservar y retener el bloque

    @PostMapping("/reservar")
    public String reservar(@RequestParam Long espacioId,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                           @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                           @RequestParam Map<String, String> params,
                           Principal principal, RedirectAttributes ra) {
        Usuario usuario = sesion.actual(principal);
        Map<Long, Integer> cantidades = new HashMap<>();
        for (Map.Entry<String, String> p : params.entrySet()) {
            if (p.getKey().startsWith("insumo_")) {
                try {
                    cantidades.put(Long.parseLong(p.getKey().substring(7)), Integer.parseInt(p.getValue().trim()));
                } catch (NumberFormatException ignorado) {
                    // parametro invalido: se ignora
                }
            }
        }
        try {
            Reserva r = reservaService.crear(usuario, espacioId, fecha, hora, cantidades);
            return "redirect:/reservas/" + r.getCodigo() + "/pago";
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
            return "redirect:/reservar/insumos?espacioId=" + espacioId + "&fecha=" + fecha + "&hora=" + hora;
        }
    }

    // ------------------------------------------------------------ Paso 4: pago con temporizador (HU-10, HU-11)

    @GetMapping("/reservas/{codigo}/pago")
    public String pago(@PathVariable String codigo, Principal principal, Model model, RedirectAttributes ra) {
        Usuario usuario = sesion.actual(principal);
        reservaService.procesarVencidas();
        Reserva r = reservaService.obtenerDelUsuario(usuario, codigo);
        if (r.getEstado() == EstadoReserva.EXPIRADA) {
            ra.addFlashAttribute("error", mensajes.get("pago.error.expirada"));
            return "redirect:/mis-reservas";
        }
        if (r.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            return "redirect:/reservas/" + codigo + "/confirmacion";
        }
        long restante = Math.max(0, Duration.between(LocalDateTime.now(), r.getExpiraEn()).getSeconds());
        model.addAttribute("reserva", r);
        model.addAttribute("complejo", r.getEspacio().getComplejo());
        model.addAttribute("restanteSegundos", restante);
        model.addAttribute("totalSegundos", 15 * 60);
        return "reserva/pago";
    }

    @PostMapping("/reservas/{codigo}/pago")
    public String registrarPago(@PathVariable String codigo,
                                @RequestParam(required = false) String referencia,
                                @RequestParam(required = false) MultipartFile comprobante,
                                Principal principal, RedirectAttributes ra) {
        Usuario usuario = sesion.actual(principal);
        try {
            reservaService.registrarPago(usuario, codigo, referencia, comprobante);
            ra.addFlashAttribute("exito", mensajes.get("pago.exito"));
            return "redirect:/reservas/" + codigo + "/confirmacion";
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
            return "redirect:/reservas/" + codigo + "/pago";
        }
    }

    // ------------------------------------------------------------ confirmacion, detalle, cancelacion, PDF (HU-12, HU-13)

    @GetMapping("/reservas/{codigo}/confirmacion")
    public String confirmacion(@PathVariable String codigo, Principal principal, Model model) {
        Usuario usuario = sesion.actual(principal);
        Reserva r = reservaService.obtenerDelUsuario(usuario, codigo);
        model.addAttribute("reserva", r);
        model.addAttribute("reservas", reservaService.listarDelUsuario(usuario.getId()));
        model.addAttribute("tab", "todas");
        return "reserva/confirmacion";
    }

    @GetMapping("/reservas/{codigo}")
    public String detalle(@PathVariable String codigo, Principal principal, Model model) {
        Usuario usuario = sesion.actual(principal);
        reservaService.procesarVencidas();
        Reserva r = reservaService.obtenerDelUsuario(usuario, codigo);
        model.addAttribute("reserva", r);
        model.addAttribute("historial", reservaService.historial(r.getId()));
        return "cuenta/reserva-detalle";
    }

    @PostMapping("/reservas/{codigo}/cancelar")
    public String cancelar(@PathVariable String codigo, Principal principal, RedirectAttributes ra) {
        Usuario usuario = sesion.actual(principal);
        try {
            reservaService.cancelar(usuario, codigo);
            ra.addFlashAttribute("exito", mensajes.get("cancelar.exito"));
            return "redirect:/mis-reservas";
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
            return "redirect:/reservas/" + codigo;
        }
    }

    @GetMapping("/reservas/{codigo}/comprobante.pdf")
    public ResponseEntity<byte[]> comprobante(@PathVariable String codigo, Principal principal) {
        Usuario usuario = sesion.actual(principal);
        Reserva r = reservaService.obtenerAutorizada(usuario, codigo);
        byte[] pdf = pdfService.generar(r);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("comprobante-" + r.getCodigo() + ".pdf").build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
