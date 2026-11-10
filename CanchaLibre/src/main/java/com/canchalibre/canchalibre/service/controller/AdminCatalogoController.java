package com.canchalibre.canchalibre.controller;

import com.canchalibre.canchalibre.config.Mensajes;
import com.canchalibre.canchalibre.domain.CategoriaInsumo;
import com.canchalibre.canchalibre.domain.Complejo;
import com.canchalibre.canchalibre.domain.Disciplina;
import com.canchalibre.canchalibre.domain.Espacio;
import com.canchalibre.canchalibre.domain.Insumo;
import com.canchalibre.canchalibre.domain.Superficie;
import com.canchalibre.canchalibre.domain.TipoTarifa;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.dto.BloqueoForm;
import com.canchalibre.canchalibre.dto.ComplejoForm;
import com.canchalibre.canchalibre.dto.EspacioForm;
import com.canchalibre.canchalibre.dto.InsumoForm;
import com.canchalibre.canchalibre.dto.TarifaForm;
import com.canchalibre.canchalibre.security.SesionUsuario;
import com.canchalibre.canchalibre.service.AdminService;
import com.canchalibre.canchalibre.service.NegocioException;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** CRUD del administrador sobre su complejo: complejo, espacios, tarifas (HU-08), insumos y bloqueos operativos. */
@Controller
public class AdminCatalogoController {

    private final AdminService adminService;
    private final SesionUsuario sesion;
    private final Mensajes mensajes;

    public AdminCatalogoController(AdminService adminService, SesionUsuario sesion, Mensajes mensajes) {
        this.adminService = adminService;
        this.sesion = sesion;
        this.mensajes = mensajes;
    }

    // ------------------------------------------------------------------ complejo

    @GetMapping("/admin/complejo")
    public String complejo(Principal principal, Model model) {
        Complejo c = adminService.complejoDe(sesion.actual(principal));
        ComplejoForm f = new ComplejoForm();
        f.setNombre(c.getNombre());
        f.setDescripcion(c.getDescripcion());
        f.setDireccion(c.getDireccion());
        f.setTelefono(c.getTelefono());
        f.setHoraApertura(c.getHoraApertura());
        f.setHoraCierre(c.getHoraCierre());
        f.setHoraInicioNocturno(c.getHoraInicioNocturno());
        f.setParqueo(c.isParqueo());
        f.setVestidores(c.isVestidores());
        f.setSoda(c.isSoda());
        f.setIluminacionNocturna(c.isIluminacionNocturna());
        f.setBanco(c.getBanco());
        f.setTitular(c.getTitular());
        f.setIban(c.getIban());
        model.addAttribute("form", f);
        model.addAttribute("complejo", c);
        return "admin/complejo";
    }

    @PostMapping("/admin/complejo")
    public String guardarComplejo(@Valid @ModelAttribute("form") ComplejoForm form, BindingResult resultado,
                                  Principal principal, Model model, RedirectAttributes ra) {
        Usuario admin = sesion.actual(principal);
        if (!resultado.hasErrors()) {
            try {
                adminService.actualizarComplejo(admin, form);
                ra.addFlashAttribute("exito", mensajes.get("admin.exito.guardado"));
                return "redirect:/admin/complejo";
            } catch (NegocioException ex) {
                model.addAttribute("error", mensajes.de(ex));
            }
        }
        model.addAttribute("complejo", adminService.complejoDe(admin));
        return "admin/complejo";
    }

    // ------------------------------------------------------------------ espacios

    @GetMapping("/admin/espacios")
    public String espacios(Principal principal, Model model) {
        model.addAttribute("espacios", adminService.listarEspacios(sesion.actual(principal)));
        return "admin/espacios";
    }

    @GetMapping("/admin/espacios/nuevo")
    public String espacioNuevo(Model model) {
        model.addAttribute("form", new EspacioForm());
        cargarCatalogosEspacio(model);
        return "admin/espacio-form";
    }

