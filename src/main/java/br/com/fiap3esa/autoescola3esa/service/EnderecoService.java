package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosConsultaCep;
import br.com.fiap3esa.autoescola3esa.infra.externo.viacep.ViaCepClient;
import org.springframework.stereotype.Service;

@Service
public class EnderecoService {
    private final ViaCepClient viaCepClient;

    public EnderecoService(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public DadosConsultaCep consultarPorCep(String cep) {
        return viaCepClient.consultar(cep);
    }
}
