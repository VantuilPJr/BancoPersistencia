package br.com.banco.repository;

import br.com.banco.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByContaIdOrderByDataPagamentoDesc(Long contaId);
}
