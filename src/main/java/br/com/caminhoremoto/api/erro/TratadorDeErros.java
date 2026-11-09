package br.com.caminhoremoto.api.erro;

import br.com.caminhoremoto.api.autenticacao.CredenciaisInvalidasException;
import br.com.caminhoremoto.api.autenticacao.EmailJaCadastradoException;
import br.com.caminhoremoto.api.autenticacao.SessaoInvalidaException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Transforma os erros da API em respostas claras, em português. */
@RestControllerAdvice
public class TratadorDeErros {

    private static final String MENSAGEM_DE_CAMPOS_INVALIDOS = "Confira os campos do formulário.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaDeErro> tratarCamposInvalidos(MethodArgumentNotValidException erro) {
        Map<String, String> errosPorCampo = new LinkedHashMap<>();
        for (FieldError erroDoCampo : erro.getBindingResult().getFieldErrors()) {
            errosPorCampo.putIfAbsent(erroDoCampo.getField(), erroDoCampo.getDefaultMessage());
        }
        RespostaDeErro resposta = new RespostaDeErro(MENSAGEM_DE_CAMPOS_INVALIDOS, errosPorCampo);
        return ResponseEntity.badRequest().body(resposta);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<RespostaDeErro> tratarEmailJaCadastrado(EmailJaCadastradoException erro) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(RespostaDeErro.apenasMensagem(erro.getMessage()));
    }

    @ExceptionHandler({CredenciaisInvalidasException.class, SessaoInvalidaException.class})
    public ResponseEntity<RespostaDeErro> tratarFaltaDeAutorizacao(RuntimeException erro) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RespostaDeErro.apenasMensagem(erro.getMessage()));
    }
}
