package br.com.fiap.cp5.messaging;

import br.com.fiap.cp5.bank.BancoTransacaoRequest;
import br.com.fiap.cp5.bank.BancoTransacaoResponse;
import br.com.fiap.cp5.bank.BancoTranquiloClient;
import br.com.fiap.cp5.service.PagamentoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static br.com.fiap.cp5.config.RabbitConfig.*;

@Component
public class PagamentoListener {
    private final BancoTranquiloClient banco;
    private final RabbitTemplate rabbit;
    private final PagamentoService pagamentos;

    public PagamentoListener(BancoTranquiloClient banco, RabbitTemplate rabbit,
                             PagamentoService pagamentos) {
        this.banco = banco;
        this.rabbit = rabbit;
        this.pagamentos = pagamentos;
    }

    @RabbitListener(queues = REQUISICOES_QUEUE)
    public void processarSolicitacao(PagamentoSolicitado mensagem) {
        BancoTransacaoResponse resposta = banco.transacionar(
                new BancoTransacaoRequest(mensagem.referencia(), mensagem.valor(),
                        mensagem.simularRecusa()));
        if (!"APROVADO".equals(resposta.status()) && !"RECUSADO".equals(resposta.status())) {
            throw new IllegalStateException("Status desconhecido do Banco Tranquilo.");
        }
        rabbit.convertAndSend(RESPOSTAS_EXCHANGE, RESPOSTA_KEY,
                new PagamentoRespondido(mensagem.pedidoId(), resposta.referencia(),
                        "APROVADO".equals(resposta.status()), resposta.protocolo(), resposta.motivo()));
    }

    @RabbitListener(queues = RESPOSTAS_QUEUE)
    public void processarResposta(PagamentoRespondido mensagem) {
        pagamentos.aplicar(mensagem);
    }
}
