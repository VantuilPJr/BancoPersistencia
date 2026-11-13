package br.com.banco.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "boleto")
public class Boleto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 48)
    private String codigoBarras;

    @Column(nullable = false)
    private String beneficiario;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    private LocalDate vencimento;

    @Column(nullable = false)
    private boolean pago = false;

    protected Boleto() {
    }

    public Boleto(String codigoBarras, String beneficiario, BigDecimal valor, LocalDate vencimento) {
        this.codigoBarras = codigoBarras;
        this.beneficiario = beneficiario;
        this.valor = valor;
        this.vencimento = vencimento;
    }

    public Long getId() { return id; }
    public String getCodigoBarras() { return codigoBarras; }
    public String getBeneficiario() { return beneficiario; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getVencimento() { return vencimento; }
    public boolean isPago() { return pago; }
    public void setPago(boolean pago) { this.pago = pago; }
}
