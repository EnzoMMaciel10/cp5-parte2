package br.com.fiap.cp5.repository;

import br.com.fiap.cp5.model.EmailEnvio;
import br.com.fiap.cp5.model.StatusEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailEnvioRepository extends JpaRepository<EmailEnvio, Long> {
    Optional<EmailEnvio> findByPedido_Id(Long pedidoId);
    List<EmailEnvio> findTop20ByStatusOrderByCriadoEmAsc(StatusEmail status);
    long countByStatus(StatusEmail status);
}
