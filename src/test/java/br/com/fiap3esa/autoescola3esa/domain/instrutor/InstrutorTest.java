package br.com.fiap3esa.autoescola3esa.domain.instrutor;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrutorTest {

    private Instrutor novoInstrutor() {
        return new Instrutor(new DadosCadastroInstrutor(
                "Carlos Instrutor", "carlos@email.com", "11988888888", "12345678901", Especialidade.CARROS,
                new DadosEndereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000")));
    }

    @Test
    @DisplayName("Instrutor cadastrado nasce ativo")
    void instrutorNasceAtivo() {
        Instrutor instrutor = novoInstrutor();

        assertThat(instrutor.isAtivo()).isTrue();
        assertThat(instrutor.getEspecialidade()).isEqualTo(Especialidade.CARROS);
    }

    @Test
    @DisplayName("Atualização altera especialidade e telefone e preserva o restante")
    void atualizacaoParcial() {
        Instrutor instrutor = novoInstrutor();

        instrutor.atualizarInformacoes(new DadosAtualizacaoInstrutor(1L, null, null, "11977776666",
                Especialidade.MOTOS, null));

        assertThat(instrutor.getEspecialidade()).isEqualTo(Especialidade.MOTOS);
        assertThat(instrutor.getTelefone()).isEqualTo("11977776666");
        assertThat(instrutor.getNome()).isEqualTo("Carlos Instrutor");
    }

    @Test
    @DisplayName("Exclusão é lógica: instrutor fica inativo")
    void exclusaoLogica() {
        Instrutor instrutor = novoInstrutor();

        instrutor.excluir();

        assertThat(instrutor.isAtivo()).isFalse();
    }
}
