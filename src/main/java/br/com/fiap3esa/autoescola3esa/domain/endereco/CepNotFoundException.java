package br.com.fiap3esa.autoescola3esa.domain.endereco;

public class CepNotFoundException extends RuntimeException {
    public CepNotFoundException(String message) {
        super(message);
    }
}
