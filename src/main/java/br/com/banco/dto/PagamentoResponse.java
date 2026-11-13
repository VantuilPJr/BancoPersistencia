package br.com.banco.dto;

import br.com.banco.model.Pagamento;
import br.com.banco.model.StatusTransacao;
import br.com.banco.model.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(
        Long id, TipoPagamento tipo, StatusTransacao status, Long contaId, BigDecimal valor,
        String codigoBarras, String chavePixOuQrCode, String destinatario,
        LocalDateTime dataPagamento, String motivoFalha) {

    public static PagamentoResponse de(Pagamento p) {
        return new PagamentoResponse(p.getId(), p.getTipo(), p.getStatus(), p.getConta().getId(), p.getValor(),
                p.getCodigoBarras(), p.getChavePixOuQrCode(), p.getDestinatario(), p.getDataPagamento(),
                p.getMotivoFalha());
    }
}
