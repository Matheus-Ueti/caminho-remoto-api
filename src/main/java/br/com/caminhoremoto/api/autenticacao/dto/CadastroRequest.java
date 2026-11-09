package br.com.caminhoremoto.api.autenticacao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank(message = "Informe seu nome.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "Informe um e-mail válido.")
        String email,

        // O BCrypt só considera os primeiros 72 caracteres da senha.
        @NotBlank(message = "Informe uma senha.")
        @Size(min = 8, max = 72, message = "A senha precisa ter entre 8 e 72 caracteres.")
        String senha) {
}
