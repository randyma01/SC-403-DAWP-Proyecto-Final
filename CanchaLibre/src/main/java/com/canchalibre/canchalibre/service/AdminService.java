package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.BloqueHorario;
import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.Disciplina;
import com.canchalibre.canchalibre.domain.EstadoBloque;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.domain.ImagenEspacio;
import com.canchalibre.canchalibre.domain.Insumo;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.dto.BloqueoForm;
import com.canchalibre.canchalibre.dto.ComplejoForm;
import com.canchalibre.canchalibre.dto.EspacioForm;
import com.canchalibre.canchalibre.dto.InsumoForm;
import com.canchalibre.canchalibre.dto.TarifaForm;
import com.canchalibre.canchalibre.repository.BloqueHorarioRepository;
import com.canchalibre.canchalibre.repository.ComplejoRepository;
import com.canchalibre.canchalibre.repository.DisciplinaRepository;
import com.canchalibre.canchalibre.repository.EspacioRepository;
import com.canchalibre.canchalibre.repository.InsumoRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Operaciones del administrador sobre SU complejo (HU-08 y epica 5): complejo, espacios, tarifas, insumos y bloqueos.
 * Todas validan que el recurso pertenezca al complejo del administrador autenticado.
 */
@Service
public class AdminService {

    private final ComplejoRepository complejoRepo;
    private final EspacioRepository espacioRepo;
    private final InsumoRepository insumoRepo;
    private final DisciplinaRepository disciplinaRepo;
    private final BloqueHorarioRepository bloqueRepo;
    private final ArchivoService archivoService;
    private final ReservaService reservaService;

    public AdminService(ComplejoRepository complejoRepo, EspacioRepository espacioRepo, InsumoRepository insumoRepo,
                        DisciplinaRepository disciplinaRepo, BloqueHorarioRepository bloqueRepo,
                        ArchivoService archivoService, ReservaService reservaService) {
        this.complejoRepo = complejoRepo;
        this.espacioRepo = espacioRepo;
        this.insumoRepo = insumoRepo;
        this.disciplinaRepo = disciplinaRepo;
        this.bloqueRepo = bloqueRepo;
        this.archivoService = archivoService;
        this.reservaService = reservaService;
    }

    // ------------------------------------------------------------------ complejo

    @Transactional(readOnly = true)
    public Complejo complejoDe(Usuario admin) {
        if (admin == null || !admin.isAdmin() || admin.getComplejo() == null) {
            throw new NegocioException("admin.error.sincomplejo");
        }
        return complejoRepo.findById(admin.getComplejo().getId()).orElseThrow(() -> new NegocioException("admin.error.sincomplejo"));
    }

    @Transactional
    public Complejo actualizarComplejo(Usuario admin, ComplejoForm f) {
        Complejo c = complejoDe(admin);
        int apertura = f.getHoraApertura().toSecondOfDay() / 3600;
        int cierre = f.getHoraCierre().toSecondOfDay() / 3600;
        if (cierre <= apertura) {
            throw new NegocioException("admin.error.horario");
        }
        c.setNombre(f.getNombre().trim());
        c.setDescripcion(f.getDescripcion());
        c.setDireccion(f.getDireccion());
        c.setTelefono(f.getTelefono());
        c.setHoraApertura(LocalTime.of(apertura, 0));
        c.setHoraCierre(LocalTime.of(cierre, 0));
        c.setHoraInicioNocturno(LocalTime.of(f.getHoraInicioNocturno().getHour(), 0));
        c.setParqueo(f.isParqueo());
        c.setVestidores(f.isVestidores());
        c.setSoda(f.isSoda());
        c.setIluminacionNocturna(f.isIluminacionNocturna());
        c.setBanco(f.getBanco());
        c.setTitular(f.getTitular());
        c.setIban(f.getIban());
        return c;
    }

    // ------------------------------------------------------------------ espacios

