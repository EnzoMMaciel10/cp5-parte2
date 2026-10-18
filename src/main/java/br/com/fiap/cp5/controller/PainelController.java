package br.com.fiap.cp5.controller;

import br.com.fiap.cp5.service.PainelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelController {
    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping("/painel")
    public String painel(Model model) {
        model.addAttribute("resumo", painelService.resumo());
        return "painel";
    }
}
