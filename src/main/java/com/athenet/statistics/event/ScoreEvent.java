package com.athenet.statistics.event;

// Contrato del topic "marcador" publicado por ms-competiciones
public record ScoreEvent(
        Long idEncuentro,
        String equipoLocal,
        String equipoVisita,
        int puntosLocal,
        int puntosVisita,
        String tipo
) {
}
