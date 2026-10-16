package br.com.fiap.cp5.controller;

import br.com.fiap.cp5.dto.PedidoForm;
import br.com.fiap.cp5.service.MercadoService;
import br.com.fiap.cp5.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final MercadoService mercadoService;

    public PedidoController(
            PedidoService pedidoService,
            MercadoService mercadoService) {

        this.pedidoService = pedidoService;
        this.mercadoService = mercadoService;
    }

    @GetMapping
    public String meusPedidos(Principal principal, Model model) {

        model.addAttribute(
                "pedidos",
                pedidoService.listarPedidosDoUsuario(
                        principal.getName()
                )
        );

        return "pedido/lista";
    }

    @GetMapping("/novo")
    public String novo(
            @RequestParam("produtoId") Long produtoId,
            Model model) {

        PedidoForm formulario = new PedidoForm();
        formulario.setProdutoId(produtoId);

        model.addAttribute("pedidoForm", formulario);
        model.addAttribute(
                "produto",
                mercadoService.buscarPorId(produtoId)
        );

        return "pedido/form";
    }

    @PostMapping
    public String criar(
            @Valid @ModelAttribute("pedidoForm") PedidoForm formulario,
            BindingResult resultado,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        if (resultado.hasFieldErrors("produtoId")) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Produto inválido. Selecione um produto na lista."
            );

            return "redirect:/mercado";
        }

        if (resultado.hasErrors()) {
            model.addAttribute(
                    "produto",
                    mercadoService.buscarPorId(formulario.getProdutoId())
            );

            return "pedido/form";
        }

        try {
            Long pedidoId = pedidoService.criarPedido(
                    principal.getName(),
                    formulario.getProdutoId(),
                    formulario.getQuantidade()
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Pedido #" + pedidoId
                            + " criado com sucesso! Aguardando pagamento."
            );

            return "redirect:/mercado";

        } catch (IllegalArgumentException exception) {
            model.addAttribute("erro", exception.getMessage());

            model.addAttribute(
                    "produto",
                    mercadoService.buscarPorId(formulario.getProdutoId())
            );

            return "pedido/form";
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String tratarErro(
            IllegalArgumentException exception,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "erro",
                exception.getMessage()
        );

        return "redirect:/mercado";
    }
}