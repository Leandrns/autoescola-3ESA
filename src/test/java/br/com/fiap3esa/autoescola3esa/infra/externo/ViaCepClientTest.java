package br.com.fiap3esa.autoescola3esa.infra.externo;

import br.com.fiap3esa.autoescola3esa.domain.endereco.CepNotFoundException;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import br.com.fiap3esa.autoescola3esa.infra.exception.ServicoExternoException;
import br.com.fiap3esa.autoescola3esa.infra.externo.viacep.ViaCepClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Testa o consumo da API externa SEM acessar a internet: o MockRestServiceServer intercepta o RestClient.
 */
class ViaCepClientTest {

    private static final String BASE_URL = "https://viacep.com.br/ws";

    private MockRestServiceServer server;
    private ViaCepClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ViaCepClient(builder.build());
    }

    @Test
    @DisplayName("Consulta um CEP existente e converte a resposta do ViaCEP")
    void consultaCepExistente() {
        String json = """
                {"cep":"01001-000","logradouro":"Praça da Sé","complemento":"lado ímpar","unidade":"",
                 "bairro":"Sé","localidade":"São Paulo","uf":"SP","estado":"São Paulo","ibge":"3550308"}
                """;
        server.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        var endereco = client.consultar("01001-000");

        assertThat(endereco.cep()).isEqualTo("01001000");
        assertThat(endereco.logradouro()).isEqualTo("Praça da Sé");
        assertThat(endereco.cidade()).isEqualTo("São Paulo");
        assertThat(endereco.uf()).isEqualTo("SP");
        server.verify();
    }

    @Test
    @DisplayName("CEP com formato válido mas inexistente (resposta {\"erro\": ...}) vira CepNotFoundException")
    void cepInexistente() {
        server.expect(requestTo(BASE_URL + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": \"true\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.consultar("99999999")).isInstanceOf(CepNotFoundException.class);
    }

    @Test
    @DisplayName("CEP com formato inválido é rejeitado sem chamar a API externa")
    void cepComFormatoInvalido() {
        assertThatThrownBy(() -> client.consultar("123")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> client.consultar(null)).isInstanceOf(RegraNegocioException.class);
        server.verify(); // nenhuma requisição era esperada
    }

    @Test
    @DisplayName("Falha da API externa (HTTP 500) vira ServicoExternoException")
    void apiExternaIndisponivel() {
        server.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.consultar("01001000")).isInstanceOf(ServicoExternoException.class);
    }
}
