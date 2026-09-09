package br.com.fiap3esa.autoescola3esa.domain.instrucao;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InstrucaoRepository extends JpaRepository<Instrucao, Long> {

    boolean existsByInstrutorAndDataHoraAndStatus(
            Instrutor instrutor, LocalDateTime dataHora, StatusInstrucao status);

    long countByAlunoAndDataHoraBetweenAndStatus(
            Aluno aluno, LocalDateTime inicio, LocalDateTime fim, StatusInstrucao status);
}