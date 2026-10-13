package br.com.fiap3esa.autoescola3esa.domain.instrutor;

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

/** Requer o MySQL local (banco autoescola3esa_test é criado automaticamente - ver application-test.properties). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class InstrutorRepositoryTest {

    @Autowired
    private InstrutorRepository repository;

    @Autowired
    private TestEntityManager em;

    private Instrutor persistir(String nome, String email, String cnh, Especialidade especialidade, boolean ativo) {
        Endereco endereco = new Endereco("Rua Teste", "000", "Fundos", "Vila Teste", "Test City", "TS", "01234567");
        Instrutor instrutor = new Instrutor(null, nome, email, "11912345678", cnh, especialidade, endereco, ativo);
        return em.persistAndFlush(instrutor);
    }

    @Test
    @DisplayName("findAllByAtivoTrue devolve apenas instrutores ativos")
    void listaSomenteAtivos() {
        persistir("Ativo", "ativo@email.com", "11111111111", Especialidade.CARROS, true);
        persistir("Inativo", "inativo@email.com", "22222222222", Especialidade.CARROS, false);

        var pagina = repository.findAllByAtivoTrue(PageRequest.of(0, 10));

        assertThat(pagina.getContent()).extracting(Instrutor::getNome).containsExactly("Ativo");
    }

    @Test
    @DisplayName("findAllByAtivoTrueAndEspecialidade filtra por especialidade e ignora inativos")
    void filtraPorEspecialidade() {
        persistir("Moto Ativo", "moto@email.com", "33333333333", Especialidade.MOTOS, true);
        persistir("Moto Inativo", "moto2@email.com", "44444444444", Especialidade.MOTOS, false);
        persistir("Carro Ativo", "carro@email.com", "55555555555", Especialidade.CARROS, true);

        var motos = repository.findAllByAtivoTrueAndEspecialidade(Especialidade.MOTOS);

        assertThat(motos).extracting(Instrutor::getNome).containsExactly("Moto Ativo");
    }
}
