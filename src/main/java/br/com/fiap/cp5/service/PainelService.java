package br.com.fiap.cp5.service;

import br.com.fiap.cp5.model.ItemPedido;
import br.com.fiap.cp5.model.Mercado;
import br.com.fiap.cp5.model.Pedido;
import br.com.fiap.cp5.model.StatusEmail;
import br.com.fiap.cp5.model.StatusPedido;
import br.com.fiap.cp5.repository.EmailEnvioRepository;
import br.com.fiap.cp5.repository.MercadoRepository;
import br.com.fiap.cp5.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PainelService {
    private final PedidoRepository pedidos;
    private final MercadoRepository produtos;
    private final EmailEnvioRepository emails;

    public PainelService(PedidoRepository pedidos, MercadoRepository produtos,
                         EmailEnvioRepository emails) {
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.emails = emails;
    }

    @Transactional(readOnly = true)
    public Resumo resumo() {
        List<Pedido> todos = pedidos.findAllByOrderByCriadoEmDesc();
        Map<Long, LinhaMutavel> linhas = new LinkedHashMap<>();
        for (Mercado produto : produtos.findAll()) {
            linhas.put(produto.getId(), new LinhaMutavel(produto.getNome(), produto.getEstoque()));
        }
        long aprovados = 0;
        long recusados = 0;
        BigDecimal faturamento = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;
        for (Pedido pedido : todos) {
            if (pedido.getStatus() == StatusPedido.RECUSADO) {
                recusados++;
            }
            if (pedido.getStatus() != StatusPedido.PAGO) {
                continue;
            }
            aprovados++;
            faturamento = faturamento.add(pedido.getTotal());
            custo = custo.add(pedido.getCustoTotal());
            for (ItemPedido item : pedido.getItens()) {
                LinhaMutavel linha = linhas.computeIfAbsent(item.getProdutoId(),
                        id -> new LinhaMutavel(item.getNomeProduto() + " (removido)", 0));
                linha.vendidos += item.getQuantidade();
                linha.receita = linha.receita.add(item.getSubtotal());
                linha.custo = linha.custo.add(item.getSubtotalCusto());
            }
        }
        int maxVendas = linhas.values().stream().mapToInt(l -> l.vendidos).max().orElse(0);
        int maxEstoque = linhas.values().stream().mapToInt(l -> l.estoque).max().orElse(0);
        BigDecimal maxLucro = linhas.values().stream()
                .map(l -> l.receita.subtract(l.custo))
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        List<Linha> grafico = new ArrayList<>();
        for (LinhaMutavel linha : linhas.values()) {
            grafico.add(new Linha(linha.nome, linha.vendidos, linha.estoque,
                    linha.receita, linha.receita.subtract(linha.custo),
                    porcentagem(linha.vendidos, maxVendas), porcentagem(linha.estoque, maxEstoque),
                    porcentagem(linha.receita.subtract(linha.custo), maxLucro)));
        }
        grafico.sort(Comparator.comparingInt(Linha::vendidos).reversed());
        return new Resumo(todos.size(), aprovados, recusados, faturamento,
                faturamento.subtract(custo), emails.countByStatus(StatusEmail.ENVIADO),
                emails.countByStatus(StatusEmail.PENDENTE),
                emails.countByStatus(StatusEmail.FALHA), grafico);
    }

    private int porcentagem(int valor, int maximo) {
        if (maximo == 0) return 0;
        return BigDecimal.valueOf(valor).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(maximo), 0, RoundingMode.HALF_UP).intValue();
    }

    private int porcentagem(BigDecimal valor, BigDecimal maximo) {
        if (maximo.signum() <= 0 || valor.signum() <= 0) return 0;
        return valor.multiply(BigDecimal.valueOf(100))
                .divide(maximo, 0, RoundingMode.HALF_UP).intValue();
    }

    private static class LinhaMutavel {
        String nome;
        int estoque;
        int vendidos;
        BigDecimal receita = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;

        LinhaMutavel(String nome, int estoque) {
            this.nome = nome;
            this.estoque = estoque;
        }
    }

    public record Resumo(long pedidos, long aprovados, long recusados,
                         BigDecimal faturamento, BigDecimal lucro,
                         long emailsEnviados, long emailsPendentes, long emailsFalhos,
                         List<Linha> produtos) {
    }

    public record Linha(String nome, int vendidos, int estoque,
                        BigDecimal receita, BigDecimal lucro,
                        int vendasPercentual, int estoquePercentual, int lucroPercentual) {
    }
}
