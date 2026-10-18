package br.com.fiap.cp5.service;

import br.com.fiap.cp5.model.Pedido;
import br.com.fiap.cp5.repository.EmailEnvioRepository;
import br.com.fiap.cp5.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class ConsultaCompraService {
    private final PedidoRepository pedidos;
    private final EmailEnvioRepository emails;

    public ConsultaCompraService(PedidoRepository pedidos, EmailEnvioRepository emails) {
        this.pedidos = pedidos;
        this.emails = emails;
    }

    @Transactional(readOnly = true)
    public Resultado consultar(Long id, String emailUsuario) {
        Pedido pedido = pedidos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!pedido.getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String statusEmail = emails.findByPedido_Id(id)
                .map(registro -> registro.getStatus().name()).orElse(null);
        return new Resultado(pedido.getId(), pedido.getStatus().name(), pedido.getTotal(),
                pedido.getProtocoloBanco(), pedido.getMotivoRecusa(), statusEmail);
    }

    public record Resultado(Long pedidoId, String status, BigDecimal total,
                           String protocoloBanco, String motivoRecusa, String statusEmail) {
    }
}
