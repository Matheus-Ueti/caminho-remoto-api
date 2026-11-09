package br.com.caminhoremoto.api.autenticacao.dto;

import br.com.caminhoremoto.api.usuario.Usuario;

/** O que a API devolve sobre o usuário. Nunca inclui a senha. */
public record UsuarioResponse(Long id, String nome, String email) {

    public static UsuarioResponse criarAPartirDe(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
