package br.com.banco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagamentoRequest(
        @NotNull Long contaId,
        @DecimalMin(value = "0.01") BigDecimal valor,
        String codigoBarras,
        String chavePixOuQrCode,
        String destinatario) {
}
