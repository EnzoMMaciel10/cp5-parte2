package br.com.fiap.cp5.messaging;

import java.math.BigDecimal;

public record PagamentoSolicitado(
        Long pedidoId,
        String referencia,
        BigDecimal valor,
        boolean simularRecusa) {
}
