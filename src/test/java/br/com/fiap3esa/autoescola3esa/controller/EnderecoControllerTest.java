package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.endereco.CepNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosConsultaCep;
import br.com.fiap3esa.autoescola3esa.infra.exception.ServicoExternoException;
import br.com.fiap3esa.autoescola3esa.service.EnderecoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnderecoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnderecoService service;

    @Test
    @WithMockUser
    @DisplayName("GET /enderecos/cep/{cep} retorna 200 com o endereço")
    void consultaComSucesso() throws Exception {
        when(service.consultarPorCep("01001000")).thenReturn(
                new DadosConsultaCep("01001000", "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP"));

        mockMvc.perform(get("/enderecos/cep/01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"));
    }

    @Test
    @WithMockUser
    @DisplayName("CEP inexistente retorna 404")
    void cepInexistente() throws Exception {
        when(service.consultarPorCep("99999999")).thenThrow(new CepNotFoundException("CEP não encontrado."));

        mockMvc.perform(get("/enderecos/cep/99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("API externa fora do ar retorna 502")
    void apiExternaForaDoAr() throws Exception {
        when(service.consultarPorCep("01001000"))
                .thenThrow(new ServicoExternoException("Serviço ViaCEP indisponível no momento. Tente novamente.", new RuntimeException()));

        mockMvc.perform(get("/enderecos/cep/01001000"))
                .andExpect(status().isBadGateway());
    }
}
