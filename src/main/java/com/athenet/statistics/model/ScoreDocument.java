package com.athenet.statistics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// Un documento por encuentro con su último marcador
@Document("marcadores")
public record ScoreDocument(
        @Id String id,
        @Indexed(unique = true) Long idEncuentro,
        String equipoLocal,
        String equipoVisita,
        int puntosLocal,
        int puntosVisita,
        String tipo
) {
}
