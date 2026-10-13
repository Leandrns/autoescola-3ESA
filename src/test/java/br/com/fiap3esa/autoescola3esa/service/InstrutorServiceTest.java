package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InstrutorServiceTest {

    @Mock
    private InstrutorRepository repository;

    @InjectMocks
    private InstrutorService service;

    private DadosCadastroInstrutor cadastro() {
        return new DadosCadastroInstrutor("Carlos Instrutor", "carlos@email.com", "11988888888", "12345678901",
                Especialidade.CARROS,
                new DadosEndereco("Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01001000"));
    }

    @Test
    @DisplayName("Cadastra instrutor ativo")
    void cadastrarInstrutor() {
        when(repository.save(any(Instrutor.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.cadastrarInstrutor(cadastro());

        assertThat(dto.nome()).isEqualTo("Carlos Instrutor");
        assertThat(dto.ativo()).isTrue();
        assertThat(dto.especialidade()).isEqualTo(Especialidade.CARROS);
    }

    @Test
    @DisplayName("Detalhar instrutor inexistente lança InstrutorNotFoundException")
    void detalharInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalharInstrutor(99L)).isInstanceOf(InstrutorNotFoundException.class);
    }

    @Test
    @DisplayName("Excluir instrutor faz exclusão lógica")
    void excluirInstrutor() {
        Instrutor instrutor = new Instrutor(cadastro());
        when(repository.findById(1L)).thenReturn(Optional.of(instrutor));

        service.excluirInstrutor(1L);

        assertThat(instrutor.isAtivo()).isFalse();
        verify(repository).save(instrutor);
    }
}
