package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosDetalhamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.InstrucaoNotFoundException;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import br.com.fiap3esa.autoescola3esa.service.InstrucaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Requer o MySQL de teste (o contexto completo sobe com Flyway). O service é mockado. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InstrucaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstrucaoService service;

    private DadosDetalhamentoInstrucao detalhamento(String status) {
        return new DadosDetalhamentoInstrucao(1L, 1L, "Ana", 2L, "Carlos", "CARROS",
                LocalDateTime.of(2026, 11, 10, 10, 0), status, null, null);
    }

    private String dataFutura() {
        return LocalDateTime.now().plusDays(7).withHour(10).withMinute(0)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /instrucoes sem corpo retorna 400")
    void agendarSemCorpo() throws Exception {
        mockMvc.perform(post("/instrucoes"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /instrucoes com campos obrigatórios ausentes retorna 400")
    void agendarComDadosInvalidos() throws Exception {
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /instrucoes válido retorna 201 com Location")
    void agendarComSucesso() throws Exception {
        when(service.agendarInstrucao(any())).thenReturn(detalhamento("AGENDADA"));
        String json = """
                {"id_aluno": 1, "id_instrutor": 2, "especialidade": "CARROS", "data_hora": "%s"}
                """.formatted(dataFutura());

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/instrucoes/1"))
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    @WithMockUser
    @DisplayName("Violação de regra de negócio retorna 422 com a mensagem")
    void regraDeNegocioRetorna422() throws Exception {
        when(service.agendarInstrucao(any())).thenThrow(new RegraNegocioException("A auto-escola não funciona aos domingos."));
        String json = """
                {"id_aluno": 1, "especialidade": "CARROS", "data_hora": "%s"}
                """.formatted(dataFutura());

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("A auto-escola não funciona aos domingos."));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /instrucoes/{id}/cancelamento retorna 200 com status CANCELADA")
    void cancelarComSucesso() throws Exception {
        when(service.cancelarInstrucao(eq(1L), any())).thenReturn(detalhamento("CANCELADA"));

        mockMvc.perform(patch("/instrucoes/1/cancelamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo_cancelamento\": \"ALUNO_DESISTIU\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH com motivo inexistente retorna 400")
    void cancelarComMotivoInvalido() throws Exception {
        mockMvc.perform(patch("/instrucoes/1/cancelamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo_cancelamento\": \"QUALQUER_COISA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /instrucoes/{id} inexistente retorna 404")
    void detalharInexistente() throws Exception {
        when(service.detalharInstrucao(99L)).thenThrow(new InstrucaoNotFoundException("ID da instrução informado não existe!"));

        mockMvc.perform(get("/instrucoes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sem autenticação, o acesso é negado (4xx)")
    void semAutenticacao() throws Exception {
        mockMvc.perform(get("/instrucoes/1"))
                .andExpect(status().is4xxClientError());
    }
}
