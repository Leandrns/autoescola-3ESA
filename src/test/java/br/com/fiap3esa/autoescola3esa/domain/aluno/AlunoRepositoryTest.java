package br.com.fiap3esa.autoescola3esa.domain.aluno;

import br.com.fiap3esa.autoescola3esa.domain.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AlunoRepositoryTest {

    @Autowired
    private AlunoRepository repository;

    @Autowired
    private TestEntityManager em;

    private Aluno persistir(String nome, String email, String cpf, boolean ativo) {
        Endereco endereco = new Endereco("Rua Teste", "000", "Fundos", "Vila Teste", "Test City", "TS", "01234567");
        Aluno aluno = new Aluno(null, nome, email, "11987654321", cpf, endereco, ativo);
        return em.persistAndFlush(aluno);
    }

    @Test
    @DisplayName("findAllByAtivoTrue devolve apenas alunos ativos")
    void listaSomenteAtivos() {
        persistir("Ativa", "ativa@email.com", "11111111111", true);
        persistir("Inativa", "inativa@email.com", "22222222222", false);

        var pagina = repository.findAllByAtivoTrue(PageRequest.of(0, 10));

        assertThat(pagina.getContent()).extracting(Aluno::getNome).containsExactly("Ativa");
    }

    @Test
    @DisplayName("Persiste e recupera o aluno com endereço embutido")
    void persisteERecupera() {
        Aluno salvo = persistir("Ana", "ana@email.com", "33333333333", true);
        em.clear();

        Aluno recuperado = repository.findById(salvo.getId()).orElseThrow();

        assertThat(recuperado.getCpf()).isEqualTo("33333333333");
        assertThat(recuperado.getEndereco().getCidade()).isEqualTo("Test City");
        assertThat(recuperado.isAtivo()).isTrue();
    }
}
