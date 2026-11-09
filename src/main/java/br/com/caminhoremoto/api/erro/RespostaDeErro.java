package br.com.caminhoremoto.api.erro;

import java.util.Map;

/**
 * Formato padrão dos erros da API.
 * "campos" mostra o erro de cada campo do formulário (ex.: {"email": "Informe um e-mail válido."}).
 */
public record RespostaDeErro(String mensagem, Map<String, String> campos) {

    public static RespostaDeErro apenasMensagem(String mensagem) {
        return new RespostaDeErro(mensagem, Map.of());
    }
}
