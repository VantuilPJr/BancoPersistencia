package br.com.banco.service;

import br.com.banco.model.Conta;
import br.com.banco.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ContaService {

    private final ContaRepository contas;

    public ContaService(ContaRepository contas) {
        this.contas = contas;
    }

    @Transactional(readOnly = true)
    public br.com.banco.dto.ContaResponse consultar(Long id) {
        return br.com.banco.dto.ContaResponse.de(buscar(id));
    }

    public Conta buscar(Long id) {
        return contas.findById(id).orElseThrow(() -> new NegocioException("Conta não encontrada: " + id, true));
    }

    public boolean temSaldo(Conta conta, BigDecimal valor) {
        return conta.getSaldoDisponivel().compareTo(valor) >= 0;
    }

    @Transactional
    public void debitar(Conta conta, BigDecimal valor) {
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contas.save(conta);
    }

    @Transactional
    public void creditar(Conta conta, BigDecimal valor) {
        conta.setSaldo(conta.getSaldo().add(valor));
        contas.save(conta);
    }

    @Transactional
    public void reservar(Conta conta, BigDecimal valor) {
        conta.setSaldoReservado(conta.getSaldoReservado().add(valor));
        contas.save(conta);
    }

    @Transactional
    public void liberarReserva(Conta conta, BigDecimal valor) {
        conta.setSaldoReservado(conta.getSaldoReservado().subtract(valor));
        contas.save(conta);
    }

    @Transactional
    public void confirmarReserva(Conta conta, BigDecimal valor) {
        conta.setSaldoReservado(conta.getSaldoReservado().subtract(valor));
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contas.save(conta);
    }
}
