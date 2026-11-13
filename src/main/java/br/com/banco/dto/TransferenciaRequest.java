package br.com.banco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferenciaRequest(
        @NotNull Long contaOrigemId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        String bancoDestino,
        String agenciaDestino,
        String contaDestino,
        String cpfDestino,
        String chavePix,
        String destinatario) {
}
