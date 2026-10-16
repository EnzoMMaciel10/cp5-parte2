package br.com.fiap.cp5.service;

import br.com.fiap.cp5.model.ItemPedido;
import br.com.fiap.cp5.model.Mercado;
import br.com.fiap.cp5.model.Pedido;
import br.com.fiap.cp5.model.StatusPedido;
import br.com.fiap.cp5.model.Usuario;
import br.com.fiap.cp5.repository.MercadoRepository;
import br.com.fiap.cp5.repository.PedidoRepository;
import br.com.fiap.cp5.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MercadoRepository mercadoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            MercadoRepository mercadoRepository,
            UsuarioRepository usuarioRepository) {

        this.pedidoRepository = pedidoRepository;
        this.mercadoRepository = mercadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosDoUsuario(String emailUsuario) {

        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "É necessário estar conectado para consultar os pedidos."
            );
        }

        String emailNormalizado = emailUsuario
                .trim()
                .toLowerCase(Locale.ROOT);

        return pedidoRepository
                .findByUsuario_EmailOrderByCriadoEmDesc(emailNormalizado);
    }

    @Transactional
    public Long criarPedido(
            String emailUsuario,
            Long produtoId,
            Integer quantidade) {

        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "É necessário estar conectado para fazer um pedido."
            );
        }

        if (produtoId == null) {
            throw new IllegalArgumentException(
                    "Informe o produto desejado."
            );
        }

        if (quantidade == null || quantidade < 1) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser pelo menos 1."
            );
        }

        String emailNormalizado = emailUsuario
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByEmail(emailNormalizado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."
                ));

        Mercado produto = mercadoRepository
                .findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produto não encontrado."
                ));

        if (produto.getEstoque() == null
                || produto.getEstoque() < quantidade) {

            throw new IllegalArgumentException(
                    "Estoque insuficiente para a quantidade solicitada."
            );
        }

        if (produto.getPreco() == null
                || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "O produto está com o preço de venda inválido."
            );
        }

        if (produto.getCusto() == null
                || produto.getCusto().compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "O produto está com o custo de aquisição inválido."
            );
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        ItemPedido item = new ItemPedido();
        item.setProdutoId(produto.getId());
        item.setNomeProduto(produto.getNome());
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());
        item.setCustoUnitario(produto.getCusto());

        pedido.adicionarItem(item);

        Pedido pedidoSalvo = pedidoRepository.saveAndFlush(pedido);

        return pedidoSalvo.getId();
    }
}