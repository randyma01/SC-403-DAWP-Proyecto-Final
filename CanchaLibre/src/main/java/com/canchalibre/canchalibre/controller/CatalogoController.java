package com.canchalibre.canchalibre.controller;

import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.domain.ImagenEspacio;
import com.canchalibre.canchalibre.domain.Superficie;
import com.canchalibre.canchalibre.domain.TipoAdministracion;
import com.canchalibre.canchalibre.dto.ComplejoCard;
import com.canchalibre.canchalibre.service.CatalogoService;
import com.canchalibre.canchalibre.service.CotizacionService;
import com.canchalibre.canchalibre.service.NegocioException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Exploracion publica: catalogo con filtros (HU-01, HU-02) y ficha del complejo/espacio (HU-03, HU-05). */
@Controller
public class CatalogoController {

    private static final String PLACEHOLDER = "/img/placeholders/futbol-1.svg";

    private final CatalogoService catalogo;
    private final CotizacionService cotizacionService;

    public CatalogoController(CatalogoService catalogo, CotizacionService cotizacionService) {
        this.catalogo = catalogo;
        this.cotizacionService = cotizacionService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) List<Integer> disciplina,
                        @RequestParam(required = false) List<TipoAdministracion> tipo,
                        @RequestParam(required = false) String provincia,
                        @RequestParam(required = false) Superficie superficie,
                        @RequestParam(required = false) String techada,
                        @RequestParam(defaultValue = "relevantes") String orden,
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                        Model model) {
        cargarResultados(model, disciplina, tipo, provincia, superficie, techada, orden, pagina, fecha);
        model.addAttribute("disciplinas", catalogo.disciplinas());
        model.addAttribute("provincias", catalogo.provincias());
        model.addAttribute("superficies", Superficie.values());
        model.addAttribute("hoy", LocalDate.now());
        return "index";
    }

    /** HU-02: devuelve solo el fragmento de resultados para actualizar la lista sin recargar la pagina. */
    @GetMapping("/complejos/resultados")
    public String resultados(@RequestParam(required = false) List<Integer> disciplina,
                             @RequestParam(required = false) List<TipoAdministracion> tipo,
                             @RequestParam(required = false) String provincia,
                             @RequestParam(required = false) Superficie superficie,
                             @RequestParam(required = false) String techada,
                             @RequestParam(defaultValue = "relevantes") String orden,
                             @RequestParam(defaultValue = "0") int pagina,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                             Model model) {
        cargarResultados(model, disciplina, tipo, provincia, superficie, techada, orden, pagina, fecha);
        return "complejo/resultados :: resultados";
    }

    private void cargarResultados(Model model, List<Integer> disciplina, List<TipoAdministracion> tipo, String provincia,
                                  Superficie superficie, String techada, String orden, int pagina, LocalDate fecha) {
        Boolean techadaFiltro = "si".equals(techada) ? Boolean.TRUE : ("no".equals(techada) ? Boolean.FALSE : null);
        Page<ComplejoCard> page = catalogo.buscar(disciplina, tipo, provincia, superficie, techadaFiltro, orden, pagina);
        model.addAttribute("pagina", page);
        model.addAttribute("orden", orden);
        model.addAttribute("fecha", fecha);
        model.addAttribute("selDisciplinas", disciplina == null ? new HashSet<Integer>() : new HashSet<>(disciplina));
        model.addAttribute("selTipos", tipo == null ? new HashSet<TipoAdministracion>() : new HashSet<>(tipo));
        model.addAttribute("selProvincia", provincia == null ? "" : provincia);
        model.addAttribute("selSuperficie", superficie == null ? "" : superficie.name());
        model.addAttribute("selTecho", techada == null ? "" : techada);
    }

    @GetMapping("/complejos/{id}")
    public String detalle(@PathVariable Long id,
                          @RequestParam(required = false) Long espacio,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                          Model model) {
        Complejo complejo = catalogo.obtenerComplejo(id);
        List<Espacio> espacios = catalogo.espaciosActivos(id);
        if (espacios.isEmpty()) {
            throw new NegocioException("catalogo.error.espacio");
        }
        Espacio seleccionado = espacios.stream().filter(e -> e.getId().equals(espacio)).findFirst().orElse(espacios.get(0));
        List<String> imagenes = seleccionado.getImagenes().stream().map(ImagenEspacio::getUrl).toList();
        if (imagenes.isEmpty()) {
            imagenes = List.of(seleccionado.getImagenUrl() != null ? seleccionado.getImagenUrl() : PLACEHOLDER);
        }
        LocalDate fechaRef = fecha != null && !fecha.isBefore(LocalDate.now()) ? fecha : LocalDate.now();
        model.addAttribute("complejo", complejo);
        model.addAttribute("espacios", espacios);
        model.addAttribute("espacio", seleccionado);
        model.addAttribute("imagenes", imagenes);
        model.addAttribute("fecha", fecha);
        model.addAttribute("insumos", cotizacionService.insumosDisponibles(seleccionado, fechaRef, LocalTime.of(12, 0)));
        return "complejo/detalle";
    }
}
