package br.com.banco.service;

import br.com.banco.dto.BoletoRequest;
import br.com.banco.dto.BoletoResponse;
import br.com.banco.dto.ClienteRequest;
import br.com.banco.dto.ClienteResponse;
import br.com.banco.dto.ContaRequest;
import br.com.banco.dto.ContaResponse;
import br.com.banco.model.Boleto;
import br.com.banco.model.Cliente;
import br.com.banco.model.Conta;
import br.com.banco.repository.BoletoRepository;
import br.com.banco.repository.ClienteRepository;
import br.com.banco.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Cadastros básicos: clientes, contas e boletos (entradas que alimentam o banco de dados). */
@Service
public class CadastroService {

    private final ClienteRepository clientes;
    private final ContaRepository contas;
    private final BoletoRepository boletos;

    public CadastroService(ClienteRepository clientes, ContaRepository contas, BoletoRepository boletos) {
        this.clientes = clientes;
        this.contas = contas;
        this.boletos = boletos;
    }

    @Transactional
    public ClienteResponse criarCliente(ClienteRequest r) {
        if (clientes.findByCpf(r.cpf()).isPresent()) {
            throw new NegocioException("Já existe cliente com este CPF");
        }
        return ClienteResponse.de(clientes.save(new Cliente(r.nome(), r.cpf())));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarClientes() {
        return clientes.findAll().stream().map(ClienteResponse::de).toList();
    }

    @Transactional
    public ContaResponse criarConta(ContaRequest r) {
        Cliente cliente = clientes.findById(r.clienteId())
                .orElseThrow(() -> new NegocioException("Cliente não encontrado: " + r.clienteId(), true));
        if (contas.findByAgenciaAndNumero(r.agencia(), r.numero()).isPresent()) {
            throw new NegocioException("Já existe conta com esta agência/número");
        }
        BigDecimal saldo = r.saldoInicial() == null ? BigDecimal.ZERO : r.saldoInicial();
        return ContaResponse.de(contas.save(new Conta(r.agencia(), r.numero(), saldo, cliente)));
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listarContas() {
        return contas.findAll().stream().map(ContaResponse::de).toList();
    }

    @Transactional
    public BoletoResponse criarBoleto(BoletoRequest r) {
        if (boletos.findByCodigoBarras(r.codigoBarras()).isPresent()) {
            throw new NegocioException("Já existe boleto com este código de barras");
        }
        return BoletoResponse.de(boletos.save(
                new Boleto(r.codigoBarras(), r.beneficiario(), r.valor(), r.vencimento())));
    }

    @Transactional(readOnly = true)
    public List<BoletoResponse> listarBoletos() {
        return boletos.findAll().stream().map(BoletoResponse::de).toList();
    }
}
