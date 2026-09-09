package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosCancelamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosDetalhamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.service.InstrucaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/instrucoes")
public class InstrucaoController {

    private final InstrucaoService service;

    public InstrucaoController(InstrucaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DadosDetalhamentoInstrucao> agendarInstrucao(
            @RequestBody @Valid DadosAgendamentoInstrucao dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoInstrucao dto = service.agendarInstrucao(dados);
        URI uri = uriBuilder.path("/instrucoes/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PatchMapping("/{id}/cancelamento")
    public ResponseEntity<DadosDetalhamentoInstrucao> cancelarInstrucao(
            @PathVariable Long id,
            @RequestBody @Valid DadosCancelamentoInstrucao dados) {
        return ResponseEntity.ok(service.cancelarInstrucao(id, dados));
    }
}
