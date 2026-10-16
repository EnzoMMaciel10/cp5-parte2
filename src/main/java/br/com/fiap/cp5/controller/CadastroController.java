package br.com.fiap.cp5.controller;

import br.com.fiap.cp5.dto.CadastroUsuarioForm;
import br.com.fiap.cp5.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CadastroController {

    private final UsuarioService usuarioService;

    public CadastroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/cadastro")
    public String formulario(Model model) {
        model.addAttribute("cadastro", new CadastroUsuarioForm());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @Valid @ModelAttribute("cadastro") CadastroUsuarioForm cadastro,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            cadastro.setSenha(null);
            return "cadastro";
        }

        try {
            usuarioService.cadastrar(
                    cadastro.getNome(),
                    cadastro.getEmail(),
                    cadastro.getSenha()
            );
        } catch (IllegalArgumentException exception) {
            cadastro.setSenha(null);
            model.addAttribute("erro", exception.getMessage());
            return "cadastro";
        } catch (DataIntegrityViolationException exception) {
            cadastro.setSenha(null);
            model.addAttribute(
                    "erro",
                    "Não foi possível concluir o cadastro. "
                            + "O e-mail pode já estar cadastrado."
            );
            return "cadastro";
        }

        return "redirect:/login?cadastro";
    }
}