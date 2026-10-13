package br.com.fiap3esa.autoescola3esa.infra.externo.viacep;

import br.com.fiap3esa.autoescola3esa.domain.endereco.CepNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosConsultaCep;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import br.com.fiap3esa.autoescola3esa.infra.exception.ServicoExternoException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente da API externa ViaCEP. É o ponto único de consumo do WebService externo.
 */
@Component
public class ViaCepClient {

    private final RestClient restClient;

    public ViaCepClient(@Qualifier("viaCepRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public DadosConsultaCep consultar(String cep) {
        String cepNumerico = cep == null ? "" : cep.replaceAll("\\D", "");
        if (cepNumerico.length() != 8) {
            throw new RegraNegocioException("CEP inválido: informe 8 dígitos (ex.: 01001000 ou 01001-000).");
        }

        ViaCepResposta resposta;
        try {
            resposta = restClient.get()
                    .uri("/{cep}/json/", cepNumerico)
                    .retrieve()
                    .body(ViaCepResposta.class);
        } catch (RestClientException e) {
            throw new ServicoExternoException("Serviço ViaCEP indisponível no momento. Tente novamente.", e);
        }

        if (resposta == null || resposta.cepInexistente()) {
            throw new CepNotFoundException("CEP não encontrado.");
        }

        return new DadosConsultaCep(
                cepNumerico,
                resposta.logradouro(),
                resposta.complemento(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf());
    }
}
