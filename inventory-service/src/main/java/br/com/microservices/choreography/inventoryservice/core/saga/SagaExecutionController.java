package br.com.microservices.choreography.inventoryservice.core.saga;

import br.com.microservices.choreography.inventoryservice.config.kafka.KafkaProperties;
import br.com.microservices.choreography.inventoryservice.core.dto.Event;
import br.com.microservices.choreography.inventoryservice.core.producer.KafkaProducer;
import br.com.microservices.choreography.inventoryservice.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static br.com.microservices.choreography.inventoryservice.core.enums.ETopics.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaExecutionController {

    private static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID: %s | EVENT ID: %s";

    private final KafkaProperties kafkaProperties;

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;

    public void handlerSaga(Event event) {
        switch (event.getStatus()) {
            case SUCCESS -> handlerSuccess(event);
            case ROLLBACK_PENDING -> handlerRollback(event);
            case FAIL -> handlerFail(event);
        }
    }

    private void handlerSuccess(Event event) {
        String topic = kafkaProperties.getTopic().get(NOTIFY_ENDING.getTopic());

        log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {}",
                event.getSource(), topic, createSagaId(event));

        sendEvent(event, topic);

    }

    private void handlerRollback(Event event) {
        String topic = kafkaProperties.getTopic().get(INVENTORY_FAIL.getTopic());

        log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {}",
                event.getSource(), topic, createSagaId(event));

        sendEvent(event, topic);

    }

    private void handlerFail(Event event) {
        String topic = kafkaProperties.getTopic().get(PAYMENT_FAIL.getTopic());

        log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {}",
                event.getSource(), topic, createSagaId(event));

        sendEvent(event, topic);
    }

    private void sendEvent(Event event, String topic) {
        var json = jsonUtil.toJson(event);
        producer.sendEvent(json, topic);
    }

    private String createSagaId(Event event) {
        return String.format(SAGA_LOG_ID, event.getPayload().getId(), event.getTransactionId(), event.getId());
    }
}
