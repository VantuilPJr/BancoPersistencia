package br.com.banco.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "conta", uniqueConstraints = @UniqueConstraint(columnNames = {"agencia", "numero"}))
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String agencia;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "saldo_reservado", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoReservado = BigDecimal.ZERO;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    protected Conta() {
    }

    public Conta(String agencia, String numero, BigDecimal saldo, Cliente cliente) {
        this.agencia = agencia;
        this.numero = numero;
        this.saldo = saldo;
        this.cliente = cliente;
    }

    public BigDecimal getSaldoDisponivel() {
        return saldo.subtract(saldoReservado);
    }

    public Long getId() { return id; }
    public String getAgencia() { return agencia; }
    public String getNumero() { return numero; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public BigDecimal getSaldoReservado() { return saldoReservado; }
    public void setSaldoReservado(BigDecimal saldoReservado) { this.saldoReservado = saldoReservado; }
    public Cliente getCliente() { return cliente; }
}
