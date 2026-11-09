package br.com.caminhoremoto.api.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SegurancaConfig {

    /** Rotas que qualquer pessoa pode chamar, mesmo sem estar logada. */
    public static final List<String> ROTAS_PUBLICAS = List.of("/auth/cadastro", "/auth/login", "/auth/sair");

    private static final String ALGORITMO_DA_CHAVE = "HmacSHA256";

    private final SecretKey chaveDoJwt;
    private final String origemDoFront;

    public SegurancaConfig(
            @Value("${autenticacao.jwt.chave-secreta}") String chaveSecreta,
            @Value("${autenticacao.front.origem}") String origemDoFront) {
        this.chaveDoJwt = new SecretKeySpec(chaveSecreta.getBytes(StandardCharsets.UTF_8), ALGORITMO_DA_CHAVE);
        this.origemDoFront = origemDoFront;
    }

    @Bean
    public SecurityFilterChain regrasDeSeguranca(HttpSecurity http) throws Exception {
        String[] rotasPublicas = ROTAS_PUBLICAS.toArray(String[]::new);

        http
                // Sem sessão no servidor: quem está logado é descoberto pelo JWT a cada pedido.
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // O cookie usa SameSite=Lax, que já bloqueia pedidos vindos de outros sites.
                .csrf(csrf -> csrf.disable())
                // Passamos a regra de CORS direto, porque o nome dela está em português
                // e o Spring só a encontraria sozinho se ela se chamasse "corsConfigurationSource".
                .cors(cors -> cors.configurationSource(regrasDeCors()))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, rotasPublicas).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .bearerTokenResolver(new LeitorDeTokenDoCookie())
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public PasswordEncoder criptografiaDeSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder geradorDeJwt() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveDoJwt));
    }

    @Bean
    public JwtDecoder leitorDeJwt() {
        return NimbusJwtDecoder.withSecretKey(chaveDoJwt).build();
    }

    @Bean
    public CorsConfigurationSource regrasDeCors() {
        CorsConfiguration regras = new CorsConfiguration();
        regras.setAllowedOrigins(List.of(origemDoFront));
        regras.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        regras.setAllowedHeaders(List.of("Content-Type"));
        // Permite que o navegador envie o cookie de login para a API.
        regras.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", regras);
        return origem;
    }
}
