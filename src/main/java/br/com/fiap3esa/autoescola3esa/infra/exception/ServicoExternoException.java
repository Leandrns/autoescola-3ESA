package br.com.fiap3esa.autoescola3esa.infra.exception;

/** Falha ao consumir uma API/WebService externo (indisponível, timeout, resposta inválida...). */
public class ServicoExternoException extends RuntimeException {
    public ServicoExternoException(String message, Throwable cause) {
        super(message, cause);
    }
}
