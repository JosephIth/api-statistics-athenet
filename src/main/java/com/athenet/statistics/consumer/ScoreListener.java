package com.athenet.statistics.consumer;

import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.athenet.statistics.event.ScoreEvent;
import com.athenet.statistics.model.ScoreDocument;
import com.athenet.statistics.repository.ScoreRepository;

@Component
public class ScoreListener {

    private static final Logger log = LoggerFactory.getLogger(ScoreListener.class);
    private static final String TIPO_INICIADO = "INICIADO";
    private static final String TIPO_PUNTO = "PUNTO";
    private static final String TIPO_FINALIZADO = "FINALIZADO";

    private final ScoreRepository repositorio;
    private final SimpMessagingTemplate mensajeria;

    public ScoreListener(ScoreRepository repositorio, SimpMessagingTemplate mensajeria) {
        this.repositorio = repositorio;
        this.mensajeria = mensajeria;
    }

    // Hilos = consumidores del grupo dentro de esta instancia (ESTADISTICAS_HILOS)
    @KafkaListener(
            topics = "${app.kafka.topic.marcador:marcador}",
            groupId = "estadisticas",
            concurrency = "${estadisticas.hilos:1}")
    public void escuchar(ConsumerRecord<String, ScoreEvent> registro) {
        ScoreEvent evento = registro.value();
        if (evento == null || evento.idEncuentro() == null) {
            log.warn("[LEE] Mensaje vacío o sin idEncuentro en partición={} offset={}",
                    registro.partition(), registro.offset());
            return;
        }

        log.info("[LEE] grupo=estadisticas particion={} offset={} clave={} tipo={} marcador={}-{}",
                registro.partition(), registro.offset(), registro.key(),
                evento.tipo(), evento.puntosLocal(), evento.puntosVisita());

        Optional<ScoreDocument> existente = repositorio.findByIdEncuentro(evento.idEncuentro());
        if (existente.isPresent() && debeIgnorar(existente.get().tipo(), evento.tipo())) {
            log.warn("[LEE] Evento fuera de orden ignorado para encuentro={}: actual={} entrante={}",
                    evento.idEncuentro(), existente.get().tipo(), evento.tipo());
            return;
        }

        String id = existente.map(ScoreDocument::id).orElse(null);
        ScoreDocument documento = repositorio.save(new ScoreDocument(
                id,
                evento.idEncuentro(),
                evento.equipoLocal(),
                evento.equipoVisita(),
                evento.puntosLocal(),
                evento.puntosVisita(),
                evento.tipo()));

        // Compatibilidad con web pública: topic global
        mensajeria.convertAndSend("/topic/marcador", documento);
        // Soporte multi-partido: topic por encuentro
        mensajeria.convertAndSend("/topic/marcador/" + evento.idEncuentro(), documento);
    }

    // Reglas defensivas frente a reintentos tardíos de INICIADO/PUNTO:
    // - INICIADO no pisa un encuentro que ya tiene PUNTO o FINALIZADO.
    // - PUNTO no pisa un encuentro ya FINALIZADO.
    // - FINALIZADO sí puede pisar otro FINALIZADO (corrección de resultado).
    private static boolean debeIgnorar(String estadoActual, String tipoEntrante) {
        if (TIPO_INICIADO.equals(tipoEntrante)) {
            return TIPO_PUNTO.equals(estadoActual) || TIPO_FINALIZADO.equals(estadoActual);
        }
        if (TIPO_PUNTO.equals(tipoEntrante)) {
            return TIPO_FINALIZADO.equals(estadoActual);
        }
        return false;
    }
}
