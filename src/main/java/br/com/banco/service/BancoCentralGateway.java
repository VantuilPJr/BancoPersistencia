package br.com.banco.service;

import java.math.BigDecimal;

public interface BancoCentralGateway {
    boolean solicitarAutorizacao(BigDecimal valor);
}
