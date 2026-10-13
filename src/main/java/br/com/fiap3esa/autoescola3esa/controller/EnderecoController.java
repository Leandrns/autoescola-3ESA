package br.com.fiap3esa.autoescola3esa.controller;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosConsultaCep;
import br.com.fiap3esa.autoescola3esa.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Endereços", description = "Consulta de endereço por CEP usando a API externa ViaCEP")
public class EnderecoController {

    private final EnderecoService service;

    public EnderecoController(EnderecoService service) {
        this.service = service;
    }

    @GetMapping("/cep/{cep}")
    @Operation(summary = "Consulta endereço pelo CEP",
            description = "Consome a API externa ViaCEP (https://viacep.com.br). Aceita 01001000 ou 01001-000.")
    public ResponseEntity<DadosConsultaCep> consultarCep(@PathVariable String cep) {
        return ResponseEntity.ok(service.consultarPorCep(cep));
    }
}
