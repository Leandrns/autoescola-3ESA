-- Como o cancelamento é lógico (status = CANCELADA), a linha cancelada continua na tabela.
-- A constraint unique (instrutor_id, data_hora) impediria reagendar o mesmo horário depois de um cancelamento.
-- A regra de conflito continua garantida no InstrucaoService (considera apenas status AGENDADA).

-- Índice comum criado ANTES de remover o unique, para a FK de instrutor_id continuar com índice de apoio.
create index idx_instrucoes_instrutor_data_hora on instrucoes (instrutor_id, data_hora);

alter table instrucoes drop index uk_instrucoes_instrutor_data_hora;
