package br.com.fiap3esa.autoescola3esa.domain.instrucao;

import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InstrucaoTest {

    private final LocalDateTime dataHora = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    @DisplayName("Nova instrução nasce AGENDADA e sem dados de cancelamento")
    void novaInstrucaoNasceAgendada() {
        Instrucao instrucao = new Instrucao(null, null, Especialidade.MOTOS, dataHora);

        assertThat(instrucao.getStatus()).isEqualTo(StatusInstrucao.AGENDADA);
        assertThat(instrucao.getEspecialidade()).isEqualTo(Especialidade.MOTOS);
        assertThat(instrucao.getMotivoCancelamento()).isNull();
        assertThat(instrucao.getDataCancelamento()).isNull();
    }

    @Test
    @DisplayName("cancelar() muda o status e registra motivo e momento")
    void cancelarRegistraMotivoEMomento() {
        Instrucao instrucao = new Instrucao(null, null, Especialidade.MOTOS, dataHora);
        LocalDateTime momento = LocalDateTime.of(2026, 10, 15, 9, 0);

        instrucao.cancelar(MotivoCancelamento.INSTRUTOR_CANCELOU, momento);

        assertThat(instrucao.getStatus()).isEqualTo(StatusInstrucao.CANCELADA);
        assertThat(instrucao.getMotivoCancelamento()).isEqualTo(MotivoCancelamento.INSTRUTOR_CANCELOU);
        assertThat(instrucao.getDataCancelamento()).isEqualTo(momento);
    }
}
