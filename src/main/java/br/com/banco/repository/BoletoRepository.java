package br.com.banco.repository;

import br.com.banco.model.Boleto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoletoRepository extends JpaRepository<Boleto, Long> {
    Optional<Boleto> findByCodigoBarras(String codigoBarras);
}