    @Transactional(readOnly = true)
    public List<Espacio> listarEspacios(Usuario admin) {
        return espacioRepo.findByComplejoIdOrderByNombreAsc(complejoDe(admin).getId());
    }

    @Transactional(readOnly = true)
    public Espacio obtenerEspacio(Usuario admin, Long id) {
        Complejo c = complejoDe(admin);
        Espacio e = espacioRepo.findById(id).orElseThrow(() -> new NegocioException("catalogo.error.espacio"));
        if (!e.getComplejo().getId().equals(c.getId())) {
            throw new NegocioException("admin.error.permiso");
        }
        return e;
    }

    @Transactional
    public Espacio guardarEspacio(Usuario admin, EspacioForm f) {
        Complejo c = complejoDe(admin);
        Espacio e = f.getId() == null ? new Espacio() : obtenerEspacio(admin, f.getId());
        Disciplina d = disciplinaRepo.findById(f.getDisciplinaId()).orElseThrow(() -> new NegocioException("admin.error.disciplina"));
        e.setComplejo(c);
        e.setDisciplina(d);
        e.setNombre(f.getNombre().trim());
        e.setDescripcion(f.getDescripcion());
        e.setSuperficie(f.getSuperficie());
        e.setTechada(f.isTechada());
        e.setCapacidad(f.getCapacidad());
        e.setMedidas(f.getMedidas());
        e.setIluminacion(f.isIluminacion());
        e.setReglamento(f.getReglamento());
        e.setTipoTarifa(f.getTipoTarifa());
        e.setTarifaDiurna(f.getTarifaDiurna());
        e.setRecargoNocturno(f.getRecargoNocturno());
        e.setDepositoGarantia(f.getDepositoGarantia());
        e.setActivo(f.isActivo());
        e = espacioRepo.save(e);

        MultipartFile imagen = f.getImagen();
        if (imagen != null && !imagen.isEmpty()) {
            String tipo = imagen.getContentType() == null ? "" : imagen.getContentType();
            if (!tipo.equals("image/png") && !tipo.equals("image/jpeg")) {
                throw new NegocioException("admin.error.imagen.tipo");
            }
            try {
                String url = archivoService.subir(imagen, "espacios", System.currentTimeMillis());
                ImagenEspacio img = new ImagenEspacio();
                img.setEspacio(e);
                img.setUrl(url);
                img.setOrden(e.getImagenes().size() + 1);
                e.getImagenes().add(img);
                if (e.getImagenUrl() == null || e.getImagenUrl().isBlank() || e.getImagenUrl().startsWith("/img/placeholders/")) {
                    e.setImagenUrl(url);
                }
            } catch (IOException ex) {
                throw new NegocioException("admin.error.imagen.subida");
            }
        }
        return e;
    }

    @Transactional
    public void alternarEspacio(Usuario admin, Long id) {
        Espacio e = obtenerEspacio(admin, id);
        e.setActivo(!e.isActivo());
    }

    /** HU-08: solo el administrador del complejo edita montos; deben ser numericos mayores a cero. */
    @Transactional
    public Espacio actualizarTarifa(Usuario admin, Long espacioId, TarifaForm f) {
        Espacio e = obtenerEspacio(admin, espacioId);
        e.setTarifaDiurna(f.getTarifaDiurna());
        e.setRecargoNocturno(f.getRecargoNocturno());
        e.setDepositoGarantia(f.getDepositoGarantia());
        e.setTipoTarifa(f.getTipoTarifa());
        return e;
    }

    // ------------------------------------------------------------------ insumos

    @Transactional(readOnly = true)
    public List<Insumo> listarInsumos(Usuario admin) {
        return insumoRepo.findByComplejoIdOrderByCategoriaAscNombreAsc(complejoDe(admin).getId());
    }

    @Transactional(readOnly = true)
    public Insumo obtenerInsumo(Usuario admin, Long id) {
        Complejo c = complejoDe(admin);
        Insumo i = insumoRepo.findById(id).orElseThrow(() -> new NegocioException("insumo.error.noexiste"));
        if (!i.getComplejo().getId().equals(c.getId())) {
            throw new NegocioException("admin.error.permiso");
        }
        return i;
    }

