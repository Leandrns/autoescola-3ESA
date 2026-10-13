package br.com.fiap3esa.autoescola3esa.domain.endereco;

/** Resposta da nossa API para a consulta de CEP (já traduzida do formato do ViaCEP). */
public record DadosConsultaCep(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String cidade,
        String uf) {
}
