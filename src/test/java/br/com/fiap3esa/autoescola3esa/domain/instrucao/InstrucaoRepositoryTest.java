package br.com.fiap3esa.autoescola3esa.domain.instrucao;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class InstrucaoRepositoryTest {

    private static final LocalDateTime DIA_10H = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Autowired
    private InstrucaoRepository repository;

    @Autowired
    private TestEntityManager em;

    private Aluno aluno;
    private Instrutor instrutor;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco("Rua Teste", "000", "Fundos", "Vila Teste", "Test City", "TS", "01234567");
        aluno = em.persist(new Aluno(null, "Ana", "ana@email.com", "11987654321", "12345678901", endereco, true));
        instrutor = em.persist(new Instrutor(null, "Carlos", "carlos@email.com", "11912345678", "10987654321",
                Especialidade.MOTOS, endereco, true));
    }

    private Instrucao agendar(LocalDateTime dataHora) {
        return em.persistAndFlush(new Instrucao(aluno, instrutor, Especialidade.MOTOS, dataHora));
    }

    @Test
    @DisplayName("Instrutor com instrução AGENDADA no horário está ocupado")
    void instrutorOcupado() {
        agendar(DIA_10H);

        assertThat(repository.existsByInstrutorAndDataHoraAndStatus(instrutor, DIA_10H, StatusInstrucao.AGENDADA)).isTrue();
        assertThat(repository.existsByInstrutorAndDataHoraAndStatus(
                instrutor, DIA_10H.plusHours(1), StatusInstrucao.AGENDADA)).isFalse();
    }

    @Test
    @DisplayName("Instrução CANCELADA libera o horário do instrutor")
    void cancelamentoLiberaHorario() {
        Instrucao instrucao = agendar(DIA_10H);

        instrucao.cancelar(MotivoCancelamento.OUTROS, LocalDateTime.of(2026, 11, 1, 9, 0));
        em.flush();

        assertThat(repository.existsByInstrutorAndDataHoraAndStatus(instrutor, DIA_10H, StatusInstrucao.AGENDADA)).isFalse();
    }

    @Test
    @DisplayName("Conta somente as instruções AGENDADAS do aluno no dia")
    void contaInstrucoesDoDia() {
        agendar(DIA_10H);
        agendar(DIA_10H.withHour(14));
        Instrucao cancelada = agendar(DIA_10H.withHour(16));
        cancelada.cancelar(MotivoCancelamento.ALUNO_DESISTIU, LocalDateTime.of(2026, 11, 1, 9, 0));
        agendar(DIA_10H.plusDays(1)); // outro dia
        em.flush();

        long total = repository.countByAlunoAndDataHoraBetweenAndStatus(
                aluno,
                DIA_10H.toLocalDate().atStartOfDay(),
                DIA_10H.toLocalDate().plusDays(1).atStartOfDay(),
                StatusInstrucao.AGENDADA);

        assertThat(total).isEqualTo(2);
    }
}
