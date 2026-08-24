package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.aluno.Aluno;
import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoRepository;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.Instrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.InstrucaoRepository;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.Instrutor;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/instrucoes")
public class InstrucaoController {
    @Autowired
    private InstrucaoRepository repository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private InstrutorRepository instrutorRepository;

    @PostMapping
    public ResponseEntity agendarInstrucao(@RequestBody @Valid DadosAgendamentoInstrucao dados) {
        Aluno aluno = alunoRepository.findById(dados.idAluno()).orElseThrow();
        Instrutor instrutor = instrutorRepository.findById(dados.idInstrutor()).orElseThrow();
        Instrucao instrucao = new Instrucao(null, aluno, instrutor, dados.dataHora());
        repository.save(instrucao);
        return ResponseEntity.ok().build();
    }
}
