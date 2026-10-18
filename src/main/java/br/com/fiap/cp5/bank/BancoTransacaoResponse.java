package br.com.fiap.cp5.bank;

public record BancoTransacaoResponse(
        String referencia,
        String status,
        String protocolo,
        String motivo) {
}
