package br.com.fiap3esa.autoescola3esa.domain.usuario;

/** Nunca expõe a senha (nem o hash). */
public record DadosDetalhamentoUsuario(
        Long id,
        String login,
        Perfil perfil) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin(), usuario.getPerfil());
    }
}
