-- A entidade Aluno mapeia telefone e ativo, e o cadastro não envia categoria.
alter table alunos drop column categoria;
alter table alunos add column telefone varchar(20) not null default '';
alter table alunos add column ativo tinyint not null default 1;
