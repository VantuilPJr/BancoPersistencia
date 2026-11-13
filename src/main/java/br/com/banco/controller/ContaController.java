package br.com.banco.controller;

import br.com.banco.dto.ContaResponse;
import br.com.banco.service.ContaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ContaResponse consultar(@PathVariable Long id) {
        return service.consultar(id);
    }
}
