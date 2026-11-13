package br.com.banco.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BancoCentralSimulado implements BancoCentralGateway {

    private final BigDecimal limite;

    public BancoCentralSimulado(@Value("${banco.bancocentral.limite:50000}") BigDecimal limite) {
        this.limite = limite;
    }

    @Override
    public boolean solicitarAutorizacao(BigDecimal valor) {
        return valor.compareTo(limite) <= 0;
    }
}
