package br.com.fiap3esa.autoescola3esa.domain.aluno;

public record DadosListagemAluno(
        Long id,
        String nome,
        String email) {
    public DadosListagemAluno(Aluno aluno) {
        this(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail()
        );
    }
}