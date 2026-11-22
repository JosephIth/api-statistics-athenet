package com.athenet.statistics.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.athenet.statistics.model.ScoreDocument;

public interface ScoreRepository extends MongoRepository<ScoreDocument, String> {

    Optional<ScoreDocument> findByIdEncuentro(Long idEncuentro);

    // Compatibilidad con la web pública: primer encuentro que no está finalizado
    Optional<ScoreDocument> findFirstByTipoNot(String tipo);
}
