package br.com.caminhoremoto.api.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Regras de segurança da API.
 *
 * O login é feito pelo Keycloak. A API faz a ponte: manda a pessoa para a tela do Keycloak,
 * recebe a resposta e guarda quem está logado numa sessão (cookie protegido).
 * Assim, os tokens do Keycloak nunca chegam ao navegador.
 *
 * Rotas criadas pelo próprio Spring Security:
 * - GET /oauth2/authorization/keycloak  → abre a tela de login do Keycloak (com ?cadastro=true, a de criar conta)
 * - GET /login/oauth2/code/keycloak     → o Keycloak devolve a pessoa para cá depois do login
 * - GET /auth/sair                      → encerra a sessão aqui e no Keycloak
 */
@Configuration
public class SegurancaConfig {

    private static final String ROTA_DE_SAIDA = "/auth/sair";
    private static final String PAGINA_DO_FRONT_APOS_LOGIN = "/aluno";

    private final String origemDoFront;

    public SegurancaConfig(@Value("${autenticacao.front.origem}") String origemDoFront) {
        this.origemDoFront = origemDoFront;
    }

    @Bean
    public SecurityFilterChain regrasDeSeguranca(
            HttpSecurity http,
            ResolvedorDoPedidoDeLogin resolvedorDoPedidoDeLogin,
            SaidaDoKeycloak saidaDoKeycloak) throws Exception {
        http
                // Passamos a regra de CORS direto, porque o nome dela está em português
                // e o Spring só a encontraria sozinho se ela se chamasse "corsConfigurationSource".
                .cors(cors -> cors.configurationSource(regrasDeCors()))
                // O cookie de sessão usa SameSite=Lax, que já bloqueia pedidos vindos de outros sites.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(rotas -> rotas.anyRequest().authenticated())
                // Quem não está logado recebe 401 em vez de ser redirecionado: o front decide o que mostrar.
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(login -> login
                        .authorizationEndpoint(pedido -> pedido.authorizationRequestResolver(resolvedorDoPedidoDeLogin))
                        .defaultSuccessUrl(origemDoFront + PAGINA_DO_FRONT_APOS_LOGIN, true)
                        .failureUrl(origemDoFront))
                .logout(saida -> saida
                        .logoutUrl(ROTA_DE_SAIDA)
                        .logoutSuccessHandler(saidaDoKeycloak));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource regrasDeCors() {
        CorsConfiguration regras = new CorsConfiguration();
        regras.setAllowedOrigins(List.of(origemDoFront));
        regras.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        regras.setAllowedHeaders(List.of("Content-Type"));
        // Permite que o navegador envie o cookie de sessão para a API.
        regras.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", regras);
        return origem;
    }
}
