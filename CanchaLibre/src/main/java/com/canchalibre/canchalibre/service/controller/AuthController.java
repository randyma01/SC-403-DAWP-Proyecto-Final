package com.canchalibre.canchalibre.controller;

import com.canchalibre.canchalibre.config.Mensajes;
import com.canchalibre.canchalibre.dto.RegistroForm;
import com.canchalibre.canchalibre.service.NegocioException;
import com.canchalibre.canchalibre.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Inicio de sesion y registro de deportistas. */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final Mensajes mensajes;

    public AuthController(UsuarioService usuarioService, Mensajes mensajes) {
        this.usuarioService = usuarioService;
        this.mensajes = mensajes;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("form", new RegistroForm());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form, BindingResult resultado,
                            Model model, RedirectAttributes ra) {
        if (resultado.hasErrors()) {
            return "auth/registro";
        }
        try {
            usuarioService.registrar(form);
        } catch (NegocioException ex) {
            model.addAttribute("error", mensajes.de(ex));
            return "auth/registro";
        }
        ra.addFlashAttribute("exito", mensajes.get("registro.exito"));
        return "redirect:/login";
    }
}
