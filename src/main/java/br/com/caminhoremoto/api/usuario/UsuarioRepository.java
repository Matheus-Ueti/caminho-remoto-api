package br.com.caminhoremoto.api.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// O Spring cria o código destas buscas sozinho, a partir do nome do método.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
