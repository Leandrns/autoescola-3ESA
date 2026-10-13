package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.aluno.*;
import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlunoServiceTest {

    @Mock
    private AlunoRepository repository;

    @InjectMocks
    private AlunoService service;

    private Aluno aluno() {
        return new Aluno(new DadosCadastroAluno("Ana Aluna", "ana@email.com", "11999999999", "12345678901",
                new DadosEndereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000")));
    }

    @Test
    @DisplayName("Cadastra aluno e devolve os dados detalhados")
    void cadastrarAluno() {
        when(repository.save(any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.cadastrarAluno(new DadosCadastroAluno("Ana Aluna", "ana@email.com", "11999999999",
                "12345678901", new DadosEndereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000")));

        assertThat(dto.nome()).isEqualTo("Ana Aluna");
        assertThat(dto.ativo()).isTrue();
    }

    @Test
    @DisplayName("Lista somente alunos ativos")
    void listarAlunosAtivos() {
        var pagina = PageRequest.of(0, 10);
        when(repository.findAllByAtivoTrue(pagina)).thenReturn(new PageImpl<>(List.of(aluno()), pagina, 1));

        var resultado = service.listarAlunos(pagina);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo("Ana Aluna");
    }

    @Test
    @DisplayName("Detalhar aluno inexistente lança AlunoNotFoundException")
    void detalharInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalharAluno(99L)).isInstanceOf(AlunoNotFoundException.class);
    }

    @Test
    @DisplayName("Excluir aluno inativa o registro (exclusão lógica)")
    void excluirAluno() {
        Aluno aluno = aluno();
        when(repository.findById(1L)).thenReturn(Optional.of(aluno));

        service.excluirAluno(1L);

        assertThat(aluno.isAtivo()).isFalse();
        verify(repository).save(aluno);
    }
}
