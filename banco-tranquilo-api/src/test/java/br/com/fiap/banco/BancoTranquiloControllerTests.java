package br.com.fiap.banco;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BancoTranquiloControllerTests {
    private final BancoTranquiloController banco = new BancoTranquiloController("chave-teste");

    @Test
    void aprovaEDevolveProtocolo() {
        TransacaoResponse resposta = banco.transacionar("chave-teste",
                new TransacaoRequest("pedido-1", new BigDecimal("25.90"), false));
        assertEquals("APROVADO", resposta.status());
        assertEquals("pedido-1", resposta.referencia());
        assertNotNull(resposta.protocolo());
    }

    @Test
    void recusaEProtegeChave() {
        TransacaoRequest pedido = new TransacaoRequest("pedido-2", new BigDecimal("10.00"), true);
        assertEquals("RECUSADO", banco.transacionar("chave-teste", pedido).status());
        assertThrows(ResponseStatusException.class,
                () -> banco.transacionar("chave-errada", pedido));
    }
}
