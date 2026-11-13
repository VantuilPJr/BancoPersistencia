package br.com.banco.repository;

import br.com.banco.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    Optional<Conta> findByAgenciaAndNumero(String agencia, String numero);
    List<Conta> findAllByNumero(String numero);
    List<Conta> findByClienteCpf(String cpf);
}
