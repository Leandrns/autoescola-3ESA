package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.DadosDetalhamentoInstrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.DadosListagemInstrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.service.InstrutorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InstrutorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstrutorService service;

    private static final String JSON_VALIDO = """
            {
              "nome": "Carlos Instrutor", "email": "carlos@email.com", "telefone": "11988888888",
              "cnh": "12345678901", "especialidade": "CARROS",
              "endereco": {"logradouro": "Rua Teste", "numero": "100", "bairro": "Centro",
                           "cidade": "São Paulo", "uf": "SP", "cep": "01001000"}
            }
            """;

    private Instrutor instrutor() {
        return new Instrutor(1L, "Carlos Instrutor", "carlos@email.com", "11988888888", "12345678901",
                Especialidade.CARROS, new Endereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000"), true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN cadastra instrutor: 201")
    void adminCadastra() throws Exception {
        when(service.cadastrarInstrutor(any())).thenReturn(new DadosDetalhamentoInstrutor(instrutor()));

        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Carlos Instrutor"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER não pode cadastrar instrutor: 403")
    void userNaoCadastra() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Cadastro com CNH inválida: 400")
    void cadastroComCnhInvalida() throws Exception {
        String json = JSON_VALIDO.replace("12345678901", "abc");

        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER pode listar instrutores: 200")
    void userListaInstrutores() throws Exception {
        when(service.listarInstrutores(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new DadosListagemInstrutor(instrutor()))));

        mockMvc.perform(get("/instrutores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Carlos Instrutor"));
    }
}
