package br.com.caminhoremoto.api.vagas;

/**
 * Uma vaga home office vinda de um site de fora. A candidatura acontece no "link" original.
 * O "id" leva o nome da fonte na frente (ex.: "jooble-123").
 */
public record Vaga(String id, String titulo, String empresa, String local, String salario,
        String resumo, String link, String atualizadaEm) {
}
