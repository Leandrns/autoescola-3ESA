package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.instrucao.*;
import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Service
public class InstrucaoService {

    private static final LocalTime ABERTURA = LocalTime.of(6, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(21, 0);
    private static final Duration DURACAO_INSTRUCAO = Duration.ofHours(1);
    private static final Duration ANTECEDENCIA_MINIMA = Duration.ofMinutes(30);
    private static final int MAX_INSTRUCOES_POR_DIA = 2;
    private static final Duration ANTECEDENCIA_MINIMA_CANCELAMENTO = Duration.ofHours(24);

    private final InstrucaoRepository repository;
    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;

    public InstrucaoService(InstrucaoRepository repository,
                            AlunoRepository alunoRepository,
                            InstrutorRepository instrutorRepository) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
    }

    @Transactional
    public DadosDetalhamentoInstrucao agendarInstrucao(DadosAgendamentoInstrucao dados) {
        Aluno aluno = alunoRepository.findById(dados.idAluno())
                .orElseThrow(() -> new AlunoNotFoundException("ID do aluno informado não existe!"));

        validarAlunoAtivo(aluno);
        validarHorarioFuncionamento(dados.dataHora());
        validarAntecedenciaMinima(dados.dataHora());
        validarLimiteDiarioDoAluno(aluno, dados.dataHora());

        Instrutor instrutor = definirInstrutor(dados);

        Instrucao instrucao = new Instrucao(aluno, instrutor, dados.especialidade(), dados.dataHora());
        Instrucao salva = repository.save(instrucao);
        return new DadosDetalhamentoInstrucao(salva);
    }

    @Transactional
    public DadosDetalhamentoInstrucao cancelarInstrucao(Long id, DadosCancelamentoInstrucao dados) {
        Instrucao instrucao = repository.findById(id)
                .orElseThrow(() -> new InstrucaoNotFoundException("ID da instrução informado não existe!"));

        validarInstrucaoAindaAgendada(instrucao);
        validarAntecedenciaMinimaCancelamento(instrucao);

        instrucao.cancelar(dados.motivoCancelamento());
        Instrucao salva = repository.save(instrucao);
        return new DadosDetalhamentoInstrucao(salva);
    }

    // Horário de funcionamento: seg-sáb, 06:00-21:00
    private void validarHorarioFuncionamento(LocalDateTime dataHora) {
        if (dataHora.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RegraNegocioException("A auto-escola não funciona aos domingos.");
        }

        LocalTime inicio = dataHora.toLocalTime();
        LocalTime fim = inicio.plus(DURACAO_INSTRUCAO); // a instrução dura 1h fixa

        if (inicio.isBefore(ABERTURA) || fim.isAfter(FECHAMENTO)) {
            throw new RegraNegocioException(
                    "As instruções só podem começar entre " + ABERTURA + " e " +
                            FECHAMENTO.minusHours(1) + " (a instrução dura 1h e a escola fecha às " + FECHAMENTO + ").");
        }
    }

    // Antecedência mínima de 30 minutos
    private void validarAntecedenciaMinima(LocalDateTime dataHora) {
        if (Duration.between(LocalDateTime.now(), dataHora).compareTo(ANTECEDENCIA_MINIMA) < 0) {
            throw new RegraNegocioException(
                    "As instruções devem ser agendadas com, no mínimo, 30 minutos de antecedência.");
        }
    }

    // Aluno inativo
    private void validarAlunoAtivo(Aluno aluno) {
        if (!aluno.isAtivo()) {
            throw new RegraNegocioException("Não é possível agendar instrução para um aluno inativo.");
        }
    }

    // Máximo de 2 instruções por dia por aluno
    private void validarLimiteDiarioDoAluno(Aluno aluno, LocalDateTime dataHora) {
        LocalDateTime inicioDoDia = dataHora.toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = inicioDoDia.plusDays(1);

        long quantidade = repository.countByAlunoAndDataHoraBetweenAndStatus(aluno, inicioDoDia, fimDoDia, StatusInstrucao.AGENDADA);
        if (quantidade >= MAX_INSTRUCOES_POR_DIA) {
            throw new RegraNegocioException("O aluno já atingiu o limite de duas instruções neste dia.");
        }
    }

    // Instrutor: informado (valida) ou nulo (sorteia)
    private Instrutor definirInstrutor(DadosAgendamentoInstrucao dados) {
        if (dados.idInstrutor() != null) {
            Instrutor instrutor = instrutorRepository.findById(dados.idInstrutor())
                    .orElseThrow(() -> new InstrutorNotFoundException("ID do instrutor informado não existe!"));

            if (!instrutor.isAtivo()) {
                throw new RegraNegocioException("Não é possível agendar instrução com um instrutor inativo.");
            }
            if (repository.existsByInstrutorAndDataHoraAndStatus(instrutor, dados.dataHora(), StatusInstrucao.AGENDADA)) {
                throw new RegraNegocioException("O instrutor já possui uma instrução agendada nesse horário.");
            }
            return instrutor;
        }

        return sortearInstrutorDisponivel(dados);
    }

    // Escolha aleatória de instrutor disponível
    private Instrutor sortearInstrutorDisponivel(DadosAgendamentoInstrucao dados) {
        List<Instrutor> candidatos =
                instrutorRepository.findAllByAtivoTrueAndEspecialidade(dados.especialidade());

        List<Instrutor> disponiveis = candidatos.stream()
                .filter(instrutor -> !repository.existsByInstrutorAndDataHoraAndStatus(instrutor, dados.dataHora(), StatusInstrucao.AGENDADA))
                .toList();

        if (disponiveis.isEmpty()) {
            throw new RegraNegocioException("Não há instrutores disponíveis nesse horário.");
        }

        return disponiveis.get(new Random().nextInt(disponiveis.size()));
    }

    private void validarInstrucaoAindaAgendada(Instrucao instrucao) {
        if (instrucao.getStatus() == StatusInstrucao.CANCELADA) {
            throw new RegraNegocioException("Esta instrução já está cancelada.");
        }
    }

    private void validarAntecedenciaMinimaCancelamento(Instrucao instrucao) {
        Duration antecedencia = Duration.between(LocalDateTime.now(), instrucao.getDataHora());
        if (antecedencia.compareTo(ANTECEDENCIA_MINIMA_CANCELAMENTO) < 0) {
            throw new RegraNegocioException(
                    "O cancelamento só pode ser feito com, no mínimo, 24 horas de antecedência.");
        }
    }
}