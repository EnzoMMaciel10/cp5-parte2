package br.com.fiap.cp5.messaging;

public record PagamentoRespondido(
        Long pedidoId,
        String referencia,
        boolean aprovado,
        String protocoloBanco,
        String motivo) {
}
