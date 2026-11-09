package br.com.caminhoremoto.api.autenticacao;

import br.com.caminhoremoto.api.autenticacao.dto.CadastroRequest;
import br.com.caminhoremoto.api.autenticacao.dto.LoginRequest;
import br.com.caminhoremoto.api.usuario.Usuario;
import br.com.caminhoremoto.api.usuario.UsuarioRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder criptografiaDeSenha;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder criptografiaDeSenha) {
        this.usuarioRepository = usuarioRepository;
        this.criptografiaDeSenha = criptografiaDeSenha;
    }

    public Usuario cadastrar(CadastroRequest cadastro) {
        String email = padronizarEmail(cadastro.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        String senhaCriptografada = criptografiaDeSenha.encode(cadastro.senha());
        Usuario novoUsuario = new Usuario(cadastro.nome().trim(), email, senhaCriptografada);
        return usuarioRepository.save(novoUsuario);
    }

    public Usuario autenticar(LoginRequest login) {
        String email = padronizarEmail(login.email());
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(CredenciaisInvalidasException::new);

        boolean senhaEstaCorreta = criptografiaDeSenha.matches(login.senha(), usuario.getSenhaCriptografada());
        if (!senhaEstaCorreta) {
            throw new CredenciaisInvalidasException();
        }
        return usuario;
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(SessaoInvalidaException::new);
    }

    // "Maria@Email.com " e "maria@email.com" são o mesmo e-mail.
    private String padronizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
