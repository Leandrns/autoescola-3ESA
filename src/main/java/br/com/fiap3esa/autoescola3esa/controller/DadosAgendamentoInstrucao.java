package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DadosAgendamentoInstrucao(
        @NotNull
        @JsonAlias("id_aluno")
        Long idAluno,

        @JsonAlias("id_instrutor")
        Long idInstrutor,

        Especialidade especialidade,

        @Future
        @NotNull
        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        @JsonAlias("data_hora")
        LocalDateTime dataHora
) {
}
