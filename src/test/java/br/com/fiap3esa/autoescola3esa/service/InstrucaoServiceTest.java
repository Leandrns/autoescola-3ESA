package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.*;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários das regras de agendamento/cancelamento. Não precisam de banco de dados.
 * O relógio é fixo: "agora" = segunda-feira, 12/10/2026 às 12:45.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InstrucaoServiceTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 12, 12, 45);
    // quarta-feira, 14/10/2026 às 10:00 (horário válido)
    private static final LocalDateTime QUARTA_10H = LocalDateTime.of(2026, 10, 14, 10, 0);

    @Mock
    private InstrucaoRepository repository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private InstrutorRepository instrutorRepository;

    private InstrucaoService service;
    private Aluno aluno;
    private Instrutor instrutor;

    @BeforeEach
    void setUp() {
        Clock clockFixo = Clock.fixed(AGORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new InstrucaoService(repository, alunoRepository, instrutorRepository, clockFixo);

        aluno = new Aluno(1L, "Ana Aluna", "ana@email.com", "11999999999", "12345678901", endereco(), true);
        instrutor = new Instrutor(2L, "Carlos Instrutor", "carlos@email.com", "11988888888",
                "12345678901", Especialidade.CARROS, endereco(), true);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(instrutorRepository.findById(2L)).thenReturn(Optional.of(instrutor));
        when(repository.save(any(Instrucao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.countByAlunoAndDataHoraBetweenAndStatus(any(), any(), any(), any())).thenReturn(0L);
        when(repository.existsByInstrutorAndDataHoraAndStatus(any(), any(), any())).thenReturn(false);
    }

    private Endereco endereco() {
        return new Endereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000");
    }

    private DadosAgendamentoInstrucao dados(Long idInstrutor, Especialidade especialidade, LocalDateTime dataHora) {
        return new DadosAgendamentoInstrucao(1L, idInstrutor, especialidade, dataHora);
    }

    // ------------------------------------------------------------------ agendamento: sucesso

    @Test
    @DisplayName("Agenda com instrutor informado e grava a especialidade do instrutor")
    void agendarComInstrutorInformado() {
        var resposta = service.agendarInstrucao(dados(2L, null, QUARTA_10H));

        ArgumentCaptor<Instrucao> captor = ArgumentCaptor.forClass(Instrucao.class);
        verify(repository).save(captor.capture());
        Instrucao salva = captor.getValue();

        assertThat(salva.getStatus()).isEqualTo(StatusInstrucao.AGENDADA);
        assertThat(salva.getEspecialidade()).isEqualTo(Especialidade.CARROS);
        assertThat(salva.getDataHora()).isEqualTo(QUARTA_10H);
        assertThat(resposta.nomeInstrutor()).isEqualTo("Carlos Instrutor");
        assertThat(resposta.status()).isEqualTo("AGENDADA");
    }

    @Test
    @DisplayName("Sem instrutor informado, sorteia um instrutor disponível da especialidade")
    void agendarSorteandoInstrutor() {
        when(instrutorRepository.findAllByAtivoTrueAndEspecialidade(Especialidade.CARROS))
                .thenReturn(List.of(instrutor));

        var resposta = service.agendarInstrucao(dados(null, Especialidade.CARROS, QUARTA_10H));

        assertThat(resposta.idInstrutor()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Aceita a última hora do dia (20:00 termina às 21:00)")
    void aceitaUltimoHorarioDoDia() {
        var resposta = service.agendarInstrucao(dados(2L, null, LocalDateTime.of(2026, 10, 14, 20, 0)));

        assertThat(resposta.status()).isEqualTo("AGENDADA");
    }

    // ------------------------------------------------------------------ agendamento: regras

    @Test
    @DisplayName("Rejeita aluno inexistente")
    void rejeitaAlunoInexistente() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, QUARTA_10H)))
                .isInstanceOf(AlunoNotFoundException.class);
    }

    @Test
    @DisplayName("Rejeita aluno inativo")
    void rejeitaAlunoInativo() {
        aluno.excluir();

        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, QUARTA_10H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("aluno inativo");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita horário que não seja hora cheia")
    void rejeitaHorarioQueNaoEHoraCheia() {
        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, LocalDateTime.of(2026, 10, 14, 10, 30))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("hora cheia");
    }

    @Test
    @DisplayName("Rejeita domingo")
    void rejeitaDomingo() {
        // 18/10/2026 é domingo
        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, LocalDateTime.of(2026, 10, 18, 10, 0))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("domingos");
    }

    @Test
    @DisplayName("Rejeita horário antes da abertura (06:00)")
    void rejeitaAntesDaAbertura() {
        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, LocalDateTime.of(2026, 10, 14, 5, 0))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("06:00");
    }

    @Test
    @DisplayName("Rejeita início às 21:00 e às 23:00 (a instrução dura 1h e a escola fecha às 21:00)")
    void rejeitaDepoisDoFechamento() {
        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, LocalDateTime.of(2026, 10, 14, 21, 0))))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, LocalDateTime.of(2026, 10, 14, 23, 0))))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Rejeita agendamento com menos de 30 minutos de antecedência")
    void rejeitaAntecedenciaMenorQue30Minutos() {
        // agora = 12:45; 13:00 está a 15 minutos
        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, LocalDateTime.of(2026, 10, 12, 13, 0))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("30 minutos");
    }

    @Test
    @DisplayName("Rejeita a 3ª instrução do aluno no mesmo dia")
    void rejeitaLimiteDiario() {
        when(repository.countByAlunoAndDataHoraBetweenAndStatus(
                eq(aluno), any(), any(), eq(StatusInstrucao.AGENDADA))).thenReturn(2L);

        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, QUARTA_10H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("duas instruções");
    }

    @Test
    @DisplayName("Rejeita instrutor inexistente")
    void rejeitaInstrutorInexistente() {
        when(instrutorRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, QUARTA_10H)))
                .isInstanceOf(InstrutorNotFoundException.class);
    }

    @Test
    @DisplayName("Rejeita instrutor inativo")
    void rejeitaInstrutorInativo() {
        instrutor.excluir();

        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, QUARTA_10H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("instrutor inativo");
    }

    @Test
    @DisplayName("Rejeita instrutor já ocupado no horário")
    void rejeitaInstrutorOcupado() {
        when(repository.existsByInstrutorAndDataHoraAndStatus(
                instrutor, QUARTA_10H, StatusInstrucao.AGENDADA)).thenReturn(true);

        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, null, QUARTA_10H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já possui uma instrução");
    }

    @Test
    @DisplayName("Rejeita especialidade diferente da do instrutor informado")
    void rejeitaEspecialidadeDivergente() {
        assertThatThrownBy(() -> service.agendarInstrucao(dados(2L, Especialidade.MOTOS, QUARTA_10H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("especialidade");
    }

    @Test
    @DisplayName("Exige especialidade quando o instrutor não é informado")
    void exigeEspecialidadeSemInstrutor() {
        assertThatThrownBy(() -> service.agendarInstrucao(dados(null, null, QUARTA_10H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("especialidade é obrigatória");
    }

    @Test
    @DisplayName("Sem instrutor disponível no sorteio, rejeita o agendamento")
    void rejeitaQuandoNaoHaInstrutorDisponivel() {
        when(instrutorRepository.findAllByAtivoTrueAndEspecialidade(Especialidade.CARROS))
                .thenReturn(List.of(instrutor));
        when(repository.existsByInstrutorAndDataHoraAndStatus(
                instrutor, QUARTA_10H, StatusInstrucao.AGENDADA)).thenReturn(true);

        assertThatThrownBy(() -> service.agendarInstrucao(dados(null, Especialidade.CARROS, QUARTA_10H)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Não há instrutores disponíveis");
    }

    // ------------------------------------------------------------------ cancelamento

    private Instrucao instrucaoAgendadaPara(LocalDateTime dataHora) {
        return new Instrucao(aluno, instrutor, Especialidade.CARROS, dataHora);
    }

    @Test
    @DisplayName("Cancela com 24h ou mais de antecedência e registra motivo e data")
    void cancelaComAntecedenciaSuficiente() {
        // agora = 12/10 12:45; instrução em 13/10 13:00 (24h15min depois)
        Instrucao instrucao = instrucaoAgendadaPara(LocalDateTime.of(2026, 10, 13, 13, 0));
        when(repository.findById(10L)).thenReturn(Optional.of(instrucao));

        var resposta = service.cancelarInstrucao(10L, new DadosCancelamentoInstrucao(MotivoCancelamento.ALUNO_DESISTIU));

        assertThat(resposta.status()).isEqualTo("CANCELADA");
        assertThat(resposta.motivoCancelamento()).isEqualTo("ALUNO_DESISTIU");
        assertThat(resposta.dataCancelamento()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("Rejeita cancelamento com menos de 24h de antecedência")
    void rejeitaCancelamentoTardio() {
        // 13/10 12:00 está a 23h15min de 12/10 12:45
        Instrucao instrucao = instrucaoAgendadaPara(LocalDateTime.of(2026, 10, 13, 12, 0));
        when(repository.findById(10L)).thenReturn(Optional.of(instrucao));

        assertThatThrownBy(() -> service.cancelarInstrucao(10L, new DadosCancelamentoInstrucao(MotivoCancelamento.OUTROS)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("24 horas");
        assertThat(instrucao.getStatus()).isEqualTo(StatusInstrucao.AGENDADA);
    }

    @Test
    @DisplayName("Rejeita cancelar uma instrução já cancelada")
    void rejeitaCancelarDuasVezes() {
        Instrucao instrucao = instrucaoAgendadaPara(LocalDateTime.of(2026, 10, 20, 10, 0));
        instrucao.cancelar(MotivoCancelamento.INSTRUTOR_CANCELOU, AGORA);
        when(repository.findById(10L)).thenReturn(Optional.of(instrucao));

        assertThatThrownBy(() -> service.cancelarInstrucao(10L, new DadosCancelamentoInstrucao(MotivoCancelamento.OUTROS)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já está cancelada");
    }

    @Test
    @DisplayName("Rejeita cancelar instrução inexistente")
    void rejeitaCancelarInstrucaoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelarInstrucao(99L, new DadosCancelamentoInstrucao(MotivoCancelamento.OUTROS)))
                .isInstanceOf(InstrucaoNotFoundException.class);
    }
}
