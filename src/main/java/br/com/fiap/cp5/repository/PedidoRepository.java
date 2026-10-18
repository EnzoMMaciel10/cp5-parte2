package br.com.fiap.cp5.repository;

import br.com.fiap.cp5.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByUsuario_EmailOrderByCriadoEmDesc(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pedido> findBloqueadoById(Long id);

    @EntityGraph(attributePaths = {"itens", "usuario"})
    List<Pedido> findAllByOrderByCriadoEmDesc();
}
