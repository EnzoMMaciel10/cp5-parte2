package br.com.fiap.cp5.repository;

import br.com.fiap.cp5.model.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    List<Mercado> findByNomeContainingIgnoreCase(String nome);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Mercado> findBloqueadoById(Long id);
}
