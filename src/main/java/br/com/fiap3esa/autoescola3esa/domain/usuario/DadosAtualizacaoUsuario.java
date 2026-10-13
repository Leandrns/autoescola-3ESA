package br.com.fiap3esa.autoescola3esa.domain.usuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoUsuario(
        @NotNull
        Long id,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha,

        Perfil perfil) {
}
