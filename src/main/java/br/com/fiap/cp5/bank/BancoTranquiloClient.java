package br.com.fiap.cp5.bank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BancoTranquiloClient {
    private final RestClient client;
    private final String chave;

    public BancoTranquiloClient(
            @Value("${banco-tranquilo.base-url}") String baseUrl,
            @Value("${banco-tranquilo.api-key}") String chave) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.chave = chave;
    }

    public BancoTransacaoResponse transacionar(BancoTransacaoRequest request) {
        BancoTransacaoResponse response = client.post()
                .uri("/api/banco-tranquilo/transacoes")
                .header("X-Banco-Api-Key", chave)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BancoTransacaoResponse.class);
        if (response == null || !request.referencia().equals(response.referencia())) {
            throw new IllegalStateException("Resposta inválida do Banco Tranquilo.");
        }
        return response;
    }
}
