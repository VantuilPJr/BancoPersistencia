package br.com.banco.dto;

import br.com.banco.model.Conta;

import java.math.BigDecimal;

public record ContaResponse(Long id, String agencia, String numero, String titular,
                            BigDecimal saldo, BigDecimal saldoReservado, BigDecimal saldoDisponivel) {

    public static ContaResponse de(Conta c) {
        return new ContaResponse(c.getId(), c.getAgencia(), c.getNumero(), c.getCliente().getNome(),
                c.getSaldo(), c.getSaldoReservado(), c.getSaldoDisponivel());
    }
}
