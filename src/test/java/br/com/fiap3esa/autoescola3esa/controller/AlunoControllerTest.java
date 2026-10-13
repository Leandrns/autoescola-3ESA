package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.DadosDetalhamentoAluno;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.service.AlunoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlunoService service;

    private static final String JSON_VALIDO = """
            {
              "nome": "Ana Aluna", "email": "ana@email.com", "telefone": "11999999999", "cpf": "12345678901",
              "endereco": {"logradouro": "Rua Teste", "numero": "100", "bairro": "Centro",
                           "cidade": "São Paulo", "uf": "SP", "cep": "01001000"}
            }
            """;

    @Test
    @WithMockUser
    @DisplayName("POST /alunos válido retorna 201")
    void cadastrarComSucesso() throws Exception {
        when(service.cadastrarAluno(any())).thenReturn(new DadosDetalhamentoAluno(
                1L, "Ana Aluna", "ana@email.com", "11999999999", "12345678901",
                new DadosEndereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000"), true));

        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ana Aluna"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /alunos com CPF inválido retorna 400")
    void cadastrarComCpfInvalido() throws Exception {
        String json = JSON_VALIDO.replace("12345678901", "123");

        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /alunos/{id} inexistente retorna 404")
    void detalharInexistente() throws Exception {
        when(service.detalharAluno(99L)).thenThrow(new AlunoNotFoundException("ID do aluno informado não existe!"));

        mockMvc.perform(get("/alunos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ID do aluno informado não existe!"));
    }
}
