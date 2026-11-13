package br.com.banco.service;

import br.com.banco.dto.PagamentoRequest;
import br.com.banco.dto.PagamentoResponse;
import br.com.banco.model.*;
import br.com.banco.repository.BoletoRepository;
import br.com.banco.repository.ContaRepository;
import br.com.banco.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentos;
    private final BoletoRepository boletos;
    private final ContaRepository contas;
    private final ContaService contaService;
    private final BancoCentralGateway bancoCentral;

    public PagamentoService(PagamentoRepository pagamentos, BoletoRepository boletos, ContaRepository contas,
                            ContaService contaService, BancoCentralGateway bancoCentral) {
        this.pagamentos = pagamentos;
        this.boletos = boletos;
        this.contas = contas;
        this.contaService = contaService;
        this.bancoCentral = bancoCentral;
    }

    @Transactional
    public PagamentoResponse pagar(TipoPagamento tipo, PagamentoRequest req) {
        Conta conta = contaService.buscar(req.contaId());
        return switch (tipo) {
            case BOLETO -> pagarBoleto(conta, req);
            case PIX -> pagarPix(conta, req);
            case SALDO_EM_CONTA -> pagarSaldo(conta, req);
        };
    }

    private PagamentoResponse pagarBoleto(Conta conta, PagamentoRequest req) {
        String codigo = req.codigoBarras();
        if (codigo == null || !codigo.matches("\\d{44,48}")) {
            throw new NegocioException("Código de barras inválido (esperado 44 a 48 dígitos)");
        }
        Boleto boleto = boletos.findByCodigoBarras(codigo)
                .orElseThrow(() -> new NegocioException("Boleto não encontrado", true));

        Pagamento pagamento = new Pagamento(TipoPagamento.BOLETO, conta, boleto.getValor());
        pagamento.setCodigoBarras(codigo);
        if (boleto.isPago()) {
            return negar(pagamento, "Boleto já pago");
        }
        return efetivar(pagamento, () -> {
            boleto.setPago(true);
            boletos.save(boleto);
        });
    }

    private PagamentoResponse pagarPix(Conta conta, PagamentoRequest req) {
        exigir(req.chavePixOuQrCode(), "chavePixOuQrCode");
        BigDecimal valor = exigirValor(req);
        Pagamento pagamento = new Pagamento(TipoPagamento.PIX, conta, valor);
        pagamento.setChavePixOuQrCode(req.chavePixOuQrCode());
        return efetivar(pagamento, () -> {});
    }

    private PagamentoResponse pagarSaldo(Conta conta, PagamentoRequest req) {
        exigir(req.destinatario(), "destinatario");
        BigDecimal valor = exigirValor(req);
        Pagamento pagamento = new Pagamento(TipoPagamento.SALDO_EM_CONTA, conta, valor);
        pagamento.setDestinatario(req.destinatario());
        List<Conta> destinos = contas.findAllByNumero(req.destinatario());
        if (destinos.isEmpty()) {
            return negar(pagamento, "Conta destinatária não encontrada");
        }
        if (destinos.size() > 1) {
            return negar(pagamento, "Número de conta destinatária ambíguo");
        }
        Conta destino = destinos.getFirst();
        if (destino.getId().equals(conta.getId())) {
            return negar(pagamento, "Conta destinatária igual à conta de origem");
        }
        return efetivar(pagamento, () -> contaService.creditar(destino, valor));
    }

    private PagamentoResponse efetivar(Pagamento pagamento, Runnable aposDebito) {
        Conta conta = pagamento.getConta();
        BigDecimal valor = pagamento.getValor();
        if (!contaService.temSaldo(conta, valor)) {
            return negar(pagamento, "Saldo insuficiente");
        }
        if (!bancoCentral.solicitarAutorizacao(valor)) {
            return negar(pagamento, "Pagamento negado pelo Banco Central");
        }
        contaService.debitar(conta, valor);
        aposDebito.run();
        pagamento.setStatus(StatusTransacao.CONCLUIDA);
        return PagamentoResponse.de(pagamentos.save(pagamento));
    }

    @Transactional(readOnly = true)
    public PagamentoResponse buscar(Long id) {
        return pagamentos.findById(id).map(PagamentoResponse::de)
                .orElseThrow(() -> new NegocioException("Pagamento não encontrado: " + id, true));
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPorConta(Long contaId) {
        return pagamentos.findByContaIdOrderByDataPagamentoDesc(contaId).stream().map(PagamentoResponse::de).toList();
    }

    private PagamentoResponse negar(Pagamento p, String motivo) {
        p.setStatus(StatusTransacao.NEGADA);
        p.setMotivoFalha(motivo);
        return PagamentoResponse.de(pagamentos.save(p));
    }

    private BigDecimal exigirValor(PagamentoRequest req) {
        if (req.valor() == null) {
            throw new NegocioException("Campo obrigatório: valor");
        }
        return req.valor();
    }

    private void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("Campo obrigatório: " + campo);
        }
    }
}
