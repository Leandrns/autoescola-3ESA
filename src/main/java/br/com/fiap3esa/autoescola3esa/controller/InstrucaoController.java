package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosCancelamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.DadosDetalhamentoInstrucao;
import br.com.fiap3esa.autoescola3esa.service.InstrucaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/instrucoes")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Instruções", description = "Agendamento, cancelamento e consulta de instruções")
public class InstrucaoController {

    private final InstrucaoService service;

    public InstrucaoController(InstrucaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Agenda uma instrução",
            description = "Se o instrutor não for informado, um instrutor disponível da especialidade é sorteado.")
    public ResponseEntity<DadosDetalhamentoInstrucao> agendarInstrucao(
            @RequestBody @Valid DadosAgendamentoInstrucao dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoInstrucao dto = service.agendarInstrucao(dados);
        URI uri = uriBuilder.path("/instrucoes/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PatchMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela uma instrução",
            description = "Exige motivo e antecedência mínima de 24 horas.")
    public ResponseEntity<DadosDetalhamentoInstrucao> cancelarInstrucao(
            @PathVariable Long id,
            @RequestBody @Valid DadosCancelamentoInstrucao dados) {
        return ResponseEntity.ok(service.cancelarInstrucao(id, dados));
    }

    @GetMapping
    @Operation(summary = "Lista as instruções (paginado)")
    public ResponseEntity<Page<DadosDetalhamentoInstrucao>> listarInstrucoes(
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable paginacao) {
        return ResponseEntity.ok(service.listarInstrucoes(paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma instrução")
    public ResponseEntity<DadosDetalhamentoInstrucao> detalharInstrucao(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharInstrucao(id));
    }
}
