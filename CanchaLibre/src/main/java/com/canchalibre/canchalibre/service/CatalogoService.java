package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.Disciplina;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.domain.Superficie;
import com.canchalibre.canchalibre.domain.TipoAdministracion;
import com.canchalibre.canchalibre.dto.ComplejoCard;
import com.canchalibre.canchalibre.repository.ComplejoRepository;
import com.canchalibre.canchalibre.repository.DisciplinaRepository;
import com.canchalibre.canchalibre.repository.EspacioRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catalogo de complejos y espacios: exploracion, filtros y paginacion (HU-01, HU-02, HU-03). */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final ComplejoRepository complejoRepo;
    private final EspacioRepository espacioRepo;
    private final DisciplinaRepository disciplinaRepo;

    @Value("${app.catalogo.tamano-pagina:12}")
    private int tamanoPagina;

    public CatalogoService(ComplejoRepository complejoRepo, EspacioRepository espacioRepo, DisciplinaRepository disciplinaRepo) {
        this.complejoRepo = complejoRepo;
        this.espacioRepo = espacioRepo;
        this.disciplinaRepo = disciplinaRepo;
    }

    /**
     * Busca complejos activos que tengan al menos UN espacio activo que cumpla TODOS los filtros de espacio
     * (disciplina, superficie, techada). Pagina de 12 en 12.
     */
    public Page<ComplejoCard> buscar(List<Integer> disciplinas, List<TipoAdministracion> tipos, String provincia,
                                     Superficie superficie, Boolean techada, String orden, int pagina) {
        Specification<Complejo> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.isTrue(root.<Boolean>get("activo")));
            if (tipos != null && tipos.size() == 1) {
                ps.add(cb.equal(root.get("tipoAdministracion"), tipos.get(0)));
            }
            if (provincia != null && !provincia.isBlank()) {
                ps.add(cb.equal(root.get("provincia"), provincia));
            }
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Espacio> e = sub.from(Espacio.class);
            List<Predicate> pe = new ArrayList<>();
            pe.add(cb.equal(e.get("complejo"), root));
            pe.add(cb.isTrue(e.<Boolean>get("activo")));
            if (disciplinas != null && !disciplinas.isEmpty()) {
                pe.add(e.get("disciplina").get("id").in(disciplinas));
            }
            if (superficie != null) {
                pe.add(cb.equal(e.get("superficie"), superficie));
            }
            if (techada != null) {
                pe.add(cb.equal(e.<Boolean>get("techada"), techada));
            }
            sub.select(e.<Long>get("id")).where(pe.toArray(new Predicate[0]));
            ps.add(cb.exists(sub));
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Sort sort;
        if ("calificacion".equals(orden)) {
            sort = Sort.by(Sort.Order.desc("calificacion"), Sort.Order.asc("nombre"));
        } else if ("nombre".equals(orden)) {
            sort = Sort.by(Sort.Order.asc("nombre"));
        } else {
            sort = Sort.by(Sort.Order.desc("destacado"), Sort.Order.desc("calificacion"), Sort.Order.asc("nombre"));
        }
        Page<Complejo> page = complejoRepo.findAll(spec, PageRequest.of(Math.max(pagina, 0), tamanoPagina, sort));
        return page.map(this::aTarjeta);
    }

    public ComplejoCard aTarjeta(Complejo c) {
        List<Espacio> espacios = espacioRepo.findByComplejoIdAndActivoTrueOrderByNombreAsc(c.getId());
        LinkedHashSet<String> disciplinas = new LinkedHashSet<>();
        BigDecimal desde = null;
        for (Espacio e : espacios) {
            disciplinas.add(e.getDisciplina().getNombre());
            if (desde == null || e.getTarifaDiurna().compareTo(desde) < 0) {
                desde = e.getTarifaDiurna();
            }
        }
        String imagen = c.getImagenUrl();
        if ((imagen == null || imagen.isBlank()) && !espacios.isEmpty()) {
            imagen = espacios.get(0).getImagenUrl();
        }
        return new ComplejoCard(c, new ArrayList<>(disciplinas), desde, imagen);
    }

    public List<Disciplina> disciplinas() {
        return disciplinaRepo.findAllByOrderByOrdenAsc();
    }

    public List<String> provincias() {
        return complejoRepo.provinciasActivas();
    }

    public Complejo obtenerComplejo(Long id) {
        return complejoRepo.findById(id).filter(Complejo::isActivo)
                .orElseThrow(() -> new NegocioException("catalogo.error.complejo"));
    }

    public List<Espacio> espaciosActivos(Long complejoId) {
        return espacioRepo.findByComplejoIdAndActivoTrueOrderByNombreAsc(complejoId);
    }

    public Espacio obtenerEspacio(Long id) {
        return espacioRepo.findById(id).filter(Espacio::isActivo)
                .filter(e -> e.getComplejo().isActivo())
                .orElseThrow(() -> new NegocioException("catalogo.error.espacio"));
    }
}
