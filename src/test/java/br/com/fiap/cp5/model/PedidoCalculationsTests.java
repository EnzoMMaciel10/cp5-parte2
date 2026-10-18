package br.com.fiap.cp5.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoCalculationsTests {
    @Test
    void calculaFaturamentoCustoELucroComDoisItens() {
        Pedido pedido = new Pedido();
        adicionar(pedido, 2, "25.90", "18.00");
        adicionar(pedido, 3, "10.00", "4.50");

        assertEquals(new BigDecimal("81.80"), pedido.getTotal());
        assertEquals(new BigDecimal("49.50"), pedido.getCustoTotal());
        assertEquals(new BigDecimal("32.30"), pedido.getTotal().subtract(pedido.getCustoTotal()));
    }

    private void adicionar(Pedido pedido, int quantidade, String preco, String custo) {
        ItemPedido item = new ItemPedido();
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(new BigDecimal(preco));
        item.setCustoUnitario(new BigDecimal(custo));
        pedido.adicionarItem(item);
    }
}
