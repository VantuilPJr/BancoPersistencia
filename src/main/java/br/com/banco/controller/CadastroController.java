package br.com.banco.controller;

import br.com.banco.dto.*;
import br.com.banco.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CadastroController {

    private final CadastroService service;

    public CadastroController(CadastroService service) {
        this.service = service;
    }

    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criarCliente(@Valid @RequestBody ClienteRequest r) {
        return service.criarCliente(r);
    }

    @GetMapping("/clientes")
    public List<ClienteResponse> listarClientes() {
        return service.listarClientes();
    }

    @PostMapping("/contas")
    @ResponseStatus(HttpStatus.CREATED)
    public ContaResponse criarConta(@Valid @RequestBody ContaRequest r) {
        return service.criarConta(r);
    }

    @GetMapping("/contas")
    public List<ContaResponse> listarContas() {
        return service.listarContas();
    }

    @PostMapping("/boletos")
    @ResponseStatus(HttpStatus.CREATED)
    public BoletoResponse criarBoleto(@Valid @RequestBody BoletoRequest r) {
        return service.criarBoleto(r);
    }

    @GetMapping("/boletos")
    public List<BoletoResponse> listarBoletos() {
        return service.listarBoletos();
    }
}
