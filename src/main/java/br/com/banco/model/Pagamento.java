package br.com.banco.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pagamento BOLETO / PIX / SALDO_EM_CONTA (diagramas de Pagamento). */
@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPagamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTransacao status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    /** BOLETO */
    private String codigoBarras;
    /** PIX: chave ou QR Code */
    private String chavePixOuQrCode;
    /** SALDO_EM_CONTA: número da conta de destino */
    private String destinatario;

    @Column(nullable = false)
    private LocalDateTime dataPagamento = LocalDateTime.now();

    private String motivoFalha;

    protected Pagamento() {
    }

    public Pagamento(TipoPagamento tipo, Conta conta, BigDecimal valor) {
        this.tipo = tipo;
        this.conta = conta;
        this.valor = valor;
        this.status = StatusTransacao.PENDENTE;
    }

    public Long getId() { return id; }
    public TipoPagamento getTipo() { return tipo; }
    public StatusTransacao getStatus() { return status; }
    public void setStatus(StatusTransacao status) { this.status = status; }
    public Conta getConta() { return conta; }
    public BigDecimal getValor() { return valor; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public String getChavePixOuQrCode() { return chavePixOuQrCode; }
    public void setChavePixOuQrCode(String chavePixOuQrCode) { this.chavePixOuQrCode = chavePixOuQrCode; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public String getMotivoFalha() { return motivoFalha; }
    public void setMotivoFalha(String motivoFalha) { this.motivoFalha = motivoFalha; }
}
