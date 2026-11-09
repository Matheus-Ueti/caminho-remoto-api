package br.com.caminhoremoto.api.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    // Nunca guardamos a senha em texto: só o resultado do BCrypt.
    @Column(nullable = false)
    private String senhaCriptografada;

    @Column(nullable = false)
    private Instant criadoEm;

    // O JPA precisa de um construtor vazio para montar o objeto a partir do banco.
    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaCriptografada) {
        this.nome = nome;
        this.email = email;
        this.senhaCriptografada = senhaCriptografada;
        this.criadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaCriptografada() {
        return senhaCriptografada;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
