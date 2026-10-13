-- Correção de digitação no enum MotivoCancelamento (ALUNO_DESISIU -> ALUNO_DESISTIU).
update instrucoes
set motivo_cancelamento = 'ALUNO_DESISTIU'
where motivo_cancelamento = 'ALUNO_DESISIU';
