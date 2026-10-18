package br.com.fiap.cp5.service;

import br.com.fiap.cp5.messaging.PagamentoRespondido;
import br.com.fiap.cp5.messaging.PagamentoSolicitado;
import br.com.fiap.cp5.model.EmailEnvio;
import br.com.fiap.cp5.model.ItemPedido;
import br.com.fiap.cp5.model.Mercado;
import br.com.fiap.cp5.model.Pedido;
import br.com.fiap.cp5.model.StatusPedido;
import br.com.fiap.cp5.repository.EmailEnvioRepository;
import br.com.fiap.cp5.repository.MercadoRepository;
import br.com.fiap.cp5.repository.PedidoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;
import java.util.UUID;

import static br.com.fiap.cp5.config.RabbitConfig.*;

@Service
public class PagamentoService {
    private final PedidoRepository pedidos;
    private final MercadoRepository produtos;
    private final EmailEnvioRepository emails;
    private final RabbitTemplate rabbit;
    private final EmailService emailService;

    public PagamentoService(PedidoRepository pedidos, MercadoRepository produtos,
                            EmailEnvioRepository emails, RabbitTemplate rabbit,
                            EmailService emailService) {
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.emails = emails;
        this.rabbit = rabbit;
        this.emailService = emailService;
    }

    @Transactional
    public void solicitar(Long pedidoId, String emailUsuario) {
        Pedido pedido = pedidos.findBloqueadoById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (!pedido.getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new IllegalArgumentException("Este pedido não pertence à sua conta.");
        }
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new IllegalArgumentException("Este pedido já foi processado.");
        }
        if (pedido.getReferenciaPagamento() == null) {
            pedido.setReferenciaPagamento(UUID.randomUUID().toString());
            pedidos.flush();
        }
        rabbit.convertAndSend(REQUISICOES_EXCHANGE, REQUISICAO_KEY,
                new PagamentoSolicitado(pedido.getId(), pedido.getReferenciaPagamento(),
                        pedido.getTotal(), pedido.isSimularRecusa()));
    }

    @Transactional
    public void aplicar(PagamentoRespondido resposta) {
        Pedido pedido = pedidos.findBloqueadoById(resposta.pedidoId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (!Objects.equals(pedido.getReferenciaPagamento(), resposta.referencia())) {
            throw new IllegalArgumentException("Referência do pagamento inválida.");
        }
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            return; // resposta duplicada: não descontar estoque nem reenviar e-mail
        }
        pedido.setProtocoloBanco(resposta.protocoloBanco());
        if (!resposta.aprovado()) {
            pedido.setStatus(StatusPedido.RECUSADO);
            pedido.setMotivoRecusa(resposta.motivo());
            return;
        }
        for (ItemPedido item : pedido.getItens()) {
            Mercado produto = produtos.findBloqueadoById(item.getProdutoId())
                    .orElseThrow(() -> new IllegalStateException("Produto do pedido não existe mais."));
            if (produto.getEstoque() < item.getQuantidade()) {
                pedido.setStatus(StatusPedido.RECUSADO);
                pedido.setMotivoRecusa("Estoque insuficiente no momento da confirmação.");
                return;
            }
            produto.setEstoque(produto.getEstoque() - item.getQuantidade());
        }
        pedido.setStatus(StatusPedido.PAGO);
        EmailEnvio registro = new EmailEnvio();
        registro.setPedido(pedido);
        registro.setDestinatario(pedido.getUsuario().getEmail());
        emails.save(registro);
        Long id = pedido.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.enviarConfirmacao(id);
            }
        });
    }
}