    @GetMapping("/admin/espacios/{id}/editar")
    public String espacioEditar(@PathVariable Long id, Principal principal, Model model) {
        Espacio e = adminService.obtenerEspacio(sesion.actual(principal), id);
        EspacioForm f = new EspacioForm();
        f.setId(e.getId());
        f.setDisciplinaId(e.getDisciplina().getId());
        f.setNombre(e.getNombre());
        f.setDescripcion(e.getDescripcion());
        f.setSuperficie(e.getSuperficie());
        f.setTechada(e.isTechada());
        f.setCapacidad(e.getCapacidad());
        f.setMedidas(e.getMedidas());
        f.setIluminacion(e.isIluminacion());
        f.setReglamento(e.getReglamento());
        f.setTipoTarifa(e.getTipoTarifa());
        f.setTarifaDiurna(e.getTarifaDiurna());
        f.setRecargoNocturno(e.getRecargoNocturno());
        f.setDepositoGarantia(e.getDepositoGarantia());
        f.setActivo(e.isActivo());
        model.addAttribute("form", f);
        model.addAttribute("espacio", e);
        cargarCatalogosEspacio(model);
        return "admin/espacio-form";
    }

    @PostMapping("/admin/espacios")
    public String espacioGuardar(@Valid @ModelAttribute("form") EspacioForm form, BindingResult resultado,
                                 Principal principal, Model model, RedirectAttributes ra) {
        Usuario admin = sesion.actual(principal);
        if (!resultado.hasErrors()) {
            try {
                adminService.guardarEspacio(admin, form);
                ra.addFlashAttribute("exito", mensajes.get("admin.exito.guardado"));
                return "redirect:/admin/espacios";
            } catch (NegocioException ex) {
                model.addAttribute("error", mensajes.de(ex));
            }
        }
        cargarCatalogosEspacio(model);
        return "admin/espacio-form";
    }

