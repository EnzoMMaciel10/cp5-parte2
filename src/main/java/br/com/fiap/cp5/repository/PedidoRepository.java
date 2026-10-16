package br.com.fiap.cp5.repository;

import br.com.fiap.cp5.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByUsuario_EmailOrderByCriadoEmDesc(String email);
}