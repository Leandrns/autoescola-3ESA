package br.com.fiap3esa.autoescola3esa.domain.aluno;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlunoTest {

    private DadosEndereco dadosEndereco() {
        return new DadosEndereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000");
    }

    private Aluno novoAluno() {
        return new Aluno(new DadosCadastroAluno(
                "Ana Aluna", "ana@email.com", "11999999999", "12345678901", dadosEndereco()));
    }

    @Test
    @DisplayName("Aluno cadastrado nasce ativo e com os dados informados")
    void alunoNasceAtivo() {
        Aluno aluno = novoAluno();

        assertThat(aluno.isAtivo()).isTrue();
        assertThat(aluno.getNome()).isEqualTo("Ana Aluna");
        assertThat(aluno.getCpf()).isEqualTo("12345678901");
        assertThat(aluno.getEndereco().getCidade()).isEqualTo("São Paulo");
    }

    @Test
    @DisplayName("Atualização altera só os campos informados (nome/telefone) e ignora em branco")
    void atualizacaoParcial() {
        Aluno aluno = novoAluno();

        aluno.atualizarInformacoes(new DadosAtualizacaoAluno(1L, "Ana Maria", "  ", "11911112222", null));

        assertThat(aluno.getNome()).isEqualTo("Ana Maria");
        assertThat(aluno.getEmail()).isEqualTo("ana@email.com");
        assertThat(aluno.getTelefone()).isEqualTo("11911112222");
    }

    @Test
    @DisplayName("Exclusão é lógica: aluno fica inativo")
    void exclusaoLogica() {
        Aluno aluno = novoAluno();

        aluno.excluir();

        assertThat(aluno.isAtivo()).isFalse();
    }
}
