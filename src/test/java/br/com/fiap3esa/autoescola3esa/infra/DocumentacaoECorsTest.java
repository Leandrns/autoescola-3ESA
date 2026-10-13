package br.com.fiap3esa.autoescola3esa.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Garante que a documentação Swagger é pública e que o CORS libera/bloqueia as origens configuradas. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentacaoECorsTest {

    @Autowired
    private MockMvc mockMvc;

    // ------------------------------------------------------------------ Swagger

    @Test
    @DisplayName("OpenAPI JSON é público e descreve os endpoints")
    void openApiJson() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/instrucoes")))
                .andExpect(content().string(containsString("bearer-key")));
    }

    @Test
    @DisplayName("OpenAPI YAML é público (regressão do typo no SecurityConfig)")
    void openApiYaml() throws Exception {
        mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger UI é público")
    void swaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ CORS

    @Test
    @DisplayName("Preflight de origem permitida (localhost:3000) para POST é aceito")
    void preflightOrigemPermitida() throws Exception {
        mockMvc.perform(options("/instrucoes")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("Preflight permite PATCH (usado no cancelamento de instrução)")
    void preflightPatch() throws Exception {
        mockMvc.perform(options("/instrucoes/1/cancelamento")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Preflight de origem NÃO permitida é rejeitado (403)")
    void preflightOrigemNaoPermitida() throws Exception {
        mockMvc.perform(options("/instrucoes")
                        .header("Origin", "http://site-malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
