package br.com.banco.service;

import br.com.banco.dto.TransferenciaRequest;
import br.com.banco.dto.TransferenciaResponse;
import br.com.banco.model.*;
import br.com.banco.repository.TransferenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferenciaService {

    private final TransferenciaRepository transferencias;
    private final ContaService contaService;
    private final BancoCentralGateway bancoCentral;

    public TransferenciaService(TransferenciaRepository transferencias,
                                ContaService contaService, BancoCentralGateway bancoCentral) {
        this.transferencias = transferencias;
        this.contaService = contaService;
        this.bancoCentral = bancoCentral;
    }

    @Transactional
    public TransferenciaResponse transferir(TipoTransferencia tipo, TransferenciaRequest req) {
        Conta origem = contaService.buscar(req.contaOrigemId());
        validarDados(tipo, req);

        Transferencia t = new Transferencia(tipo, origem, req.valor());
        t.setBancoDestino(req.bancoDestino());
        t.setAgenciaDestino(req.agenciaDestino());
        t.setContaDestino(req.contaDestino());
        t.setCpfDestino(req.cpfDestino());
        t.setChavePix(req.chavePix());
        t.setDestinatario(req.destinatario());

        if (!contaService.temSaldo(origem, req.valor())) {
            return negar(t, "Saldo insuficiente");
        }

        if (tipo == TipoTransferencia.DOC) {
            contaService.reservar(origem, req.valor());
            t.setStatus(StatusTransacao.AGENDADO);
            t.setDataProcessamento(proximoDiaUtil());
            return TransferenciaResponse.de(transferencias.save(t));
        }

        if (!bancoCentral.solicitarAutorizacao(req.valor())) {
            return negar(t, "Transação não autorizada pelo Banco Central");
        }
        contaService.debitar(origem, req.valor());
        t.setStatus(StatusTransacao.CONCLUIDA);
        t.setDataProcessamento(LocalDateTime.now());
        return TransferenciaResponse.de(transferencias.save(t));
    }

    @Transactional
    public List<TransferenciaResponse> processarAgendadas() {
        return transferencias
                .findByStatusAndDataProcessamentoLessThanEqual(StatusTransacao.AGENDADO, LocalDateTime.now())
                .stream().map(this::processarAgendada).toList();
    }

    @Transactional
    public TransferenciaResponse processarAgendada(Long id) {
        Transferencia t = transferencias.findById(id)
                .orElseThrow(() -> new NegocioException("Transferência não encontrada: " + id, true));
        if (t.getStatus() != StatusTransacao.AGENDADO) {
            throw new NegocioException("Transferência não está agendada");
        }
        return processarAgendada(t);
    }

    private TransferenciaResponse processarAgendada(Transferencia t) {
        Conta origem = t.getContaOrigem();
        if (bancoCentral.solicitarAutorizacao(t.getValor())) {
            contaService.confirmarReserva(origem, t.getValor());
            t.setStatus(StatusTransacao.CONCLUIDA);
            t.setDataProcessamento(LocalDateTime.now());
        } else {
            contaService.liberarReserva(origem, t.getValor());
            t.setStatus(StatusTransacao.NEGADA);
            t.setMotivoFalha("Transação não autorizada pelo Banco Central");
        }
        return TransferenciaResponse.de(transferencias.save(t));
    }

    @Transactional(readOnly = true)
    public TransferenciaResponse buscar(Long id) {
        return transferencias.findById(id).map(TransferenciaResponse::de)
                .orElseThrow(() -> new NegocioException("Transferência não encontrada: " + id, true));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponse> listarPorConta(Long contaId) {
        return transferencias.findByContaOrigemIdOrderByDataSolicitacaoDesc(contaId)
                .stream().map(TransferenciaResponse::de).toList();
    }

    private TransferenciaResponse negar(Transferencia t, String motivo) {
        t.setStatus(StatusTransacao.NEGADA);
        t.setMotivoFalha(motivo);
        return TransferenciaResponse.de(transferencias.save(t));
    }

    private void validarDados(TipoTransferencia tipo, TransferenciaRequest r) {
        switch (tipo) {
            case PIX -> {
                exigir(r.chavePix(), "chavePix");
                exigir(r.destinatario(), "destinatario");
            }
            case TED -> {
                exigir(r.bancoDestino(), "bancoDestino");
                exigir(r.agenciaDestino(), "agenciaDestino");
                exigir(r.contaDestino(), "contaDestino");
            }
            case DOC -> {
                exigir(r.bancoDestino(), "bancoDestino");
                exigir(r.agenciaDestino(), "agenciaDestino");
                exigir(r.contaDestino(), "contaDestino");
                exigir(r.cpfDestino(), "cpfDestino");
                if (!r.cpfDestino().matches("\\d{11}")) {
                    throw new NegocioException("cpfDestino deve ter 11 dígitos");
                }
            }
        }
    }

    private void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("Campo obrigatório: " + campo);
        }
    }

    private LocalDateTime proximoDiaUtil() {
        LocalDateTime d = LocalDateTime.now().plusDays(1);
        while (d.getDayOfWeek().getValue() > 5) {
            d = d.plusDays(1);
        }
        return d;
    }
}
