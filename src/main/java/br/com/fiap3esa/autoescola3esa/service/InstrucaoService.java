package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.*;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
    private final Clock clock;

    public InstrucaoService(InstrucaoRepository repository,
                            AlunoRepository alunoRepository,
                            InstrutorRepository instrutorRepository,
                            Clock clock) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ agendamento

    @Transactional
    public DadosDetalhamentoInstrucao agendarInstrucao(DadosAgendamentoInstrucao dados) {
        Aluno aluno = alunoRepository.findById(dados.idAluno())
                .orElseThrow(() -> new AlunoNotFoundException("ID do aluno informado não existe!"));

        validarAlunoAtivo(aluno);
        validarHoraInteira(dados.dataHora());
        validarHorarioFuncionamento(dados.dataHora());
        validarAntecedenciaMinima(dados.dataHora());
        validarLimiteDiarioDoAluno(aluno, dados.dataHora());

        Instrutor instrutor = definirInstrutor(dados);

        Instrucao instrucao = new Instrucao(aluno, instrutor, instrutor.getEspecialidade(), dados.dataHora());
        Instrucao salva = repository.save(instrucao);
        return new DadosDetalhamentoInstrucao(salva);
    }

    // ------------------------------------------------------------------ cancelamento

    @Transactional
    public DadosDetalhamentoInstrucao cancelarInstrucao(Long id, DadosCancelamentoInstrucao dados) {
        Instrucao instrucao = repository.findById(id)
                .orElseThrow(() -> new InstrucaoNotFoundException("ID da instrução informado não existe!"));

        validarInstrucaoAindaAgendada(instrucao);
        validarAntecedenciaMinimaCancelamento(instrucao);

        instrucao.cancelar(dados.motivoCancelamento(), LocalDateTime.now(clock));
        Instrucao salva = repository.save(instrucao);
        return new DadosDetalhamentoInstrucao(salva);
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoInstrucao> listarInstrucoes(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosDetalhamentoInstrucao::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoInstrucao detalharInstrucao(Long id) {
        Instrucao instrucao = repository.findById(id)
                .orElseThrow(() -> new InstrucaoNotFoundException("ID da instrução informado não existe!"));
        return new DadosDetalhamentoInstrucao(instrucao);
    }

    // ------------------------------------------------------------------ regras de agendamento

    // Aluno inativo
    private void validarAlunoAtivo(Aluno aluno) {
        if (!aluno.isAtivo()) {
            throw new RegraNegocioException("Não é possível agendar instrução para um aluno inativo.");
        }
    }

    // Instruções sempre em hora cheia (a duração fixa de 1h evita sobreposição parcial de horários)
    private void validarHoraInteira(LocalDateTime dataHora) {
        if (dataHora.getMinute() != 0) {
            throw new RegraNegocioException("O horário deve ser em hora cheia (ex.: 09:00, 13:00, 14:00).");
        }
    }

    // Horário de funcionamento: seg-sáb, 06:00-21:00. A instrução dura 1h, então o último início é 20:00.
    private void validarHorarioFuncionamento(LocalDateTime dataHora) {
        if (dataHora.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RegraNegocioException("A auto-escola não funciona aos domingos.");
        }

        LocalTime inicio = dataHora.toLocalTime();
        LocalTime ultimoInicioPermitido = FECHAMENTO.minus(DURACAO_INSTRUCAO);

        if (inicio.isBefore(ABERTURA) || inicio.isAfter(ultimoInicioPermitido)) {
            throw new RegraNegocioException(
                    "As instruções só podem começar entre " + ABERTURA + " e " + ultimoInicioPermitido +
                            " (a instrução dura 1h e a escola fecha às " + FECHAMENTO + ").");
        }
    }

    // Antecedência mínima de 30 minutos
    private void validarAntecedenciaMinima(LocalDateTime dataHora) {
        if (Duration.between(LocalDateTime.now(clock), dataHora).compareTo(ANTECEDENCIA_MINIMA) < 0) {
            throw new RegraNegocioException(
                    "As instruções devem ser agendadas com, no mínimo, 30 minutos de antecedência.");
        }
    }

    // Máximo de 2 instruções (agendadas) por dia por aluno
    private void validarLimiteDiarioDoAluno(Aluno aluno, LocalDateTime dataHora) {
        LocalDateTime inicioDoDia = dataHora.toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = inicioDoDia.plusDays(1);

        long quantidade = repository.countByAlunoAndDataHoraBetweenAndStatus(
                aluno, inicioDoDia, fimDoDia, StatusInstrucao.AGENDADA);
        if (quantidade >= MAX_INSTRUCOES_POR_DIA) {
            throw new RegraNegocioException("O aluno já atingiu o limite de duas instruções neste dia.");
        }
    }

    // Instrutor: informado (valida) ou nulo (sorteia entre os disponíveis da especialidade)
    private Instrutor definirInstrutor(DadosAgendamentoInstrucao dados) {
        if (dados.idInstrutor() != null) {
            Instrutor instrutor = instrutorRepository.findById(dados.idInstrutor())
                    .orElseThrow(() -> new InstrutorNotFoundException("ID do instrutor informado não existe!"));

            if (!instrutor.isAtivo()) {
                throw new RegraNegocioException("Não é possível agendar instrução com um instrutor inativo.");
            }
            if (dados.especialidade() != null && dados.especialidade() != instrutor.getEspecialidade()) {
                throw new RegraNegocioException(
                        "A especialidade informada não corresponde à especialidade do instrutor.");
            }
            if (instrutorOcupado(instrutor, dados.dataHora())) {
                throw new RegraNegocioException("O instrutor já possui uma instrução agendada nesse horário.");
            }
            return instrutor;
        }

        if (dados.especialidade() == null) {
            throw new RegraNegocioException("A especialidade é obrigatória quando o instrutor não é informado.");
        }
        return sortearInstrutorDisponivel(dados.especialidade(), dados.dataHora());
    }

    private Instrutor sortearInstrutorDisponivel(Especialidade especialidade, LocalDateTime dataHora) {
        List<Instrutor> disponiveis = instrutorRepository.findAllByAtivoTrueAndEspecialidade(especialidade)
                .stream()
                .filter(instrutor -> !instrutorOcupado(instrutor, dataHora))
                .toList();

        if (disponiveis.isEmpty()) {
            throw new RegraNegocioException("Não há instrutores disponíveis nesse horário.");
        }

        return disponiveis.get(ThreadLocalRandom.current().nextInt(disponiveis.size()));
    }

    private boolean instrutorOcupado(Instrutor instrutor, LocalDateTime dataHora) {
        return repository.existsByInstrutorAndDataHoraAndStatus(instrutor, dataHora, StatusInstrucao.AGENDADA);
    }

    // ------------------------------------------------------------------ regras de cancelamento

    private void validarInstrucaoAindaAgendada(Instrucao instrucao) {
        if (instrucao.getStatus() == StatusInstrucao.CANCELADA) {
            throw new RegraNegocioException("Esta instrução já está cancelada.");
        }
    }

    // Cancelamento somente com 24h (ou mais) de antecedência
    private void validarAntecedenciaMinimaCancelamento(Instrucao instrucao) {
        Duration antecedencia = Duration.between(LocalDateTime.now(clock), instrucao.getDataHora());
        if (antecedencia.compareTo(ANTECEDENCIA_MINIMA_CANCELAMENTO) < 0) {
            throw new RegraNegocioException(
                    "O cancelamento só pode ser feito com, no mínimo, 24 horas de antecedência.");
        }
    }
}
