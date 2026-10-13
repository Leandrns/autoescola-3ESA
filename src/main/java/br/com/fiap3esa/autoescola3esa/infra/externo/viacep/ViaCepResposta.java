package br.com.fiap3esa.autoescola3esa.infra.externo.viacep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Formato de resposta do ViaCEP (https://viacep.com.br/ws/{cep}/json/).
 * Quando o CEP tem formato válido mas não existe, a API devolve apenas {"erro": "true"} (ou true).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResposta(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Object erro) {

    public boolean cepInexistente() {
        return erro != null && "true".equalsIgnoreCase(String.valueOf(erro));
    }
}
