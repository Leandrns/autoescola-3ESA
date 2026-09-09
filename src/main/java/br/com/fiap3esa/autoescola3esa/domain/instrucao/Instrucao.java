package br.com.fiap3esa.autoescola3esa.domain.instrucao;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Especialidade;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "instrucao")
@Table(name = "instrucoes")
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Instrucao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrutor_id")
    private Instrutor instrutor;

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private StatusInstrucao status;

    @Enumerated(EnumType.STRING)
    private MotivoCancelamento motivoCancelamento;

    private LocalDateTime dataCancelamento;

    public Instrucao(Aluno aluno, Instrutor instrutor, Especialidade especialidade, LocalDateTime dataHora) {
        this.aluno = aluno;
        this.instrutor = instrutor;
        this.especialidade = especialidade;
        this.dataHora = dataHora;
        this.status = StatusInstrucao.AGENDADA;
    }

    public void cancelar(MotivoCancelamento motivo) {
        this.status = StatusInstrucao.CANCELADA;
        this.motivoCancelamento = motivo;
        this.dataCancelamento = LocalDateTime.now();
    }
}