-- A entidade Instrucao mapeia a coluna especialidade.
alter table instrucoes add column especialidade varchar(20);

update instrucoes i
    join instrutores t on t.id = i.instrutor_id
set i.especialidade = t.especialidade;

alter table instrucoes modify column especialidade varchar(20) not null;
