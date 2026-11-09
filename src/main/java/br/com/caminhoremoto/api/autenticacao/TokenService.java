package br.com.caminhoremoto.api.autenticacao;

import br.com.caminhoremoto.api.usuario.Usuario;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final String EMISSOR = "caminho-remoto-api";

    private final JwtEncoder geradorDeJwt;
    private final Duration validade;

    public TokenService(JwtEncoder geradorDeJwt, @Value("${autenticacao.jwt.validade}") Duration validade) {
        this.geradorDeJwt = geradorDeJwt;
        this.validade = validade;
    }

    /** Gera o "crachá digital" do usuário. O id dele vai no campo "sub" (subject) do JWT. */
    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();

        JwtClaimsSet dados = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return geradorDeJwt.encode(JwtEncoderParameters.from(cabecalho, dados)).getTokenValue();
    }
}
