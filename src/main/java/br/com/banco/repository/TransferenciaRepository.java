package br.com.banco.repository;

import br.com.banco.model.StatusTransacao;
import br.com.banco.model.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {
    List<Transferencia> findByContaOrigemIdOrderByDataSolicitacaoDesc(Long contaId);
    List<Transferencia> findByStatusAndDataProcessamentoLessThanEqual(StatusTransacao status, LocalDateTime ate);
}
