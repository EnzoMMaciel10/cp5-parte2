package br.com.fiap.cp5.service;

import br.com.fiap.cp5.model.Mercado;
import br.com.fiap.cp5.repository.MercadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MercadoService {

    private final MercadoRepository mercadoRepository;

    public MercadoService(MercadoRepository mercadoRepository) {
        this.mercadoRepository = mercadoRepository;
    }

    public List<Mercado> listarTodos() {
        return mercadoRepository.findAll();
    }

    public List<Mercado> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return listarTodos();
        }

        return mercadoRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    public Mercado buscarPorId(Long id) {
        return mercadoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Produto não encontrado.")
                );
    }

    @Transactional
    public Mercado cadastrar(Mercado mercado) {
        mercado.setId(null);
        return mercadoRepository.save(mercado);
    }

    @Transactional
    public Mercado atualizar(Long id, Mercado dadosAtualizados) {
        Mercado mercado = buscarPorId(id);

        mercado.setNome(dadosAtualizados.getNome());
        mercado.setTipo(dadosAtualizados.getTipo());
        mercado.setSetor(dadosAtualizados.getSetor());
        mercado.setTamanho(dadosAtualizados.getTamanho());
        mercado.setPreco(dadosAtualizados.getPreco());
        mercado.setCusto(dadosAtualizados.getCusto());
        mercado.setEstoque(dadosAtualizados.getEstoque());

        return mercadoRepository.save(mercado);
    }

    @Transactional
    public void excluir(Long id) {
        Mercado mercado = buscarPorId(id);
        mercadoRepository.delete(mercado);
    }
}