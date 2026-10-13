package br.com.fiap3esa.autoescola3esa.domain.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("findByLogin devolve o usuário (UserDetails) com o perfil gravado")
    void findByLogin() {
        em.persistAndFlush(new Usuario("admin.teste", "HASH", Perfil.ADMIN));

        UserDetails encontrado = repository.findByLogin("admin.teste");

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("findByLogin devolve null para login inexistente e existsByLogin diferencia os casos")
    void loginInexistente() {
        em.persistAndFlush(new Usuario("existe", "HASH", Perfil.USER));

        assertThat(repository.findByLogin("nao.existe")).isNull();
        assertThat(repository.existsByLogin("existe")).isTrue();
        assertThat(repository.existsByLogin("nao.existe")).isFalse();
    }
}
