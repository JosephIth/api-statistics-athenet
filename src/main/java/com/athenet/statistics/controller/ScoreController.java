package com.athenet.statistics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.athenet.statistics.model.ScoreDocument;
import com.athenet.statistics.repository.ScoreRepository;

@RestController
@RequestMapping("/marcador")
public class ScoreController {

    private final ScoreRepository repositorio;

    public ScoreController(ScoreRepository repositorio) {
        this.repositorio = repositorio;
    }

    // Compatibilidad con web pública: primer encuentro en curso (204 si no hay ninguno)
    @GetMapping("/actual")
    public ResponseEntity<ScoreDocument> actual() {
        return repositorio.findFirstByTipoNot("FINALIZADO")
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // Estado actual de un encuentro específico (soporta varios partidos simultáneos)
    @GetMapping("/{idEncuentro}")
    public ResponseEntity<ScoreDocument> porEncuentro(@PathVariable Long idEncuentro) {
        return repositorio.findByIdEncuentro(idEncuentro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
