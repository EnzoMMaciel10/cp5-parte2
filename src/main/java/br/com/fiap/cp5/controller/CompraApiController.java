package br.com.fiap.cp5.controller;

import br.com.fiap.cp5.dto.CompraRequest;
import br.com.fiap.cp5.service.PagamentoService;
import br.com.fiap.cp5.service.PedidoService;
import br.com.fiap.cp5.service.ConsultaCompraService;
import jakarta.validation.Valid;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/compras")
public class CompraApiController {
    private final PedidoService pedidos;
    private final PagamentoService pagamentos;
    private final ConsultaCompraService consultas;

    public CompraApiController(PedidoService pedidos, PagamentoService pagamentos,
                               ConsultaCompraService consultas) {
        this.pedidos = pedidos;
        this.pagamentos = pagamentos;
        this.consultas = consultas;
    }

    @GetMapping("/{id}")
    public ConsultaCompraService.Resultado consultar(@PathVariable Long id, Principal principal) {
        return consultas.consultar(id, principal.getName());
    }

    @PostMapping
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody CompraRequest request,
                                                   Principal principal) {
        Long id = pedidos.criarPedido(principal.getName(), request.produtoId(),
                request.quantidade(), request.simularRecusa());
        try {
            pagamentos.solicitar(id, principal.getName());
            return ResponseEntity.accepted().body(new CompraResponse(id, "ENFILEIRADO",
                    "Consulte o status em /pedidos."));
        } catch (AmqpException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new CompraResponse(id, "PENDENTE",
                            "Pedido salvo. Reenvie o pagamento em Meus pedidos."));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> erro(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    public record CompraResponse(Long pedidoId, String status, String mensagem) {
    }
}
