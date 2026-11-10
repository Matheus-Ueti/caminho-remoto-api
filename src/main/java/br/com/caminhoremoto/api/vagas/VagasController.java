package br.com.caminhoremoto.api.vagas;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Rotas públicas: qualquer pessoa vê as vagas, mesmo sem login. */
@RestController
@RequestMapping("/vagas")
public class VagasController {

    private final CatalogoDeVagas catalogoDeVagas;

    public VagasController(CatalogoDeVagas catalogoDeVagas) {
        this.catalogoDeVagas = catalogoDeVagas;
    }

    /** Com ?busca=texto, devolve só as vagas com esse texto no título. */
    @GetMapping
    public List<Vaga> listarVagas(@RequestParam(required = false) String busca) {
        return catalogoDeVagas.buscarVagas(busca);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vaga> buscarVaga(@PathVariable String id) {
        Vaga vaga = catalogoDeVagas.buscarVagaPorId(id);
        if (vaga == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vaga);
    }
}
