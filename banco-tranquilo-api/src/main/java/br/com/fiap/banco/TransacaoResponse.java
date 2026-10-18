package br.com.fiap.banco;

public record TransacaoResponse(String referencia, String status,
                                String protocolo, String motivo) {
}
