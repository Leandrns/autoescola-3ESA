package br.com.fiap3esa.autoescola3esa.domain.aluno;

import br.com.fiap3esa.autoescola3esa.domain.endereco.DadosEndereco;

public record DadosDetalhamentoAluno(
        Long id,
        String nome,
        String email,
        String telefone,
        String cpf,
        Categoria categoria,
        DadosEndereco endereco,
        boolean ativo) {
    public DadosDetalhamentoAluno(Aluno aluno) {
        this(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getTelefone(),
                aluno.getCpf(),
                aluno.getCategoria(),
                new DadosEndereco(aluno.getEndereco()),
                aluno.isAtivo());
    }
}