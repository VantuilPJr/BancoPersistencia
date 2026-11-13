package br.com.banco.dto;

import br.com.banco.model.Cliente;

public record ClienteResponse(Long id, String nome, String cpf) {
    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getCpf());
    }
}
