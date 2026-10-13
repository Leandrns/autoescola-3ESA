package br.com.fiap3esa.autoescola3esa.service;

import br.com.fiap3esa.autoescola3esa.domain.usuario.*;
import br.com.fiap3esa.autoescola3esa.infra.exception.RegraNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DadosDetalhamentoUsuario cadastrarUsuario(DadosCadastroUsuario dados) {
        if (repository.existsByLogin(dados.login())) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este login.");
        }
        Usuario usuario = new Usuario(dados.login(), passwordEncoder.encode(dados.senha()), dados.perfil());
        Usuario salvo = repository.save(usuario);
        return new DadosDetalhamentoUsuario(salvo);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoUsuario> listarUsuarios(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosDetalhamentoUsuario::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario detalharUsuario(Long id) {
        return new DadosDetalhamentoUsuario(buscar(id));
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizarUsuario(DadosAtualizacaoUsuario dados) {
        Usuario usuario = buscar(dados.id());
        if (dados.senha() != null && !dados.senha().isBlank()) {
            usuario.atualizarSenha(passwordEncoder.encode(dados.senha()));
        }
        if (dados.perfil() != null) {
            usuario.atualizarPerfil(dados.perfil());
        }
        return new DadosDetalhamentoUsuario(repository.save(usuario));
    }

    @Transactional
    public void excluirUsuario(Long id) {
        repository.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("ID do usuário informado não existe!"));
    }
}