    @PostMapping("/admin/espacios/{id}/alternar")
    public String espacioAlternar(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            adminService.alternarEspacio(sesion.actual(principal), id);
            ra.addFlashAttribute("exito", mensajes.get("admin.exito.guardado"));
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
        }
        return "redirect:/admin/espacios";
    }

    private void cargarCatalogosEspacio(Model model) {
        model.addAttribute("disciplinas", adminService.disciplinas());
        model.addAttribute("superficies", Superficie.values());
        model.addAttribute("tiposTarifa", TipoTarifa.values());
    }

    // ------------------------------------------------------------------ tarifas (HU-08)

    @GetMapping("/admin/tarifas")
    public String tarifas(Principal principal, Model model) {
        model.addAttribute("espacios", adminService.listarEspacios(sesion.actual(principal)));
        model.addAttribute("tiposTarifa", TipoTarifa.values());
        return "admin/tarifas";
    }

    @PostMapping("/admin/tarifas/{id}")
    public String tarifaGuardar(@PathVariable Long id, @Valid @ModelAttribute TarifaForm form, BindingResult resultado,
                                Principal principal, RedirectAttributes ra) {
        if (resultado.hasErrors()) {
            ra.addFlashAttribute("error", mensajes.get("tarifa.error.invalida"));
            return "redirect:/admin/tarifas";
        }
        try {
            adminService.actualizarTarifa(sesion.actual(principal), id, form);
            ra.addFlashAttribute("exito", mensajes.get("admin.exito.guardado"));
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
        }
        return "redirect:/admin/tarifas";
    }

    // ------------------------------------------------------------------ insumos

    @GetMapping("/admin/insumos")
    public String insumos(Principal principal, Model model) {
        model.addAttribute("insumos", adminService.listarInsumos(sesion.actual(principal)));
        return "admin/insumos";
    }

    @GetMapping("/admin/insumos/nuevo")
    public String insumoNuevo(Model model) {
        model.addAttribute("form", new InsumoForm());
        cargarCatalogosInsumo(model);
        return "admin/insumo-form";
    }

    @GetMapping("/admin/insumos/{id}/editar")
    public String insumoEditar(@PathVariable Long id, Principal principal, Model model) {
        Insumo i = adminService.obtenerInsumo(sesion.actual(principal), id);
        InsumoForm f = new InsumoForm();
        f.setId(i.getId());
        f.setNombre(i.getNombre());
        f.setDescripcion(i.getDescripcion());
        f.setCategoria(i.getCategoria());
        f.setPrecio(i.getPrecio());
        f.setUnidad(i.getUnidad());
        f.setStock(i.getStock());
        f.setActivo(i.isActivo());
        f.setDisciplinaIds(i.getDisciplinas().stream().map(Disciplina::getId).toList());
        model.addAttribute("form", f);
        cargarCatalogosInsumo(model);
        return "admin/insumo-form";
    }

    @PostMapping("/admin/insumos")
    public String insumoGuardar(@Valid @ModelAttribute("form") InsumoForm form, BindingResult resultado,
                                Principal principal, Model model, RedirectAttributes ra) {
        if (!resultado.hasErrors()) {
            try {
                adminService.guardarInsumo(sesion.actual(principal), form);
                ra.addFlashAttribute("exito", mensajes.get("admin.exito.guardado"));
                return "redirect:/admin/insumos";
            } catch (NegocioException ex) {
                model.addAttribute("error", mensajes.de(ex));
            }
        }
        cargarCatalogosInsumo(model);
        return "admin/insumo-form";
    }

    @PostMapping("/admin/insumos/{id}/alternar")
    public String insumoAlternar(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            adminService.alternarInsumo(sesion.actual(principal), id);
            ra.addFlashAttribute("exito", mensajes.get("admin.exito.guardado"));
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
        }
        return "redirect:/admin/insumos";
    }

    private void cargarCatalogosInsumo(Model model) {
        model.addAttribute("disciplinas", adminService.disciplinas());
        model.addAttribute("categorias", CategoriaInsumo.values());
    }

    // ------------------------------------------------------------------ bloqueos y mantenimiento

    @GetMapping("/admin/bloqueos")
    public String bloqueos(Principal principal, Model model) {
        BloqueoForm f = new BloqueoForm();
        f.setFecha(LocalDate.now().plusDays(1));
        model.addAttribute("form", f);
        cargarBloqueos(sesion.actual(principal), model);
        return "admin/bloqueos";
    }

    @PostMapping("/admin/bloqueos")
    public String bloqueoCrear(@Valid @ModelAttribute("form") BloqueoForm form, BindingResult resultado,
                               Principal principal, Model model, RedirectAttributes ra) {
        Usuario admin = sesion.actual(principal);
        if (!resultado.hasErrors()) {
            try {
                int n = adminService.crearBloqueo(admin, form);
                ra.addFlashAttribute("exito", mensajes.get("bloqueo.exito", n));
                return "redirect:/admin/bloqueos";
            } catch (NegocioException ex) {
                model.addAttribute("error", mensajes.de(ex));
            }
        }
        cargarBloqueos(admin, model);
        return "admin/bloqueos";
    }

    @PostMapping("/admin/bloqueos/{id}/eliminar")
    public String bloqueoEliminar(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            adminService.eliminarBloqueo(sesion.actual(principal), id);
            ra.addFlashAttribute("exito", mensajes.get("bloqueo.eliminado"));
        } catch (NegocioException ex) {
            ra.addFlashAttribute("error", mensajes.de(ex));
        }
        return "redirect:/admin/bloqueos";
    }

    private void cargarBloqueos(Usuario admin, Model model) {
        List<Espacio> espacios = adminService.listarEspacios(admin);
        model.addAttribute("espacios", espacios);
        model.addAttribute("bloqueos", adminService.listarBloqueos(admin));
    }
}
