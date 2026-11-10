package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.BloqueHorario;
import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.EstadoBloque;
import com.canchalibre.canchalibre.domain.EstadoReserva;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.domain.Reserva;
import com.canchalibre.canchalibre.domain.ReservaInsumo;
import com.canchalibre.canchalibre.domain.TipoTransaccion;
import com.canchalibre.canchalibre.domain.Transaccion;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.dto.Cotizacion;
import com.canchalibre.canchalibre.dto.LineaCotizacion;
import com.canchalibre.canchalibre.dto.PanelStats;
import com.canchalibre.canchalibre.repository.BloqueHorarioRepository;
import com.canchalibre.canchalibre.repository.EspacioRepository;
import com.canchalibre.canchalibre.repository.ReservaRepository;
import com.canchalibre.canchalibre.repository.TransaccionRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Ciclo de vida transaccional de las reservas (HU-09 a HU-15):
 * crear y retener el bloque, temporizador de 15 min, comprobante, cancelacion, validacion administrativa.
 */
@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final ReservaRepository reservaRepo;
    private final BloqueHorarioRepository bloqueRepo;
    private final EspacioRepository espacioRepo;
    private final TransaccionRepository transaccionRepo;
    private final CotizacionService cotizacionService;
    private final ArchivoService archivoService;
    private final NotificacionService notificacionService;

    @Value("${app.reserva.minutos-limite:15}")
    private int minutosLimite;

    public ReservaService(ReservaRepository reservaRepo, BloqueHorarioRepository bloqueRepo, EspacioRepository espacioRepo,
                          TransaccionRepository transaccionRepo, CotizacionService cotizacionService,
                          ArchivoService archivoService, NotificacionService notificacionService) {
        this.reservaRepo = reservaRepo;
        this.bloqueRepo = bloqueRepo;
        this.espacioRepo = espacioRepo;
        this.transaccionRepo = transaccionRepo;
        this.cotizacionService = cotizacionService;
        this.archivoService = archivoService;
        this.notificacionService = notificacionService;
    }

    // ------------------------------------------------------------------ HU-09: crear y retener el bloque

    @Transactional
    public Reserva crear(Usuario usuario, Long espacioId, LocalDate fecha, LocalTime hora, Map<Long, Integer> cantidades) {
        LocalDateTime ahora = LocalDateTime.now();
        procesarVencidas();

        Espacio espacio = espacioRepo.findById(espacioId)
                .filter(Espacio::isActivo)
                .filter(e -> e.getComplejo().isActivo())
                .orElseThrow(() -> new NegocioException("catalogo.error.espacio"));
        validarHorario(espacio.getComplejo(), fecha, hora, ahora);

        // Bloqueo pesimista de la fila (si existe) + UNIQUE en la base: imposible reservar dos veces el mismo bloque
        BloqueHorario bloque = bloqueRepo.findParaActualizar(espacioId, fecha, hora).orElse(null);
        if (bloque == null) {
            bloque = new BloqueHorario();
            bloque.setEspacio(espacio);
            bloque.setFecha(fecha);
            bloque.setHoraInicio(hora);
            bloque.setHoraFin(hora.plusHours(1));
            bloque.setEstado(EstadoBloque.DISPONIBLE);
            try {
                bloque = bloqueRepo.saveAndFlush(bloque);
            } catch (DataIntegrityViolationException ex) {
                throw new NegocioException("reserva.error.ocupado");
            }
        } else if (!bloque.estaLibre(ahora)) {
            throw new NegocioException("reserva.error.ocupado");
        }

        List<LineaCotizacion> lineas = cotizacionService.resolverLineas(espacio, fecha, hora, cantidades, true);
        Cotizacion cot = cotizacionService.cotizar(espacio, hora, lineas);

        Reserva r = new Reserva();
        r.setCodigo("TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        r.setUsuario(usuario);
        r.setEspacio(espacio);
        r.setBloque(bloque);
        r.setFecha(fecha);
        r.setHoraInicio(hora);
        r.setHoraFin(hora.plusHours(1));
        r.setEstado(EstadoReserva.PENDIENTE_PAGO);
        r.setNocturno(cot.isNocturno());
        r.setTarifaDiurna(cot.getTarifaDiurna());
        r.setRecargoNocturno(cot.getRecargoNocturno());
        r.setSubtotalPista(cot.getSubtotalPista());
        r.setSubtotalInsumos(cot.getSubtotalInsumos());
        r.setImpuesto(cot.getImpuesto());
        r.setTotal(cot.getTotal());
        r.setDepositoGarantia(cot.getDepositoGarantia());
        r.setExpiraEn(ahora.plusMinutes(minutosLimite));
        for (LineaCotizacion l : lineas) {
            ReservaInsumo ri = new ReservaInsumo();
            ri.setReserva(r);
            ri.setInsumo(l.getInsumo());
            ri.setNombreInsumo(l.getInsumo().getNombre());
            ri.setCantidad(l.getCantidad());
            ri.setPrecioUnitario(l.getPrecioUnitario());
            ri.setSubtotal(l.getSubtotal());
            r.getInsumos().add(ri);
        }
        r = reservaRepo.saveAndFlush(r);
        r.setCodigo(String.format("CLB-%d-%s-%05d", ahora.getYear(), espacio.getDisciplina().getCodigo(), r.getId()));

        // El bloque pasa a "Apartado Temporal" en la base de datos mientras el usuario paga
        bloque.setEstado(EstadoBloque.APARTADO_TEMPORAL);
        bloque.setExpiraEn(r.getExpiraEn());
        bloque.setMotivo(null);

        registrar(r, usuario, TipoTransaccion.RESERVA_CREADA, null, EstadoReserva.PENDIENTE_PAGO, r.getTotal(),
                "Reserva creada; bloque apartado por " + minutosLimite + " minutos");
        return r;
    }

    private void validarHorario(Complejo c, LocalDate fecha, LocalTime hora, LocalDateTime ahora) {
        if (hora.getMinute() != 0 || hora.getSecond() != 0) {
            throw new NegocioException("reserva.error.horario");
        }
        int h = hora.toSecondOfDay() / 3600;
        int apertura = c.getHoraApertura().toSecondOfDay() / 3600;
        int cierre = c.getHoraCierre().toSecondOfDay() / 3600;
        if (h < apertura || h >= cierre) {
            throw new NegocioException("reserva.error.horario");
        }
        if (!fecha.atTime(hora).isAfter(ahora)) {
            throw new NegocioException("reserva.error.pasado");
        }
    }

    // ------------------------------------------------------------------ HU-11: comprobante de pago

    /**
     * Registra el comprobante dentro del plazo. noRollbackFor: si el plazo ya vencio, la expiracion queda guardada
     * aunque se informe el error al usuario. Todas las validaciones ocurren ANTES de modificar la reserva.
     */
    @Transactional(noRollbackFor = NegocioException.class)
    public Reserva registrarPago(Usuario usuario, String codigo, String referencia, MultipartFile archivo) {
        Reserva r = obtenerDelUsuario(usuario, codigo);
        if (r.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            throw new NegocioException("pago.error.estado");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (r.getExpiraEn() != null && !r.getExpiraEn().isAfter(ahora)) {
            expirar(r);
            throw new NegocioException("pago.error.expirada");
        }

        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        String ref = referencia == null || referencia.isBlank() ? null : referencia.trim();
        if (!hayArchivo && ref == null) {
            throw new NegocioException("pago.error.requerido");
        }
        if (ref != null && ref.length() > 60) {
            throw new NegocioException("pago.error.referencia");
        }
        String urlComprobante = null;
        String nombreComprobante = null;
        if (hayArchivo) {
            if (archivo.getSize() > MAX_BYTES) {
                throw new NegocioException("pago.error.tamano");
            }
            String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
            String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
            boolean extOk = nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".pdf");
            boolean tipoOk = tipo.equals("image/png") || tipo.equals("image/jpeg") || tipo.equals("application/pdf");
            if (!extOk || !tipoOk) {
                throw new NegocioException("pago.error.tipo");
            }
            try {
                urlComprobante = archivoService.subir(archivo, "comprobantes", r.getId());
            } catch (IOException ex) {
                log.error("No se pudo subir el comprobante de {}", codigo, ex);
                throw new NegocioException("pago.error.subida");
            }
            nombreComprobante = archivo.getOriginalFilename();
            if (nombreComprobante != null && nombreComprobante.length() > 200) {
                nombreComprobante = nombreComprobante.substring(nombreComprobante.length() - 200);
            }
        }

        EstadoReserva anterior = r.getEstado();
        r.setReferenciaBancaria(ref);
        if (hayArchivo) {
            r.setComprobanteUrl(urlComprobante);
            r.setComprobanteNombre(nombreComprobante);
            r.setComprobanteSubidoEn(ahora);
        }
        r.setEstado(EstadoReserva.EN_VERIFICACION);
        r.setExpiraEn(null); // el temporizador se detiene
        BloqueHorario b = r.getBloque();
        if (b != null) {
            b.setEstado(EstadoBloque.OCUPADO); // el espacio queda retenido a nombre del usuario
            b.setExpiraEn(null);
        }
        registrar(r, usuario, TipoTransaccion.PAGO_REGISTRADO, anterior, EstadoReserva.EN_VERIFICACION, r.getTotal(),
                "Comprobante de pago recibido");
        return r;
    }

    // ------------------------------------------------------------------ HU-12: cancelacion voluntaria

    @Transactional
    public Reserva cancelar(Usuario usuario, String codigo) {
        Reserva r = obtenerDelUsuario(usuario, codigo);
        if (!r.getEstado().isCancelable()) {
            throw new NegocioException("cancelar.error.estado");
        }
        EstadoReserva anterior = r.getEstado();
        liberarBloque(r);
        r.setEstado(EstadoReserva.CANCELADA);
        r.setExpiraEn(null);
        registrar(r, usuario, TipoTransaccion.CANCELACION, anterior, EstadoReserva.CANCELADA, BigDecimal.ZERO,
                "Cancelada por el usuario");
        return r;
    }

    // ------------------------------------------------------------------ HU-10: expiracion automatica

    /** Libera reservas cuyo plazo vencio y finaliza las confirmadas ya realizadas. Lo invoca el scheduler y las pantallas. */
    @Transactional
    public void procesarVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        for (Reserva r : reservaRepo.findByEstadoAndExpiraEnBefore(EstadoReserva.PENDIENTE_PAGO, ahora)) {
            expirar(r);
        }
        for (Reserva r : reservaRepo.findByEstadoAndFechaLessThanEqual(EstadoReserva.CONFIRMADA, ahora.toLocalDate())) {
            if (!r.getFecha().atTime(r.getHoraFin()).isAfter(ahora)) {
                EstadoReserva anterior = r.getEstado();
                r.setEstado(EstadoReserva.FINALIZADA);
                registrar(r, null, TipoTransaccion.FINALIZACION, anterior, EstadoReserva.FINALIZADA, BigDecimal.ZERO,
                        "Actividad finalizada");
            }
        }
        // Retenciones huerfanas (sin reserva pendiente asociada) que ya vencieron
        for (BloqueHorario b : bloqueRepo.findByEstadoAndExpiraEnBefore(EstadoBloque.APARTADO_TEMPORAL, ahora)) {
            b.setEstado(EstadoBloque.DISPONIBLE);
            b.setExpiraEn(null);
        }
    }

    private void expirar(Reserva r) {
        EstadoReserva anterior = r.getEstado();
        liberarBloque(r);
        r.setEstado(EstadoReserva.EXPIRADA);
        r.setExpiraEn(null);
        registrar(r, null, TipoTransaccion.EXPIRACION, anterior, EstadoReserva.EXPIRADA, BigDecimal.ZERO,
                "Venció el tiempo límite de pago");
        notificacionService.notificar(r.getUsuario(), r, "Reserva expirada",
                "La reserva " + r.getCodigo() + " expiró porque no se confirmó el pago a tiempo. El horario volvió a estar disponible.");
    }

    private void liberarBloque(Reserva r) {
        BloqueHorario b = r.getBloque();
        if (b != null) {
            b.setEstado(EstadoBloque.DISPONIBLE);
            b.setExpiraEn(null);
            b.setMotivo(null);
        }
        r.setBloque(null);
    }

    // ------------------------------------------------------------------ HU-15: validacion administrativa

    @Transactional
    public Reserva aprobar(Usuario admin, Long reservaId) {
        Reserva r = reservaDelAdmin(admin, reservaId);
        if (r.getEstado() != EstadoReserva.EN_VERIFICACION) {
            throw new NegocioException("admin.error.estado");
        }
        r.setEstado(EstadoReserva.CONFIRMADA);
        registrar(r, admin, TipoTransaccion.PAGO_APROBADO, EstadoReserva.EN_VERIFICACION, EstadoReserva.CONFIRMADA, r.getTotal(),
                "Pago validado por el administrador");
        notificacionService.notificar(r.getUsuario(), r, "Pago confirmado",
                "Su pago fue validado. La reserva " + r.getCodigo() + " está confirmada.");
        return r;
    }

    @Transactional
    public Reserva rechazar(Usuario admin, Long reservaId, String motivo) {
        Reserva r = reservaDelAdmin(admin, reservaId);
        if (r.getEstado() != EstadoReserva.EN_VERIFICACION) {
            throw new NegocioException("admin.error.estado");
        }
        String m = motivo == null ? "" : motivo.trim();
        if (m.isEmpty()) {
            throw new NegocioException("admin.error.motivo");
        }
        if (m.length() > 300) {
            throw new NegocioException("admin.error.motivo.largo");
        }
        liberarBloque(r);
        r.setEstado(EstadoReserva.RECHAZADA);
        r.setMotivoRechazo(m);
        registrar(r, admin, TipoTransaccion.PAGO_RECHAZADO, EstadoReserva.EN_VERIFICACION, EstadoReserva.RECHAZADA, BigDecimal.ZERO, m);
        notificacionService.notificar(r.getUsuario(), r, "Reserva rechazada",
                "Su reserva " + r.getCodigo() + " fue rechazada: " + m);
        return r;
    }

    private Reserva reservaDelAdmin(Usuario admin, Long reservaId) {
        Reserva r = reservaRepo.findById(reservaId).orElseThrow(() -> new NegocioException("reserva.error.noencontrada"));
        if (!perteneceAlComplejo(admin, r)) {
            throw new NegocioException("admin.error.permiso");
        }
        return r;
    }

    public boolean perteneceAlComplejo(Usuario admin, Reserva r) {
        return admin != null && admin.isAdmin() && admin.getComplejo() != null
                && admin.getComplejo().getId().equals(r.getEspacio().getComplejo().getId());
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public Reserva obtenerDelUsuario(Usuario usuario, String codigo) {
        Reserva r = reservaRepo.findByCodigo(codigo).orElseThrow(() -> new NegocioException("reserva.error.noencontrada"));
        if (!r.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("reserva.error.noencontrada");
        }
        return r;
    }

    /** Reserva visible para su dueno o para el administrador del complejo (descarga del comprobante). */
    @Transactional(readOnly = true)
    public Reserva obtenerAutorizada(Usuario usuario, String codigo) {
        Reserva r = reservaRepo.findByCodigo(codigo).orElseThrow(() -> new NegocioException("reserva.error.noencontrada"));
        if (r.getUsuario().getId().equals(usuario.getId()) || perteneceAlComplejo(usuario, r)) {
            return r;
        }
        throw new NegocioException("reserva.error.noencontrada");
    }

    /** HU-14: reservas del usuario, de la mas reciente a la mas antigua. */
    @Transactional(readOnly = true)
    public List<Reserva> listarDelUsuario(Long usuarioId) {
        return reservaRepo.findByUsuarioIdOrderByFechaDescHoraInicioDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Transaccion> historial(Long reservaId) {
        return transaccionRepo.findByReservaIdOrderByCreadoEnAsc(reservaId);
    }

    @Transactional(readOnly = true)
    public Page<Reserva> buscarParaAdmin(Long complejoId, String q, EstadoReserva estado, Long espacioId, LocalDate fecha, int pagina, int tamano) {
        Specification<Reserva> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            Join<Reserva, Espacio> esp = root.join("espacio");
            Join<Reserva, Usuario> usu = root.join("usuario");
            ps.add(cb.equal(esp.get("complejo").get("id"), complejoId));
            if (estado != null) {
                ps.add(cb.equal(root.get("estado"), estado));
            }
            if (espacioId != null) {
                ps.add(cb.equal(esp.get("id"), espacioId));
            }
            if (fecha != null) {
                ps.add(cb.equal(root.get("fecha"), fecha));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.<String>get("codigo")), like),
                        cb.like(cb.lower(usu.<String>get("nombre")), like),
                        cb.like(cb.lower(usu.<String>get("apellidos")), like),
                        cb.like(cb.lower(esp.<String>get("nombre")), like)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        Sort orden = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("horaInicio"));
        return reservaRepo.findAll(spec, PageRequest.of(Math.max(pagina, 0), tamano, orden));
    }

    @Transactional(readOnly = true)
    public Reserva obtenerParaAdmin(Usuario admin, Long id) {
        return reservaDelAdmin(admin, id);
    }

    @Transactional(readOnly = true)
    public PanelStats estadisticas(Long complejoId) {
        long hoy = reservaRepo.countByEspacioComplejoIdAndFechaAndEstadoIn(complejoId, LocalDate.now(),
                List.of(EstadoReserva.EN_VERIFICACION, EstadoReserva.CONFIRMADA, EstadoReserva.FINALIZADA));
        long pendientes = reservaRepo.countByEspacioComplejoIdAndEstado(complejoId, EstadoReserva.EN_VERIFICACION);
        long confirmadas = reservaRepo.countByEspacioComplejoIdAndEstado(complejoId, EstadoReserva.CONFIRMADA);
        long canceladas = reservaRepo.countByEspacioComplejoIdAndEstadoIn(complejoId,
                List.of(EstadoReserva.CANCELADA, EstadoReserva.RECHAZADA));
        return new PanelStats(hoy, pendientes, confirmadas, canceladas);
    }

    // ------------------------------------------------------------------ bitacora

    private void registrar(Reserva r, Usuario actor, TipoTransaccion tipo, EstadoReserva anterior, EstadoReserva nuevo,
                           BigDecimal monto, String detalle) {
        Transaccion t = new Transaccion();
        t.setReserva(r);
        t.setUsuario(actor);
        t.setTipo(tipo);
        t.setEstadoAnterior(anterior);
        t.setEstadoNuevo(nuevo);
        t.setMonto(monto);
        t.setDetalle(detalle != null && detalle.length() > 300 ? detalle.substring(0, 300) : detalle);
        transaccionRepo.save(t);
    }
}
