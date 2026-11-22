package com.athenet.statistics.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.athenet.statistics.event.ScoreEvent;
import com.athenet.statistics.model.ScoreDocument;
import com.athenet.statistics.repository.ScoreRepository;

@ExtendWith(MockitoExtension.class)
class ScoreListenerTest {

    @Mock ScoreRepository repositorio;
    @Mock SimpMessagingTemplate mensajeria;

    ScoreListener listener;

    @BeforeEach
    void setup() {
        listener = new ScoreListener(repositorio, mensajeria);
    }

    @Test
    void guardaEnMongoYEmiteEnTopicoGlobalYPorEncuentro() {
        ScoreEvent evento = new ScoreEvent(31L, "USACH", "U. de Chile", 2, 1, "PUNTO");
        ScoreDocument guardado = new ScoreDocument("mongo-1", 31L, "USACH", "U. de Chile", 2, 1, "PUNTO");
        when(repositorio.findByIdEncuentro(31L)).thenReturn(Optional.empty());
        when(repositorio.save(any(ScoreDocument.class))).thenReturn(guardado);

        listener.escuchar(new ConsumerRecord<>("marcador", 0, 1L, "31", evento));

        verify(repositorio).save(new ScoreDocument(null, 31L, "USACH", "U. de Chile", 2, 1, "PUNTO"));
        verify(mensajeria).convertAndSend("/topic/marcador", guardado);
        verify(mensajeria).convertAndSend("/topic/marcador/31", guardado);
    }

    @Test
    void ignoraIniciadoTardioCuandoYaHayPuntosOFinalizado() {
        ScoreDocument enCurso = new ScoreDocument("mongo-1", 31L, "USACH", "U. de Chile", 1, 0, "PUNTO");
        when(repositorio.findByIdEncuentro(31L)).thenReturn(Optional.of(enCurso));

        ScoreEvent iniciadoTardio = new ScoreEvent(31L, "USACH", "U. de Chile", 0, 0, "INICIADO");
        listener.escuchar(new ConsumerRecord<>("marcador", 0, 2L, "31", iniciadoTardio));

        verify(repositorio, never()).save(any());
        verifyNoInteractions(mensajeria);
    }

    @Test
    void ignoraPuntoTardioCuandoYaFinalizoPeroAceptaCorreccionFinalizada() {
        ScoreDocument finalizado = new ScoreDocument("mongo-1", 31L, "USACH", "U. de Chile", 2, 1, "FINALIZADO");
        when(repositorio.findByIdEncuentro(31L)).thenReturn(Optional.of(finalizado));

        ScoreEvent puntoTardio = new ScoreEvent(31L, "USACH", "U. de Chile", 2, 0, "PUNTO");
        listener.escuchar(new ConsumerRecord<>("marcador", 0, 3L, "31", puntoTardio));
        verify(repositorio, never()).save(any());
        verifyNoInteractions(mensajeria);

        ScoreEvent correccion = new ScoreEvent(31L, "USACH", "U. de Chile", 3, 1, "FINALIZADO");
        ScoreDocument corregido = new ScoreDocument("mongo-1", 31L, "USACH", "U. de Chile", 3, 1, "FINALIZADO");
        when(repositorio.save(any(ScoreDocument.class))).thenReturn(corregido);

        listener.escuchar(new ConsumerRecord<>("marcador", 0, 4L, "31", correccion));

        verify(repositorio).save(corregido);
        verify(mensajeria).convertAndSend("/topic/marcador", corregido);
        verify(mensajeria).convertAndSend("/topic/marcador/31", corregido);
    }
}
