package br.com.banco.controller;

import br.com.banco.dto.TransferenciaRequest;
import br.com.banco.dto.TransferenciaResponse;
import br.com.banco.model.StatusTransacao;
import br.com.banco.model.TipoTransferencia;
import br.com.banco.service.TransferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaService service;

    public TransferenciaController(TransferenciaService service) {
        this.service = service;
    }

    @PostMapping("/{tipo}")
    public ResponseEntity<TransferenciaResponse> transferir(@PathVariable String tipo,
                                                            @Valid @RequestBody TransferenciaRequest req) {
        TransferenciaResponse r = service.transferir(parse(tipo), req);
        HttpStatus status = r.status() == StatusTransacao.NEGADA ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(r);
    }

    private static TipoTransferencia parse(String tipo) {
        try {
            return TipoTransferencia.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new br.com.banco.service.NegocioException("Tipo de transferência inválido: " + tipo + " (pix, ted ou doc)");
        }
    }

    @GetMapping("/{id}")
    public TransferenciaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/conta/{contaId}")
    public List<TransferenciaResponse> listar(@PathVariable Long contaId) {
        return service.listarPorConta(contaId);
    }

    @PostMapping("/agendadas/processar")
    public List<TransferenciaResponse> processarAgendadas() {
        return service.processarAgendadas();
    }

    @PostMapping("/{id}/processar")
    public TransferenciaResponse processar(@PathVariable Long id) {
        return service.processarAgendada(id);
    }
}
