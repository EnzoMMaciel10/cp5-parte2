package br.com.fiap.banco;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/banco-tranquilo")
public class BancoTranquiloController {
    private final String chave;

    public BancoTranquiloController(@Value("${banco-tranquilo.api-key}") String chave) {
        this.chave = chave;
    }

    @PostMapping("/transacoes")
    public TransacaoResponse transacionar(
            @RequestHeader("X-Banco-Api-Key") String chaveRecebida,
            @Valid @RequestBody TransacaoRequest pedido) {
        if (!chave.equals(chaveRecebida)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chave do banco inválida.");
        }
        boolean aprovado = !pedido.simularRecusa();
        return new TransacaoResponse(pedido.referencia(),
                aprovado ? "APROVADO" : "RECUSADO",
                UUID.nameUUIDFromBytes(pedido.referencia().getBytes(StandardCharsets.UTF_8)).toString(),
                aprovado ? null : "Transação recusada na simulação do Banco Tranquilo.");
    }
}
