package br.com.fiap3esa.autoescola3esa.domain.instrucao;

import java.time.LocalDateTime;

public record DadosDetalhamentoInstrucao(
        Long id,
        Long idAluno,
        String nomeAluno,
        Long idInstrutor,
        String nomeInstrutor,
        String especialidade,
        LocalDateTime dataHora,
        String status,
        String motivoCancelamento,
        LocalDateTime dataCancelamento
) {
    public DadosDetalhamentoInstrucao(Instrucao instrucao) {
        this(
                instrucao.getId(),
                instrucao.getAluno().getId(),
                instrucao.getAluno().getNome(),
                instrucao.getInstrutor().getId(),
                instrucao.getInstrutor().getNome(),
                instrucao.getEspecialidade().name(),
                instrucao.getDataHora(),
                instrucao.getStatus().name(),
                instrucao.getMotivoCancelamento() != null ? instrucao.getMotivoCancelamento().name() : null,
                instrucao.getDataCancelamento()
        );
    }
}