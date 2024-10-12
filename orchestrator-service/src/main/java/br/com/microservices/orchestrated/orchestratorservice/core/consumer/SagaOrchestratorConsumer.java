package br.com.microservices.orchestrated.orchestratorservice.core.consumer;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.service.OrchestrationService;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SagaOrchestratorConsumer {

    private final OrchestrationService service;

    private final JsonUtil jsonUtil;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.start-saga}")

    public void consumerStartSagaEvent(String payload) {
        log.info("Receiving notification event {} from start-saga topic", payload);

        Event event = jsonUtil.toEvent(payload);

        service.startSaga(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.orchestrator}")

    public void consumerOrchestratorEvent(String payload) {
        log.info("Receiving notification event {} from orchestrator topic", payload);

        Event event = jsonUtil.toEvent(payload);

        service.continueSaga(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-success}")

    public void consumerFinishSuccessEvent(String payload) {
        log.info("Receiving notification event {} from finish-success topic", payload);

        Event event = jsonUtil.toEvent(payload);

        service.finishSagaSuccess(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-fail}")

    public void consumerFinishFailEvent(String payload) {
        log.info("Receiving notification event {} from finish-fail topic", payload);

        Event event = jsonUtil.toEvent(payload);

        service.finishSagaFail(event);
    }
}
