package br.com.banco.dto;

import br.com.banco.model.StatusTransacao;
import br.com.banco.model.TipoTransferencia;
import br.com.banco.model.Transferencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaResponse(
        Long id, TipoTransferencia tipo, StatusTransacao status, Long contaOrigemId, BigDecimal valor,
        String bancoDestino, String agenciaDestino, String contaDestino, String cpfDestino,
        String chavePix, String destinatario,
        LocalDateTime dataSolicitacao, LocalDateTime dataProcessamento, String motivoFalha) {

    public static TransferenciaResponse de(Transferencia t) {
        return new TransferenciaResponse(t.getId(), t.getTipo(), t.getStatus(), t.getContaOrigem().getId(),
                t.getValor(), t.getBancoDestino(), t.getAgenciaDestino(), t.getContaDestino(), t.getCpfDestino(),
                t.getChavePix(), t.getDestinatario(), t.getDataSolicitacao(), t.getDataProcessamento(),
                t.getMotivoFalha());
    }
}
