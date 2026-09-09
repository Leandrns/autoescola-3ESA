package br.com.fiap3esa.autoescola3esa.domain.instrucao;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record DadosCancelamentoInstrucao(
        @NotNull(message = "O motivo do cancelamento é obrigatório.")
        @JsonAlias("motivo_cancelamento")
        MotivoCancelamento motivoCancelamento
) {
}
