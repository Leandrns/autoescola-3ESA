package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.usuario.*;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    @DisplayName("Cadastra usuário gravando a senha criptografada (nunca em texto puro)")
    void cadastrarCriptografaSenha() {
        when(repository.existsByLogin("maria")).thenReturn(false);
        when(passwordEncoder.encode("segredo123")).thenReturn("HASH");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.cadastrarUsuario(new DadosCadastroUsuario("maria", "segredo123", Perfil.USER));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("HASH");
        assertThat(dto.login()).isEqualTo("maria");
        assertThat(dto.perfil()).isEqualTo(Perfil.USER);
    }

    @Test
    @DisplayName("Rejeita login duplicado")
    void rejeitaLoginDuplicado() {
        when(repository.existsByLogin("maria")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarUsuario(new DadosCadastroUsuario("maria", "segredo123", Perfil.USER)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("login");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Atualiza perfil e senha quando informados")
    void atualizarUsuario() {
        Usuario usuario = new Usuario("maria", "HASH_ANTIGO", Perfil.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha")).thenReturn("HASH_NOVO");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.atualizarUsuario(new DadosAtualizacaoUsuario(1L, "novaSenha", Perfil.ADMIN));

        assertThat(usuario.getSenha()).isEqualTo("HASH_NOVO");
        assertThat(dto.perfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    @DisplayName("Detalhar/excluir usuário inexistente lança UsuarioNotFoundException")
    void usuarioInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalharUsuario(99L)).isInstanceOf(UsuarioNotFoundException.class);
        assertThatThrownBy(() -> service.excluirUsuario(99L)).isInstanceOf(UsuarioNotFoundException.class);
    }

    @Test
    @DisplayName("Exclui usuário existente")
    void excluirUsuario() {
        Usuario usuario = new Usuario("maria", "HASH", Perfil.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        service.excluirUsuario(1L);

        verify(repository).delete(usuario);
    }
}
