package br.com.caminhoremoto.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// O agendamento atualiza as vagas de tempos em tempos (veja CatalogoDeVagas).
@EnableScheduling
@SpringBootApplication
public class CaminhoRemotoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaminhoRemotoApiApplication.class, args);
	}

}
