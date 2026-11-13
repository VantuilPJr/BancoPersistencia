package br.com.banco.controller;

import br.com.banco.dto.PagamentoRequest;
import br.com.banco.dto.PagamentoResponse;
import br.com.banco.model.StatusTransacao;
import br.com.banco.model.TipoPagamento;
import br.com.banco.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @PostMapping("/{tipo}")
    public ResponseEntity<PagamentoResponse> pagar(@PathVariable String tipo,
                                                   @Valid @RequestBody PagamentoRequest req) {
        PagamentoResponse r = service.pagar(parse(tipo), req);
        HttpStatus status = r.status() == StatusTransacao.NEGADA ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(r);
    }

    private static TipoPagamento parse(String tipo) {
        try {
            return TipoPagamento.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new br.com.banco.service.NegocioException("Tipo de pagamento inválido: " + tipo + " (boleto, pix ou saldo_em_conta)");
        }
    }

    @GetMapping("/{id}")
    public PagamentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/conta/{contaId}")
    public List<PagamentoResponse> listar(@PathVariable Long contaId) {
        return service.listarPorConta(contaId);
    }
}
