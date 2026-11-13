package br.com.banco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContaRequest(
        @NotNull Long clienteId,
        @NotBlank String agencia,
        @NotBlank String numero,
        @DecimalMin("0.00") BigDecimal saldoInicial) {
}
