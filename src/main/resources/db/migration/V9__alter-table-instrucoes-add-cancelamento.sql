alter table instrucoes
    add column status varchar(20) not null default 'AGENDADA',
    add column motivo_cancelamento varchar(30),
    add column data_cancelamento timestamp;