package com.athenet.statistics.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.athenet.statistics.event.ScoreEvent;

// Segundo grupo opcional para auditoría/diagnóstico en Kafka UI (AUDIT_ENABLED=true)
@Component
@ConditionalOnProperty(name = "app.kafka.audit-enabled", havingValue = "true")
public class AuditListener {

    private static final Logger log = LoggerFactory.getLogger(AuditListener.class);

    @KafkaListener(topics = "${app.kafka.topic.marcador:marcador}", groupId = "auditoria")
    public void auditar(ConsumerRecord<String, ScoreEvent> registro) {
        ScoreEvent evento = registro.value();
        log.info("[AUDITORIA] grupo=auditoria particion={} offset={} clave={} tipo={}",
                registro.partition(), registro.offset(), registro.key(),
                evento != null ? evento.tipo() : null);
    }
}
