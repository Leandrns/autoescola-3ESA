package br.com.fiap3esa.autoescola3esa.infra.exception;

import br.com.fiap3esa.autoescola3esa.domain.aluno.AlunoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.endereco.CepNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.instrucao.InstrucaoNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3esa.autoescola3esa.domain.usuario.UsuarioNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Mapeia exceções para status HTTP coerentes.
 * Importante: NÃO existe um handler genérico para Exception. Ele engoliria erros de segurança
 * (AccessDeniedException viraria 422) e de JSON inválido (viraria 422 em vez de 400).
 */
@RestControllerAdvice
public class TratadorGlobalErros {

    // 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> tratarEntityNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({
            AlunoNotFoundException.class,
            InstrutorNotFoundException.class,
            InstrucaoNotFoundException.class,
            UsuarioNotFoundException.class,
            CepNotFoundException.class})
    public ResponseEntity<DadosMensagem> tratarNaoEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DadosMensagem(e.getMessage()));
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosBadRequest>> tratarBadRequest(MethodArgumentNotValidException e) {
        List<FieldError> erros = e.getFieldErrors();
        return ResponseEntity.badRequest().body(erros.stream().map(DadosBadRequest::new).toList());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DadosMensagem> tratarCorpoIlegivel() {
        return ResponseEntity.badRequest()
                .body(new DadosMensagem("Corpo da requisição ausente ou inválido (JSON, datas ou enums incorretos)."));
    }

    // 401 / 403
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DadosMensagem> tratarAutenticacao() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DadosMensagem("Login ou senha inválidos."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DadosMensagem> tratarAcessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new DadosMensagem("Acesso negado: seu perfil não tem permissão para este recurso."));
    }

    // 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DadosMensagem> tratarIntegridade() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new DadosMensagem("Violação de integridade: registro duplicado ou referência inválida."));
    }

    // 422
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<DadosMensagem> tratarRegraNegocio(RegraNegocioException e) {
        return ResponseEntity.unprocessableContent().body(new DadosMensagem(e.getMessage()));
    }

    // 502
    @ExceptionHandler(ServicoExternoException.class)
    public ResponseEntity<DadosMensagem> tratarServicoExterno(ServicoExternoException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new DadosMensagem(e.getMessage()));
    }

    private record DadosBadRequest(String field, String message) {
        public DadosBadRequest(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }

    private record DadosMensagem(String message) {
    }
}