    @Transactional
    public Insumo guardarInsumo(Usuario admin, InsumoForm f) {
        Complejo c = complejoDe(admin);
        Insumo i = f.getId() == null ? new Insumo() : obtenerInsumo(admin, f.getId());
        i.setComplejo(c);
        i.setNombre(f.getNombre().trim());
        i.setDescripcion(f.getDescripcion());
        i.setCategoria(f.getCategoria());
        i.setPrecio(f.getPrecio());
        i.setUnidad(f.getUnidad().trim());
        i.setStock(f.getStock());
        i.setActivo(f.isActivo());
        Set<Disciplina> discs = new HashSet<>();
        if (f.getDisciplinaIds() != null) {
            discs.addAll(disciplinaRepo.findAllById(f.getDisciplinaIds()));
        }
        i.getDisciplinas().clear();
        i.getDisciplinas().addAll(discs);
        return insumoRepo.save(i);
    }

    @Transactional
    public void alternarInsumo(Usuario admin, Long id) {
        Insumo i = obtenerInsumo(admin, id);
        i.setActivo(!i.isActivo());
    }

    // ------------------------------------------------------------------ bloqueos operativos

    @Transactional(readOnly = true)
    public List<BloqueHorario> listarBloqueos(Usuario admin) {
        return bloqueRepo.findDelComplejo(complejoDe(admin).getId(), EstadoBloque.MANTENIMIENTO, LocalDate.now());
    }

    /** Pone en mantenimiento cada hora del rango [desde, hasta). Falla si alguna hora ya esta reservada. */
    @Transactional
    public int crearBloqueo(Usuario admin, BloqueoForm f) {
        Espacio e = obtenerEspacio(admin, f.getEspacioId());
        int desde = f.getHoraDesde().toSecondOfDay() / 3600;
        int hasta = f.getHoraHasta().toSecondOfDay() / 3600;
        if (hasta <= desde) {
            throw new NegocioException("bloqueo.error.rango");
        }
        if (f.getFecha().isBefore(LocalDate.now())) {
            throw new NegocioException("bloqueo.error.pasado");
        }
        reservaService.procesarVencidas();
        LocalDateTime ahora = LocalDateTime.now();
        int creados = 0;
        for (int h = desde; h < hasta; h++) {
            LocalTime inicio = LocalTime.of(h, 0);
            BloqueHorario b = bloqueRepo.findParaActualizar(e.getId(), f.getFecha(), inicio).orElse(null);
            if (b == null) {
                b = new BloqueHorario();
                b.setEspacio(e);
                b.setFecha(f.getFecha());
                b.setHoraInicio(inicio);
                b.setHoraFin(inicio.plusHours(1));
            } else if (b.getEstado() == EstadoBloque.MANTENIMIENTO) {
                continue;
            } else if (!b.estaLibre(ahora)) {
                throw new NegocioException("bloqueo.error.ocupado", inicio.toString());
            }
            b.setEstado(EstadoBloque.MANTENIMIENTO);
            b.setExpiraEn(null);
            b.setMotivo(f.getMotivo().trim());
            bloqueRepo.save(b);
            creados++;
        }
        return creados;
    }

    @Transactional
    public void eliminarBloqueo(Usuario admin, Long bloqueId) {
        Complejo c = complejoDe(admin);
        BloqueHorario b = bloqueRepo.findById(bloqueId).orElseThrow(() -> new NegocioException("bloqueo.error.noexiste"));
        if (!b.getEspacio().getComplejo().getId().equals(c.getId()) || b.getEstado() != EstadoBloque.MANTENIMIENTO) {
            throw new NegocioException("admin.error.permiso");
        }
        bloqueRepo.delete(b);
    }

    @Transactional(readOnly = true)
    public List<Disciplina> disciplinas() {
        return disciplinaRepo.findAllByOrderByOrdenAsc();
    }
}
