package br.com.fiap.cp5.repository;

import br.com.fiap.cp5.model.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    List<Mercado> findByNomeContainingIgnoreCase(String nome);
}