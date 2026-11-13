package br.com.banco.dto;

import br.com.banco.model.Boleto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BoletoResponse(Long id, String codigoBarras, String beneficiario, BigDecimal valor,
                             LocalDate vencimento, boolean pago) {
    public static BoletoResponse de(Boleto b) {
        return new BoletoResponse(b.getId(), b.getCodigoBarras(), b.getBeneficiario(), b.getValor(),
                b.getVencimento(), b.isPago());
    }
}
