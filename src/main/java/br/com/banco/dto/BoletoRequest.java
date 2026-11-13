package br.com.banco.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BoletoRequest(
        @NotBlank @Pattern(regexp = "\\d{44,48}", message = "Código de barras deve ter 44 a 48 dígitos") String codigoBarras,
        @NotBlank String beneficiario,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        LocalDate vencimento) {
}
