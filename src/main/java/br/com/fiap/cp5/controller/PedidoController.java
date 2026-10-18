package br.com.fiap.cp5.controller;

import br.com.fiap.cp5.dto.PedidoForm;
import br.com.fiap.cp5.service.MercadoService;
import br.com.fiap.cp5.service.PedidoService;
import br.com.fiap.cp5.service.PagamentoService;
import org.springframework.amqp.AmqpException;
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
    private final PagamentoService pagamentoService;

    public PedidoController(
            PedidoService pedidoService,
            MercadoService mercadoService,
            PagamentoService pagamentoService) {

        this.pedidoService = pedidoService;
        this.mercadoService = mercadoService;
        this.pagamentoService = pagamentoService;
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
                    formulario.getQuantidade(),
                    formulario.isSimularRecusa()
            );

            try {
                pagamentoService.solicitar(pedidoId, principal.getName());
            } catch (AmqpException exception) {
                redirectAttributes.addFlashAttribute("erro",
                        "Pedido #" + pedidoId + " criado, mas o RabbitMQ está indisponível. "
                        + "Reenvie o pagamento em Meus pedidos.");
                return "redirect:/pedidos";
            }

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Pedido #" + pedidoId
                    + " criado! Pagamento enviado para processamento."
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

    @PostMapping("/{id}/pagar")
    public String reenviarPagamento(@org.springframework.web.bind.annotation.PathVariable Long id,
                                    Principal principal, RedirectAttributes redirectAttributes) {
        try {
            pagamentoService.solicitar(id, principal.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Pagamento reenviado para processamento.");
        } catch (IllegalArgumentException | AmqpException exception) {
            redirectAttributes.addFlashAttribute("erro", exception instanceof AmqpException
                    ? "RabbitMQ indisponível. Tente novamente." : exception.getMessage());
        }
        return "redirect:/pedidos";
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
