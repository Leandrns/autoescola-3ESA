package br.com.fiap3esa.autoescola3esa.domain.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    @DisplayName("O perfil vira a authority ROLE_<PERFIL> usada pelo @PreAuthorize")
    void perfilViraRole() {
        Usuario admin = new Usuario("admin", "hash", Perfil.ADMIN);
        Usuario comum = new Usuario("maria", "hash", Perfil.USER);

        assertThat(admin.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        assertThat(comum.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Implementa UserDetails com login/senha e conta habilitada")
    void implementaUserDetails() {
        Usuario usuario = new Usuario("admin", "hash", Perfil.ADMIN);

        assertThat(usuario.getUsername()).isEqualTo("admin");
        assertThat(usuario.getPassword()).isEqualTo("hash");
        assertThat(usuario.isEnabled()).isTrue();
        assertThat(usuario.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("Atualiza senha e perfil")
    void atualizaSenhaEPerfil() {
        Usuario usuario = new Usuario("maria", "hash1", Perfil.USER);

        usuario.atualizarSenha("hash2");
        usuario.atualizarPerfil(Perfil.ADMIN);

        assertThat(usuario.getSenha()).isEqualTo("hash2");
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.ADMIN);
    }
}
